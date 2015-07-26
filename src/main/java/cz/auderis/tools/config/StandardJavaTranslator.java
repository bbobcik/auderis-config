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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code PrimitiveTranslator}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public final class StandardJavaTranslator {

	private static final StandardJavaTranslator INSTANCE = new StandardJavaTranslator();
	private final Map<Class<?>, PrimitiveTranslator> primitiveTranslatorMap;
	private final Map<Class<?>, Class<?>> primitiveToBoxedMap;
	private final Map<Class<?>, Class<?>> boxedToPrimitiveMap;

	public static StandardJavaTranslator instance() {
		return INSTANCE;
	}

	public boolean isPrimitiveOrBoxed(Class<?> targetType) {
		assert null != targetType;
		return primitiveTranslatorMap.containsKey(targetType);
	}

	public boolean isPrimitiveOrBoxedIntegerType(Class<?> targetType) {
		assert null != targetType;
		final PrimitiveTranslator translator = primitiveTranslatorMap.get(targetType);
		return (null != translator) && translator.isInteger();
	}

	public boolean isPrimitiveOrBoxedFloatType(Class<?> targetType) {
		assert null != targetType;
		final PrimitiveTranslator translator = primitiveTranslatorMap.get(targetType);
		return (null != translator) && !translator.isInteger();
	}

	public Class<?> switchPrimitiveAndBoxedType(Class<?> type) {
		assert null != type;
		if (type.isPrimitive()) {
			return primitiveToBoxedMap.get(type);
		}
		return boxedToPrimitiveMap.get(type);
		// Returns null if the type is neither primitive nor boxed-primitive
	}

	public Object translatePrimitive(Object source, Class<?> returnType, boolean strict) {
		assert null != returnType;
		final PrimitiveTranslator translator = primitiveTranslatorMap.get(returnType);
		assert null != translator;
		if (null == source) {
			return translator.defaultValue(returnType);
		} else if (translator.getSupportedClasses().contains(source.getClass())) {
			// The source already has the required type (or its boxed/unboxed complement)
			return source;
		} else if (source instanceof Number) {
			return translator.convertNumber((Number) source);
		} else if (source instanceof String) {
			final String primitiveStr = ((String) source).trim();
			try {
				return translator.translateString(primitiveStr);
			} catch (NumberFormatException e) {
				if (strict) {
					throw new ConfigurationDataException("cannot parse value '" + primitiveStr + "' into type "
							+ returnType.getClass().getName(), e);
				}
				// In non-strict mode, fall through to returning default value
			}
		}
		// Basic conversions failed, return default value for the given type (i.e. for boxed variants
		// return null, for true primitives return their Java default value)
		return translator.defaultValue(returnType);
	}

	public Object translateEnum(Object sourceValue, Class<?> returnType, boolean strict) {
		assert (null != returnType) && returnType.isEnum();
		if (null == sourceValue) {
			return null;
		} else if (returnType.isAssignableFrom(sourceValue.getClass())) {
			// Source is already compatible with the enum
			return sourceValue;
		} else if (!(sourceValue instanceof String)) {
			// Non-text values are not supported
			if (strict) {
				throw new ConfigurationDataException("cannot transform type " + sourceValue.getClass().getName()
						+ " into enum " + returnType.getName());
			}
			return null;
		}
		final String enumName = ((String) sourceValue).trim();
		if (enumName.isEmpty()) {
			// Get blank strings out of the way
			return null;
		}
		for (Object enumObj : returnType.getEnumConstants()) {
			final Enum enumConst = (Enum) enumObj;
			if (enumName.equalsIgnoreCase(enumConst.name())) {
				return enumConst;
			}
		}
		// Despite the defined name, an appropriate enum was not found
		if (strict) {
			throw new ConfigurationDataException("cannot find identifier '" + enumName + "' in enum "
					+ returnType.getName());
		}
		return null;
	}

	private StandardJavaTranslator() {
		primitiveTranslatorMap = new HashMap<Class<?>, PrimitiveTranslator>();
		primitiveToBoxedMap = new HashMap<Class<?>, Class<?>>();
		boxedToPrimitiveMap = new HashMap<Class<?>, Class<?>>();
		for (PrimitiveTranslator tx : PrimitiveTranslator.values()) {
			final List<Class<?>> supportedClasses = tx.getSupportedClasses();
			for (Class<?> supportedClass : supportedClasses) {
				primitiveTranslatorMap.put(supportedClass, tx);
			}
			final Class<?> primitiveType = supportedClasses.get(1);
			final Class<?> boxedType = supportedClasses.get(0);
			primitiveToBoxedMap.put(primitiveType, boxedType);
			boxedToPrimitiveMap.put(boxedType, primitiveType);
		}
	}


	@SuppressWarnings("unchecked")
	enum PrimitiveTranslator {

		BOOLEAN {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Boolean.class, (Class<?>) Boolean.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Boolean.parseBoolean(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return 0 != num.intValue();
			}

			@Override
			public Object defaultValue() {
				return false;
			}

			@Override
			public boolean isInteger() {
				return false;
			}
		},

		BYTE {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Byte.class, (Class<?>) Byte.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Byte.parseByte(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return num.byteValue();
			}

			@Override
			public Object defaultValue() {
				return (byte) 0;
			}
		},

		SHORT {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Short.class, (Class<?>) Short.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Short.parseShort(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return num.shortValue();
			}

			@Override
			public Object defaultValue() {
				return (short) 0;
			}
		},

		INT {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Integer.class, (Class<?>) Integer.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Integer.parseInt(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return num.intValue();
			}

			@Override
			public Object defaultValue() {
				return 0;
			}
		},

		LONG {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Long.class, (Class<?>) Long.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Long.parseLong(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return num.longValue();
			}

			@Override
			public Object defaultValue() {
				return 0L;
			}
		},

		FLOAT {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Float.class, (Class<?>) Float.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Float.parseFloat(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return num.floatValue();
			}

			@Override
			public Object defaultValue() {
				return 0.0F;
			}

			@Override
			public boolean isInteger() {
				return false;
			}
		},

		DOUBLE {
			@Override
			public List<Class<?>> getSupportedClasses() {
				return Arrays.asList(Double.class, (Class<?>) Double.TYPE);
			}

			@Override
			public Object translateString(String source) {
				return Double.parseDouble(source);
			}

			@Override
			public Object convertNumber(Number num) {
				return num.doubleValue();
			}

			@Override
			public Object defaultValue() {
				return 0.0D;
			}

			@Override
			public boolean isInteger() {
				return false;
			}
		},
		;

		public abstract List<Class<?>> getSupportedClasses();

		public abstract Object defaultValue();

		public Object defaultValue(Class<?> resultType) {
			return resultType.isPrimitive() ? defaultValue() : null;
		}

		public abstract Object translateString(String source);

		public abstract Object convertNumber(Number num);

		public boolean isInteger() {
			return true;
		}

	}

}
