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

import cz.auderis.tools.config.ConfigurationDataProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code MapBasedDataProvider}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class MapBasedDataProvider implements ConfigurationDataProvider {

	private final Map<String, ?> dataSource;

	public MapBasedDataProvider(Map<String, ?> dataSource) {
		if (null == dataSource) {
			throw new NullPointerException();
		}
		this.dataSource = new HashMap<String, Object>(dataSource);
	}

	@Override
	public boolean containsKey(String key) {
		if (null == key) {
			throw new NullPointerException();
		}
		return dataSource.containsKey(key);
	}

	@Override
	public Object getRawObject(String key) {
		if (null == key) {
			throw new NullPointerException();
		}
		return dataSource.get(key);
	}

}
