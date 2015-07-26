/*
 * Copyright 2015 Boleslav Bobcik - Auderis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.auderis.tools.resource;

import cz.auderis.tools.config.ConfigurationData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * {@code ResourceLoader}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class ResourceLoader {

	private static final ThreadLocal<ResourceLoader> defaultInstance = new ThreadLocal<ResourceLoader>();

	private final List<ClassLoader> candidateClassLoaders;
	private final Locale targetLocale;
	private final String bundlePackage;

	public static ResourceLoader instance() {
		synchronized (defaultInstance) {
			ResourceLoader instance = defaultInstance.get();
			if (null == instance) {
				final List<ClassLoader> candidates = getDefaultClassLoaderCandidates();
				final Locale defaultLocale = Locale.getDefault();
				instance = new ResourceLoader(candidates, defaultLocale, null);
				defaultInstance.set(instance);
			}
			return instance;
		}
	}

	public static ResourceLoader instance(ClassLoader clsLoader, Locale locale) {
		if ((null == clsLoader) && (null == locale)) {
			return instance();
		}
		final List<ClassLoader> candidates;
		if (null == clsLoader) {
			candidates = getDefaultClassLoaderCandidates();
		} else {
			candidates = Collections.singletonList(clsLoader);
		}
		if (null == locale) {
			locale = Locale.getDefault();
		}
		return new ResourceLoader(candidates, locale, null);
	}

	public static ResourceLoader forLocale(Locale locale) {
		return instance(null, locale);
	}

	public static ResourceLoader forLocale(String language) {
		final Locale locale;
		if ((null == language) || language.trim().isEmpty()) {
			locale = null;
		} else {
			locale = new Locale(language);
		}
		return instance(null, locale);
	}

	public ResourceLoader withClassLoaders(ClassLoader... alternatives) {
		if (null == alternatives) {
			throw new NullPointerException();
		} else if (0 == alternatives.length) {
			throw new IllegalArgumentException("no classloaders were specified");
		}
		final List<ClassLoader> candidates = Arrays.asList(alternatives);
		if (candidates.equals(this.candidateClassLoaders)) {
			return this;
		}
		return new ResourceLoader(Collections.unmodifiableList(candidates), targetLocale, bundlePackage);
	}

	public ResourceLoader withFallbackClassLoaders(ClassLoader... fallbacks) {
		if (null == fallbacks) {
			throw new NullPointerException();
		} else if (0 == fallbacks.length) {
			return this;
		}
		final int classLoaderCount = candidateClassLoaders.size() + fallbacks.length;
		final List<ClassLoader> extraCandidates = Arrays.asList(fallbacks);
		final ArrayList<ClassLoader> newCandidates = new ArrayList<ClassLoader>(classLoaderCount);
		newCandidates.addAll(this.candidateClassLoaders);
		newCandidates.addAll(extraCandidates);
		return new ResourceLoader(Collections.unmodifiableList(newCandidates), targetLocale, bundlePackage);
	}

	public ResourceLoader withLocale(Locale newLocale) {
		if (null == newLocale) {
			throw new NullPointerException();
		} else if (newLocale.equals(targetLocale)) {
			return this;
		}
		return new ResourceLoader(candidateClassLoaders, newLocale, bundlePackage);
	}

	public ResourceLoader withBundlePackage(Package pkg) {
		final String newPkgName;
		if (null == pkg) {
			newPkgName = null;
		} else {
			newPkgName = pkg.getName();
		}
		if (null == newPkgName) {
			if (null == bundlePackage) {
				return this;
			}
		} else if (newPkgName.equals(bundlePackage)) {
			return this;
		}
		return new ResourceLoader(candidateClassLoaders, targetLocale, newPkgName);
	}

	public ExtResourceBundle loadResourceBundle(Class forClass) {
		return loadResources(forClass, EmptyResourceBundle.extInstance());
	}

	public ExtResourceBundle loadResources(Class forClass, ResourceBundle defaultBundle) {
		if (null == forClass) {
			throw new NullPointerException();
		}
		final ResourceBundle foundBundle = loadPlainResources(forClass, defaultBundle);
		if (null == foundBundle) {
			return null;
		} else if (foundBundle instanceof ExtResourceBundle) {
			return (ExtResourceBundle) foundBundle;
		}
		return new ExtResourceBundle(foundBundle);
	}

	public <T> T loadAsConfigurationObject(Class<?> resourceClass, Class<T> cfgObjectClass) {
		return loadAsConfigurationObject(resourceClass, cfgObjectClass, EmptyResourceBundle.instance(), null);
	}

	public <T> T loadAsConfigurationObject(Class<?> resourceClass, Class<T> cfgObjectClass, ResourceBundle defaultBundle) {
		return loadAsConfigurationObject(resourceClass, cfgObjectClass, defaultBundle, null);
	}

	public <T> T loadAsConfigurationObject(Class<?> resourceClass, Class<T> cfgObjectClass,
										   ResourceBundle defaultBundle, ClassLoader clsLoader) {
		if ((null == resourceClass) || (null == cfgObjectClass)) {
			throw new NullPointerException();
		}
		final ResourceBundle foundBundle = loadPlainResources(resourceClass, defaultBundle);
		final SimpleResourceDataProvider dataProvider;
		if (null != foundBundle) {
			dataProvider = new SimpleResourceDataProvider(foundBundle);
		} else {
			dataProvider = new SimpleResourceDataProvider(EmptyResourceBundle.instance());
		}
		final T cfgObject = ConfigurationData.createConfigurationObject(dataProvider, cfgObjectClass, clsLoader);
		return cfgObject;
	}

	public ResourceBundle loadPlainResources(Class forClass, ResourceBundle defaultBundle) {
		if (null == forClass) {
			throw new NullPointerException();
		}
		// Lookup of fully qualified name
		final String fullyQualifiedName = getQualifiedBundleName(forClass);
		for (ClassLoader loader : candidateClassLoaders) {
			if (null == loader) {
				continue;
			}
			final ResourceBundle bundle = ResourceBundle.getBundle(fullyQualifiedName, targetLocale, loader);
			if (null != bundle) {
				return bundle;
			}
		}
		// Fallback: lookup of simple name
		final String simpleName = forClass.getSimpleName();
		for (ClassLoader loader : candidateClassLoaders) {
			if (null == loader) {
				continue;
			}
			final ResourceBundle bundle = ResourceBundle.getBundle(simpleName, targetLocale, loader);
			if (null != bundle) {
				return bundle;
			}
		}
		// Nothing found, return default bundle
		return defaultBundle;
	}

	private ResourceLoader(List<ClassLoader> candidateClassLoaders, Locale targetLocale, String bundlePackage) {
		this.candidateClassLoaders = candidateClassLoaders;
		this.targetLocale = targetLocale;
		this.bundlePackage = bundlePackage;
	}

	private String getQualifiedBundleName(Class targetClass) {
		if (null == bundlePackage) {
			return targetClass.getName();
		}
		final String baseClassName = targetClass.getSimpleName();
		return bundlePackage + '.' + baseClassName;
	}

	private static List<ClassLoader> getDefaultClassLoaderCandidates() {
		final ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
		final List<ClassLoader> defaultCandidates = Collections.singletonList(defaultClassLoader);
		return defaultCandidates;
	}

}
