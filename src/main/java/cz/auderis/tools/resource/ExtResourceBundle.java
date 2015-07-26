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
import cz.auderis.tools.config.StandardJavaTranslator;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * {@code ExtResourceBundle}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class ExtResourceBundle extends ResourceBundle implements ResourceAccessor {

	public ExtResourceBundle(ResourceBundle decoratedBundle) {
		super();
		if (null == decoratedBundle) {
			throw new NullPointerException();
		}
		setParent(decoratedBundle);
	}

	@Override
	public Enumeration<String> getKeys() {
		// This class is only a decorator, no extra keys are provided
		return parent.getKeys();
	}

	@Override
	protected Object handleGetObject(String key) {
		// Delegate all object retrievals to parent bundle
		return null;
	}

	@Override
	public Locale getLocale() {
		return parent.getLocale();
	}


	@Override
	public Object getRawObject(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getObject(key);
	}

	public String getString(String key, String defaultString) {
		if (!containsKey(key)) {
			return defaultString;
		}
		return getString(key);
	}

	public int getInt(String key, int defaultValue) {
		if (containsKey(key)) {
			final Object object = getObject(key);
			if (object instanceof Integer) {
				return (Integer) object;
			} else if (object instanceof String) {
				try {
					return Integer.parseInt((String) object);
				} catch (NumberFormatException e) {
					// exception ignored
				}
			}
		}
		return defaultValue;
	}

	public long getLong(String key, long defaultValue) {
		if (containsKey(key)) {
			final Object object = getObject(key);
			if (object instanceof Long) {
				return (Long) object;
			} else if (object instanceof String) {
				try {
					return Long.parseLong((String) object);
				} catch (NumberFormatException e) {
					// exception ignored
				}
			}
		}
		return defaultValue;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		if (containsKey(key)) {
			final Object object = getObject(key);
			if (object instanceof Boolean) {
				return (Boolean) object;
			} else if (object instanceof String) {
				return Boolean.parseBoolean((String) object);
			}
		}
		return defaultValue;
	}

	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass, T defaultValue) {
		if ((null == key) || (null == enumClass)) {
			throw new NullPointerException();
		} else if (!enumClass.isEnum()) {
			throw new IllegalArgumentException("class is not an enum");
		}
		if (containsKey(key)) {
			final Object object = getObject(key);
			if (enumClass.isAssignableFrom(object.getClass())) {
				return enumClass.cast(object);
			} else if (object instanceof String) {
				final String enumName = (String) object;
				final T[] items = enumClass.getEnumConstants();
				for (T item : items) {
					if (item.name().equals(enumName)) {
						return item;
					}
				}
			}
		}
		return defaultValue;
	}

	public BigInteger getBigInteger(String key, BigInteger defaultValue) {
		if (containsKey(key)) {
			final Object object = getObject(key);
			if (object instanceof BigInteger) {
				return (BigInteger) object;
			} else if (object instanceof Long) {
				return BigInteger.valueOf((Long) object);
			} else if (object instanceof Integer) {
				return BigInteger.valueOf((long) (Integer) object);
			} else if (object instanceof String) {
				try {
					return new BigInteger((String) object);
				} catch (NumberFormatException e) {
					// Exception ignored
				}
			}
		}
		return defaultValue;
	}

	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		if (containsKey(key)) {
			final Object object = getObject(key);
			if (object instanceof BigDecimal) {
				return (BigDecimal) object;
			} else if (object instanceof Long) {
				return BigDecimal.valueOf((Long) object);
			} else if (object instanceof Integer) {
				return BigDecimal.valueOf((long) (Integer) object);
			} else if (object instanceof String) {
				try {
					return new BigDecimal((String) object);
				} catch (NumberFormatException e) {
					// Exception ignored
				}
			}
		}
		return defaultValue;
	}

	public <T> T getObject(String key, Class<T> targetClass, T defaultValue) {
		if ((null == key) || (null == targetClass)) {
			throw new NullPointerException();
		}
		if (containsKey(key)) {
			final Object object = getObject(key);
			final Class<?> objectClass = object.getClass();
			if (targetClass.isAssignableFrom(objectClass)) {
				return targetClass.cast(object);
			}
			// Try to use an appropriate constructor
			try {
				final Constructor<T> constructor = targetClass.getConstructor(objectClass);
				final T result = constructor.newInstance(object);
				return result;
			} catch (Exception e) {
				// Exceptions ignored
			}
			final Class<?> altObjectClass = StandardJavaTranslator.instance().switchPrimitiveAndBoxedType(objectClass);
			if (null != altObjectClass) {
				try {
					final Constructor<T> constructor = targetClass.getConstructor(altObjectClass);
					final T result = constructor.newInstance(object);
					return result;
				} catch (Exception e) {
					// Exceptions ignored
				}
			}
		}
		return defaultValue;
	}

	@Override
	public <T> T getObjectWithTextDefault(String key, Class<T> targetClass, String defaultTextValue) {
		if ((null == key) || (null == targetClass)) {
			throw new NullPointerException();
		}
		if (containsKey(key)) {
			final Object object = getObject(key);
			final Class<?> objectClass = object.getClass();
			if (targetClass.isAssignableFrom(objectClass)) {
				return targetClass.cast(object);
			}
			// Try to use an appropriate constructor
			try {
				final Constructor<T> constructor = targetClass.getConstructor(objectClass);
				final T result = constructor.newInstance(object);
				return result;
			} catch (Exception e) {
				// Exceptions ignored
			}
		}
		if ((null == defaultTextValue) || defaultTextValue.isEmpty()) {
			return null;
		}
		try {
			final Constructor<T> constructor = targetClass.getConstructor(String.class);
			final T defaultResult = constructor.newInstance(defaultTextValue);
			return defaultResult;
		} catch (Exception e) {
			final String msg = String.format("Resource (class %s) cannot be constructed from default text value %s: %s",
					targetClass.getName(), defaultTextValue, e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}

	public <T> T getConfigurationObject(Class<T> configObjectClass, ClassLoader clsLoader) {
		final T result = ConfigurationData.createConfigurationObject(this, configObjectClass, clsLoader);
		return result;
	}

	public <T> T getConfigurationObject(Class<T> configObjectClass) {
		final T result = ConfigurationData.createConfigurationObject(this, configObjectClass);
		return result;
	}

}
