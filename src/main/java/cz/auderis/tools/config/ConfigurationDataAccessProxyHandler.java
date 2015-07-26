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

import cz.auderis.tools.config.annotation.ConfigurationEntries;
import cz.auderis.tools.config.annotation.ConfigurationEntry;
import cz.auderis.tools.config.annotation.DefaultConfigurationEntryValue;

import java.lang.ref.SoftReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@code ResourceProxyHandler}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
class ConfigurationDataAccessProxyHandler implements InvocationHandler {

	private static final String GETTER_PREFIX = "get";
	private static final String GETTER_PREFIX_BOOLEAN = "is";
	private static final Object NULL_CACHE_ENTRY = new Object();

	private final ConfigurationDataProvider dataProvider;
	private final ConcurrentMap<Method, SoftReference<Object>> cache;
	private final boolean strictMode;

	ConfigurationDataAccessProxyHandler(ConfigurationDataProvider dataProvider, boolean strictMode) {
		assert null != dataProvider;
		this.dataProvider = dataProvider;
		this.cache = new ConcurrentHashMap<Method, SoftReference<Object>>();
		this.strictMode = strictMode;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		assert (null == args) || (0 != args.length);
		// Try to reuse cached value
		if ((null == args) && cache.containsKey(method)) {
			final SoftReference<Object> resultRef = cache.get(method);
			final Object result = resultRef.get();
			if (result == NULL_CACHE_ENTRY) {
				return null;
			} else if (null != result) {
				return result;
			}
			// Soft-reference content was probably destroyed by garbage collector,
			// remove stale cache entry and fall-back to normal value resolution
			cache.remove(method, resultRef);
		}
		// Compute result
		final String keyName = getResourceKey(method);
		assert null != keyName;
		final Object sourceValue;
		if (dataProvider.containsKey(keyName)) {
			sourceValue = dataProvider.getRawObject(keyName);
		} else {
			sourceValue = getDefaultSourceValue(method);
		}
		final Object result = translateObject(sourceValue, method, args);
		// Cache result (only for no-argument methods)
		if (null == args) {
			final Object cachedValue = (null != result) ? result : NULL_CACHE_ENTRY;
			cache.put(method, new SoftReference<Object>(cachedValue));
		}
		return result;
	}

	private Object getDefaultSourceValue(Method method) {
		final DefaultConfigurationEntryValue defaultValAnnotation = method.getAnnotation(DefaultConfigurationEntryValue.class);
		String result = (null != defaultValAnnotation) ? defaultValAnnotation.value() : null;
		if ((null != result) && result.isEmpty()) {
			result = null;
		}
		return result;
	}

	private Object translateObject(Object sourceValue, Method method, Object[] args) {
		final Class<?> returnType = method.getReturnType();
		// Case 1: handle text-based types
		if (String.class == returnType) {
			final String textResult = translateToString(sourceValue, args);
			return textResult;
		}
		final StandardJavaTranslator standardTranslator = StandardJavaTranslator.instance();
		// Case 2: handle enums (strict mode = throws exception if enum cannot be resolved)
		if (returnType.isEnum()) {
			final Object enumResult = standardTranslator.translateEnum(sourceValue, returnType, strictMode);
			return enumResult;
		}
		// Handle primitive types regardless whether they are boxed or unboxed (strict mode = throws exception
		// if the text-to-number parser fails)
		if (standardTranslator.isPrimitiveOrBoxed(returnType)) {
			final Object primitiveResult = standardTranslator.translatePrimitive(sourceValue, returnType, strictMode);
			return primitiveResult;
		}
		// Try to apply plugin data translators
		final Object pluginResult = tryPluginTranslator(sourceValue, returnType, method, args);
		if (null != pluginResult) {
			return (DataTranslator.NULL_OBJECT != pluginResult) ? pluginResult : null;
		}
		// Attempt to construct target type by feeding the source value into an appropriate constructor
		final Object constructedResult = tryConstruct(sourceValue, returnType, args);
		if (null != constructedResult) {
			return constructedResult;
		}
		// Try special cases
		if (null != sourceValue) {
			if ((Class.class == returnType) && (sourceValue instanceof String)) {
				try {
					final Class<?> classResult = Class.forName((String) sourceValue);
					return classResult;
				} catch (ClassNotFoundException e) {
					// Exception ignored
				}
			}
		}
		return null;
	}

	private String translateToString(Object sourceValue, Object[] args) {
		if (null == sourceValue) {
			return "";
		} else if (sourceValue instanceof String) {
			if (null != args) {
				try {
					return MessageFormat.format((String) sourceValue, args);
				} catch (Exception e) {
					// Fall through
				}
			}
			return (String) sourceValue;
		}
		return sourceValue.toString();
	}

