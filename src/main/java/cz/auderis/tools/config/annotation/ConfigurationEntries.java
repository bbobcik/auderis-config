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

import cz.auderis.tools.config.ConfigurationData;
import cz.auderis.tools.config.ConfigurationDataProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be used to mark an interface as a container of
 * configuration entry access methods. This indication is not strictly required,
 * i.e. interfaces without any class-level annotation may be used with
 * {@link ConfigurationData#createConfigurationObject(ConfigurationDataProvider, Class)}
 * as well, but the presence is recommended.
 *
 * <p>The primary purpose of this annotation is that it allows factoring of
 * common configuration entry name prefix to a single place. If
 * the prefix attribute is an empty string (which is a default value),
 * no prefix will be used. If the prefix is set to {@link #CLASS_NAME_PREFIX},
 * the simple class name (without packages) will be used as a prefix.
 *
 * <p>Example of source configuration identifier - given the following interface
 * <pre>
 *     <i>{@literal @}ConfigurationEntries( prefix = "org.example" )</i>
 *     <b>public interface</b> TestInterface {
 *         String textValue();
 *         <b>int</b> numericValue();
 *         <i>{@literal @}ConfigurationEntryName( name = "system.enabled" )</i>
 *         <b>boolean</b> enabled();
 *     }
 * </pre>
 * identifiers of the retrieved configuration entries will be:
 * <table summary="" border="1">
 *     <tr><th>Access method</th><th>Configuration entry</th></tr>
 *     <tr><td>{@code textValue()}</td><td>{@code org.example.textValue}</td></tr>
 *     <tr><td>{@code numericValue()}</td><td>{@code org.example.numericValue}</td></tr>
 *     <tr><td>{@code enabled()}</td><td>{@code org.example.system.enabled}</td></tr>
 * </table>
 *
 * <p>Notice that this annotation is not inherited. Even though child classes, when annotated,
 * will fully override the parent annotation, each configuration entry will construct its fully
 * qualified name based on the {@code ConfigurationEntries} annotation used on its declaring
 * interface.
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationEntries {

	/**
	 * When used as a {@link #prefix()} value, it is replaced at run-time by
	 * the simple class name of the annotated interface (without package parts).
	 */
	String CLASS_NAME_PREFIX = "*";

	/**
	 * Common prefix of all configuration entries within the annotated interface.
	 * @return common configuration entries prefix
	 */
	String prefix() default "";

}
