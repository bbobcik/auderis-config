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

package cz.auderis.tools.config.spi;

import cz.auderis.tools.config.ConfigurationDataException;
import cz.auderis.tools.config.DataTranslator;
import cz.auderis.tools.config.DataTranslatorContext;
import cz.auderis.tools.config.annotation.ConfigurationEntry;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class EnumSetTranslator implements DataTranslator {

	private static final String ENUM_ID_SEPARATOR_CHARS = ",;: \t\r\n";

	@Override
	public String getId() {
		return "enum set translator";
	}

	@Override
	public int getTargetClassSupportPriority(Class<?> targetClass, DataTranslatorContext context) {
		if (!targetClass.isAssignableFrom(EnumSet.class)) {
			return DataTranslator.PRIORITY_NOT_SUPPORTED;
		}
		final Class<? extends Enum<?>> enumClass = getTargetEnumClass(context);
		if (null == enumClass) {
			return DataTranslator.PRIORITY_NOT_SUPPORTED;
		}
		return DataTranslator.PRIORITY_NORMAL_SUPPORT;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object translateToClass(Object source, Class<?> targetClass, DataTranslatorContext context) {
		if (!targetClass.isAssignableFrom(EnumSet.class)) {
			return null;
		}
		// Determine the type of enum used for collection elements
		final Class<? extends Enum<?>> enumClass = getTargetEnumClass(context);
		if (enumClass == null) {
			return null;
		} else if (null == source) {
			// Translate source null into empty enum set
			return createEmptyEnumSet(enumClass);
		}
		final Class<?> sourceClass = source.getClass();
		if (Collection.class.isAssignableFrom(sourceClass)) {
			final Collection srcCollection = (Collection<?>) source;
			return translateSourceCollection(enumClass, srcCollection);
		} else if (enumClass.equals(sourceClass)) {
			// Return single item set
			final Set result = createEmptyEnumSet(enumClass);
			result.add(enumClass.cast(source));
			return result;
		} else if (String.class.isAssignableFrom(sourceClass)) {
			final String srcText = (String) source;
			return translateSourceText(enumClass, srcText);
		}
		return null;
	}

	private Class<? extends Enum<?>> getTargetEnumClass(DataTranslatorContext context) {
		final AnnotatedElement targetElement = context.getTargetElement();
		final ConfigurationEntry entryAnnotation = targetElement.getAnnotation(ConfigurationEntry.class);
		if (null == entryAnnotation) {
			return null;
		}
		final Class<?> itemClass = entryAnnotation.collectionItemType();
		if ((null == itemClass) || !itemClass.isEnum()) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) itemClass;
		return enumClass;
	}

	@Override
	public String toString() {
		return getId();
	}

	@SuppressWarnings("unchecked")
	private Object translateSourceText(Class<? extends Enum<?>> enumClass, String srcText) {
		final Set result = createEmptyEnumSet(enumClass);
		Map<String, Enum<?>> enumConstantById = null;
		final StringTokenizer parser = new StringTokenizer(srcText, ENUM_ID_SEPARATOR_CHARS);
		while (parser.hasMoreTokens()) {
			final String enumId = parser.nextToken();
			assert (null != enumId) && !enumId.trim().isEmpty();
			if (null == enumConstantById) {
				// Lazy initialization
				enumConstantById = createEnumIdMap(enumClass);
			}
			final Enum<?> enumItem = enumConstantById.get(enumId);
			if (null == enumItem) {
				throw new ConfigurationDataException("invalid enum name '" + enumId + "' for enum set of type "
						+ enumClass.getName());
			}
			result.add(enumItem);
		}
		return result;
	}

	private Object translateSourceCollection(Class<? extends Enum<?>> enumClass, Collection srcCollection) {
		if (srcCollection.isEmpty()) {
			return createEmptyEnumSet(enumClass);
		}
		int typeMismatches = 0;
		Class<?> firstIncompatibleType = null;
		for (final Object srcItem : srcCollection) {
			if (null == srcItem) {
				// Enum set cannot contain null items in any case
				return null;
			}
			final Class<?> srcItemClass = srcItem.getClass();
			if (!enumClass.isAssignableFrom(srcItemClass)) {
				++typeMismatches;
				if (null == firstIncompatibleType) {
					firstIncompatibleType = srcItemClass;
				}
			}
		}
		if (typeMismatches >= Math.min(2, srcCollection.size())) {
			// Most probably the source is intended for different translator
			return null;
		} else if (null != firstIncompatibleType) {
			throw new ConfigurationDataException("invalid source for enum set of type " + enumClass.getName()
					+ ", contains item of class " + firstIncompatibleType.getName());
		}
		try {
			return EnumSet.copyOf(srcCollection);
		} catch (Exception e) {
			throw new ConfigurationDataException("invalid source for enum set of type " + enumClass.getName(), e);
		}
	}

	private static Set<?> createEmptyEnumSet(Class<? extends Enum<?>> enumClass) {
		// Circumvent compilation checks by double cast
		@SuppressWarnings({ "unchecked", "RedundantCast" })
		final Class<Enum> elementClass = (Class<Enum>) ((Object) enumClass);
		final Set<?> result = EnumSet.noneOf(elementClass);
		return result;
	}

	private static Map<String, Enum<?>> createEnumIdMap(Class<? extends Enum<?>> enumClass) {
		final Enum<?>[] constants = enumClass.getEnumConstants();
		assert (null != constants) && (constants.length > 0);
		final Map<String, Enum<?>> mapById = new HashMap<String, Enum<?>>(constants.length);
		boolean hasCaseAmbiguousEntries = false;
		// Try using normalized (lower-case) IDs first
		for (final Enum<?> enumConst : constants) {
			final String name = enumConst.name();
			final Object prevConst = mapById.put(name.toLowerCase(), enumConst);
			if (null != prevConst) {
				hasCaseAmbiguousEntries = true;
				break;
			}
		}
		final Map<String, Enum<?>> result;
		if (hasCaseAmbiguousEntries) {
			// Redo the contents of the map
			mapById.clear();
			for (final Enum<?> enumConst : constants) {
				final String name = enumConst.name();
				mapById.put(name, enumConst);
			}
			result = mapById;
		} else {
			result = new SimpleKeyNormalizingMap<Enum<?>>(mapById);
		}
		return result;
	}

}