	private Object tryPluginTranslator(Object sourceValue, Class targetClass, AnnotatedElement element, Object[] args) {
		final ServiceLoader<DataTranslator> translators = ServiceLoader.load(DataTranslator.class);
		final Iterator<DataTranslator> translatorIterator = translators.iterator();
		final List<TranslatorCandidate> applicableTranslators = new ArrayList<TranslatorCandidate>(2);
		final DataTranslatorContext context = new DataTranslatorContextImpl(element, args, strictMode);
		// Find translator candidates
		while (true) {
			boolean hasNext;
			try {
				hasNext = translatorIterator.hasNext();
			} catch (ServiceConfigurationError e) {
				hasNext = false;
			}
			if (!hasNext) {
				break;
			}
			try {
				final DataTranslator translator = translatorIterator.next();
				final int supportPriority = translator.getTargetClassSupportPriority(targetClass, context);
				if (supportPriority > DataTranslator.PRIORITY_NOT_SUPPORTED) {
					final TranslatorCandidate candidate = new TranslatorCandidate(translator, supportPriority);
					applicableTranslators.add(candidate);
				}
			} catch (Exception e) {
				// Silently ignored
			}
		}
		// Try to apply translator candidates (in the order of their priorities)
		// until one of them returns non-null value
		if (!applicableTranslators.isEmpty()) {
			Collections.sort(applicableTranslators);
			for (TranslatorCandidate candidate : applicableTranslators) {
				try {
					final DataTranslator selectedTranslator = candidate.translator;
					final Object result = selectedTranslator.translateToClass(sourceValue, targetClass, context);
					if (null != result) {
						return result;
					}
				} catch (Exception e) {
					// Silently ignored
				}
			}
		}
		return null;
	}

	private Object tryConstruct(Object sourceValue, Class<?> returnType, Object[] args) {
		if (null == sourceValue) {
			return null;
		}
		try {
			if (null == args) {
				final Object[] paramRef = { sourceValue };
				final Constructor<?> constructor = findSingleArgumentConstructor(returnType, paramRef);
				if (null == constructor) {
					return null;
				}
				// Use value from paramRef in case it was necessary to convert the source value
				final Object result = constructor.newInstance(paramRef[0]);
				return result;
			}
			// More complex case - append method arguments
			final Class[] argumentTypes = new Class[1 + args.length];
			argumentTypes[0] = sourceValue.getClass();
			for (int i=0; i<args.length; ++i) {
				final Object arg = args[i];
				final Class argClass;
				if (null == arg) {
					argClass = null;
				} else {
					argClass = arg.getClass();
				}
				argumentTypes[i + 1] = argClass;
			}
			// Enumerate all constructors and try to find the one matching the argument list
			final Constructor<?>[] constructors = returnType.getConstructors();
			for (Constructor<?> constructor : constructors) {
				final Class<?>[] parameterTypes = constructor.getParameterTypes();
				final boolean paramMatch = matchParameters(parameterTypes, argumentTypes);
				if (paramMatch) {
					final Object[] extArgs = new Object[1 + args.length];
					extArgs[0] = sourceValue;
					for (int i=0; i<args.length; ++i) {
						extArgs[i + 1] = args[0];
					}
					final Object result = constructor.newInstance(extArgs);
					return result;
				}
			}
		} catch (Exception e) {
			// Silently ignored
		}
		return null;
	}

	private Constructor<?> findSingleArgumentConstructor(Class<?> type, Object[] paramRef) {
		final Object param = paramRef[0];
		assert null != param;
		final Class<?> paramClass = param.getClass();
		try {
			final Constructor<?> constructor = type.getConstructor(paramClass);
			return constructor;
		} catch (NoSuchMethodException e) {
			// Silently ignored
		}
		// If the paramClass represents a primitive value, try to use the boxed variant
		// (or vice versa)
		final StandardJavaTranslator stdTranslator = StandardJavaTranslator.instance();
		final Class<?> altParamClass = stdTranslator.switchPrimitiveAndBoxedType(paramClass);
		if (null != altParamClass) {
			try {
				final Constructor<?> constructor = type.getConstructor(altParamClass);
				return constructor;
			} catch (NoSuchMethodException e) {
				// Silently ignored
			}
		}
		// Try other strategies only if the parameter is a string
		if (String.class == paramClass) {
			final String textParam = (String) param;
			for (Constructor<?> candidate : type.getConstructors()) {
				final Class<?>[] candidateArgTypes = candidate.getParameterTypes();
				if (1 != candidateArgTypes.length) {
					continue;
				}
				final Class<?> argType = candidateArgTypes[0];
				assert null != argType;
				// Try to convert the parameter into primitive value
				if (stdTranslator.isPrimitiveOrBoxed(argType)) {
					final Object convertedParam = stdTranslator.translatePrimitive(textParam, argType, false);
					if (null != convertedParam) {
						paramRef[0] = convertedParam;
						return candidate;
					}
				}
			}
		}
		return null;
	}

