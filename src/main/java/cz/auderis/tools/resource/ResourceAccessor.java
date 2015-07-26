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

import cz.auderis.tools.config.ConfigurationDataProvider;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@code ResourceProvider}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public interface ResourceAccessor extends ConfigurationDataProvider {

	String getString(String key, String defaultString);

	boolean getBoolean(String key, boolean defaultValue);

	int getInt(String key, int defaultValue);

	long getLong(String key, long defaultValue);

	BigInteger getBigInteger(String key, BigInteger defaultValue);

	BigDecimal getBigDecimal(String key, BigDecimal defaultValue);

	<T extends Enum<T>> T getEnum(String key, Class<T> enumClass, T defaultValue);

	<T> T getObject(String key, Class<T> targetClass, T defaultValue);

	<T> T getObjectWithTextDefault(String key, Class<T> targetClass, String defaultTextValue);

}
