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

/**
 * {@code DataTranslator} defines a mechanism by which an arbitrary object
 * is translated into an instance of a given class. Classes implementing
 * this interface are discovered using standard {@link java.util.ServiceLoader}.
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public interface DataTranslator {

	/**
	 * Priority 0 denotes that a class is not supported by this translator.
	 *
	 * @see #getTargetClassSupportPriority(Class, DataTranslatorContext)
	 */
	int PRIORITY_NOT_SUPPORTED = 0;

	/**
	 * The default priority means that a class is supported by this translator.
	 *
	 * @see #getTargetClassSupportPriority(Class, DataTranslatorContext)
	 */
	int PRIORITY_NORMAL_SUPPORT = 10;

	/**
	 * This constant allows the translator to indicate that the result value
	 * is {@code null}, which normally means unsuccessful translation, causing
	 * further attempts of translation.
	 */
	Object NULL_OBJECT = new Object[0];


	/**
	 * Gets an identifier of this translator. The identifier may be used
	 * for diagnostic purposes.
	 *
	 * @return id of this translator
	 */
	String getId();

	/**
	 * Determines whether the argument represents a class supported by this
	 * translator. Positive values mean that the class is supported.
	 * Unsupported classes are typically represented by
	 * {@link #PRIORITY_NOT_SUPPORTED} constant.
	 *
	 * <p>The translator that reports the highest priority will be used for the
	 * actual translation. If there are multiple translators with the same
	 * maximum priority, the choice will be arbitrary.
	 *
	 * @param targetClass class of the expected result of translation
	 * @param context context of the translation
	 *
	 * @return positive value if this translator is able to handle
	 * {@code targetClass}; zero or negative value if the class is not
	 * supported
	 */
	int getTargetClassSupportPriority(Class<?> targetClass, DataTranslatorContext context);

	/**
	 * Attempts to translate an arbitrary object into an instance of {@code targetClass}.
	 * If the translation couldn't be performed, {@code null} is returned.
	 *
	 * <p>As the translation happens while preparing configuration data, the translator
	 * receives information about the context of translation.
	 *
	 * @param source non-null object that is to be translated
	 * @param targetClass requested target class
	 * @param context context of the translation
	 *
	 * @return instance of {@code targetClass}; {@code null} if the translation cannot
	 * be performed; {@link #NULL_OBJECT} if the result should be true {@code null} value
	 */
	Object translateToClass(Object source, Class<?> targetClass, DataTranslatorContext context);

}