	private boolean matchParameters(Class<?>[] parameterTypes, Class[] argumentTypes) {
		if (parameterTypes.length != argumentTypes.length) {
			return false;
		}
		for (int i=0; i<parameterTypes.length; ++i) {
			final Class<?> paramType = parameterTypes[i];
			final Class<?> argType = argumentTypes[i];
			if (null == argType) {
				// Wildcard - null can be used for all non-primitive types
				if (paramType.isPrimitive()) {
					return false;
				}
			} else if (!paramType.isAssignableFrom(argType)) {
				return false;
			}
		}
		return true;
	}

	private String getResourceKey(Method method) {
		final String keyPrefix = getResourceKeyPrefix(method);
		final ConfigurationEntry nameAnnotation = method.getAnnotation(ConfigurationEntry.class);
		if (null != nameAnnotation) {
			final String keyName = nameAnnotation.name();
			final boolean keyNameDefined = (null != keyName) && !keyName.isEmpty();
			if (keyNameDefined && dataProvider.containsKey(keyPrefix + keyName)) {
				return keyPrefix + keyName;
			}
			final String[] keyAliases = nameAnnotation.alias();
			if ((null != keyAliases) && (0 != keyAliases.length)) {
				// Some aliases are defined
				for (String alias : keyAliases) {
					if ((null != alias) && !alias.isEmpty() && dataProvider.containsKey(keyPrefix + alias)) {
						return keyPrefix + alias;
					}
				}
			}
			// If the explicit name was defined, use it in all cases (i.e. don't use the implicit name)
			if (keyNameDefined) {
				return keyPrefix + keyName;
			}
		}
		// There is no annotation present or the name is empty, derive name from method
		final String methodName = method.getName();
		final boolean booleanGetter = isBasicBooleanGetter(method);
		final String keyName = trimOptionalGetterPrefix(methodName, booleanGetter);
		return keyPrefix + keyName;
	}

	private String getResourceKeyPrefix(Member method) {
		final Class<?> declaringClass = method.getDeclaringClass();
		final ConfigurationEntries prefixAnnotation = declaringClass.getAnnotation(ConfigurationEntries.class);
		if (null == prefixAnnotation) {
			return "";
		}
		final String explicitPrefix = prefixAnnotation.prefix();
		if ((null == explicitPrefix) || explicitPrefix.trim().isEmpty()) {
			return "";
		}
		final StringBuilder resultPrefix = new StringBuilder();
		if (ConfigurationEntries.CLASS_NAME_PREFIX.equals(explicitPrefix)) {
			resultPrefix.append(declaringClass.getSimpleName());
		} else {
			resultPrefix.append(explicitPrefix);
		}
		if (resultPrefix.charAt(resultPrefix.length() - 1) != '.') {
			resultPrefix.append('.');
		}
		return resultPrefix.toString();
	}

	private String trimOptionalGetterPrefix(String methodName, boolean considerBooleanPrefix) {
		final String prefix;
		if (methodName.startsWith(GETTER_PREFIX)) {
			prefix = GETTER_PREFIX;
		} else if (considerBooleanPrefix && methodName.startsWith(GETTER_PREFIX_BOOLEAN)) {
			prefix = GETTER_PREFIX_BOOLEAN;
		} else {
			return methodName;
		}
		final String propertyName = methodName.substring(prefix.length());
		final char firstChar = propertyName.charAt(0);
		if (!Character.isUpperCase(firstChar)) {
			// This is probably not a getter
			return methodName;
		}
		return Character.toLowerCase(firstChar) + propertyName.substring(1);
	}

	private boolean isBasicBooleanGetter(Method method) {
		final Class<?> resultType = method.getReturnType();
		if (Boolean.TYPE != resultType) {
			return false;
		} else if (0 != method.getParameterTypes().length) {
			return false;
		}
		return true;
	}

	protected static final class TranslatorCandidate implements Comparable<TranslatorCandidate> {

		final DataTranslator translator;
		final int priority;

		TranslatorCandidate(DataTranslator translator, int priority) {
			this.translator = translator;
			this.priority = priority;
		}

		@Override
		public int compareTo(TranslatorCandidate other) {
			return other.priority - this.priority;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if ((null == obj) || (getClass() != obj.getClass())) {
				return false;
			}
			final TranslatorCandidate other = (TranslatorCandidate) obj;
			return (priority == other.priority) && translator.equals(other.translator);
		}

		@Override
		public int hashCode() {
			return priority;
		}
	}


	protected static final class DataTranslatorContextImpl implements DataTranslatorContext {

		private final AnnotatedElement element;
		private final Object[] arguments;
		private final boolean strictMode;

		DataTranslatorContextImpl(AnnotatedElement element, Object[] arguments, boolean strictMode) {
			this.element = element;
			this.arguments = arguments;
			this.strictMode = strictMode;
		}

		@Override
		public AnnotatedElement getTargetElement() {
			return element;
		}

		@Override
		public Object[] getTargetArguments() {
			return arguments;
		}

		@Override
		public boolean isStrictModeEnabled() {
			return strictMode;
		}
	}

}
