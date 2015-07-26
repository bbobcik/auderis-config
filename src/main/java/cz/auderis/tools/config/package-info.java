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

/**
 * Provides mechanism for semantically safe access to configuration data. The term "configuration
 * data" represents data that are:
 * <ul>
 *     <li>Usually immutable during the application process life</li>
 *     <li>Represented by primitive entities (such as strings, numbers etc.) or low-level, less-structured classes,
 *     such as {@link java.util.UUID}, {@link java.net.InetAddress} and similar</li>
 * </ul>
 * The typical sources of configuration data may be for example property files, resource bundles, one-time queries
 * to a database or LDAP, system properties and others.
 *
 * <p>This package allows turning regular interfaces into accessors of configuration data. This approach
 * has several benefits:
 * <ul>
 *     <li>Programmer defines configuration entries semantics (data type + entry name) at compile time.</li>
 *     <li>Configuration entries may (<em>should!</em>) be documented via normal Javadoc.</li>
 *     <li>Usages of configuration entries may be found by usual IDE tools.</li>
 *     <li>Client of configuration data accessor doesn't have to be concerned with the actual source
 *     of configuration data.</li>
 *     <li>A certain level of intelligence is used to turn configuration data source values into requested
 *     data types. For example text-represented numbers would be correctly transformed to primitives.</li>
 * </ul>
 *
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
package cz.auderis.tools.config;
