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

package cz.auderis.tools.config;

import cz.auderis.tools.config.spi.CompoundDataProvider;
import cz.auderis.tools.config.spi.MapBasedDataProvider;
import cz.auderis.tools.config.spi.SystemPropertyDataProvider;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * {@code ConfigurationData}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public final class ConfigurationData {

	public static ConfigurationDataProvider getSystemPropertyDataProvider() {
		return SystemPropertyDataProvider.instance();
	}

	public static ConfigurationDataProvider getMapDataProvider(Map<String, ?> dataSource) {
		return new MapBasedDataProvider(dataSource);
	}

	public static ConfigurationDataProvider getCompoundDataProvider(ConfigurationDataProvider... providers) {
		return new CompoundDataProvider(providers);
	}

	@SuppressWarnings("unchecked")
	public static <T> T createConfigurationObject(ConfigurationDataProvider dataProvider, Class<T> targetClass, ClassLoader clsLoader, boolean strict) {
		if ((null == dataProvider) || (null == targetClass)) {
			throw new NullPointerException();
		}
		final Class<?>[] interfaces = { targetClass };
		final ConfigurationDataAccessProxyHandler proxyHandler = new ConfigurationDataAccessProxyHandler(dataProvider, strict);
		Exception firstFailure = null;
		// First try the provided classloader
		if (null != clsLoader) {
			try {
				final T proxy = (T) Proxy.newProxyInstance(clsLoader, interfaces, proxyHandler);
				return proxy;
			} catch (Exception e) {
				// Consume exception and use fallback classloader
				firstFailure = e;
			}
		}
		// Try the classloader associated with the target class (if different from the provided one)
		final ClassLoader targetClassLoader = targetClass.getClassLoader();
		if (targetClassLoader != clsLoader) {
			try {
				final T proxy = (T) Proxy.newProxyInstance(targetClassLoader, interfaces, proxyHandler);
				return proxy;
			} catch (Exception e) {
				// Consume exception and go to next fallback classloader
				if (null == firstFailure) {
					firstFailure = e;
				}
			}
		}
		// As the last resort, try to use the classloader from current thread context
		final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		try {
			final T proxy = (T) Proxy.newProxyInstance(contextLoader, interfaces, proxyHandler);
			return proxy;
		} catch (Exception e) {
			// Silently ignored
			if (null == firstFailure) {
				firstFailure = e;
			}
		}
		throw new IllegalArgumentException("cannot create proxy class", firstFailure);
	}

	public static <T> T createConfigurationObject(ConfigurationDataProvider dataProvider, Class<T> targetClass, ClassLoader clsLoader) {
		return createConfigurationObject(dataProvider, targetClass, clsLoader, true);
	}

	public static <T> T createConfigurationObject(ConfigurationDataProvider dataProvider, Class<T> targetClass, boolean strict) {
		return createConfigurationObject(dataProvider, targetClass, null, strict);
	}

	public static <T> T createConfigurationObject(ConfigurationDataProvider dataProvider, Class<T> targetClass) {
		return createConfigurationObject(dataProvider, targetClass, null, true);
	}

	private ConfigurationData() {
		throw new AssertionError();
	}

}
