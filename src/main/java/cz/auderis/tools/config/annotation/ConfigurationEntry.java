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

package cz.auderis.tools.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This method-level annotation is used for specification of
 * explicit configuration entry name. If this annotation is not
 * present, by default the name of the method with optional getter
 * prefix stripped is used).
 *
 * <p>The annotation may define optional aliases, if the configuration
 * entry may be known under multiple names.
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ConfigurationEntry {

	/**
	 * Declares explicit primary name of the configuration entry.
	 *
	 * <p>If the value is empty string or the annotation is not present,
	 * the primary name is derived from the method name with optional getter
	 * prefix intelligently stripped (prefix "get" is removed only if followed
	 * by capital letter), as shown in the following examples:
	 * <ul>
	 *     <li>{@code getSimpleProperty()} is transformed to {@code simpleProperty}</li>
	 *     <li>{@code getterOfInt()} is not transformed, i.e. the name is {@code getterOfInt}</li>
	 * </ul>
	 *
	 * @return explicit name of configuration entry
	 */
	String name() default "";

	/**
	 * Declares configuration entry aliases that will be searched
	 * if no configuration entry with the primary name is found.
	 *
	 * @return array of configuration entry aliases
	 */
	String[] alias() default { };

	/**
	 * If the type of configuration entry is a collection, it is necessary to determine the legal type
	 * of the collection's items. Using this attribute the type is established for the purpose of parsing.
	 * @return
	 */
	Class<?> collectionItemType() default Void.class;

}
