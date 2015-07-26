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

public class SingleItemDataProvider implements ConfigurationDataProvider {
	private final String key;
	private final Object value;

	public static ConfigurationDataProvider itemProvider(String key, Object value) {
		return new SingleItemDataProvider(key, value);
	}

	public SingleItemDataProvider(String key, Object value) {
		if (null == key) {
			throw new NullPointerException();
		}
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean containsKey(String key) {
		return this.key.equals(key);
	}

	@Override
	public Object getRawObject(String key) {
		return this.key.equals(key) ? value : null;
	}

}
