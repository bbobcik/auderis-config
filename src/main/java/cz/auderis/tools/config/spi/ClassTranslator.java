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
import cz.auderis.tools.config.DataTranslatorContext;

public class ClassTranslator extends SingleTargetClassTranslator {

	public ClassTranslator() {
		super(Class.class);
	}

	@Override
	public String getId() {
		return "class translator";
	}

	@Override
	protected Object translate(Object source, DataTranslatorContext context) {
		if (source instanceof String) {
			final String clsName = (String) source;
			try {
				return Class.forName(clsName);
			} catch (ClassNotFoundException e) {
				if (context.isStrictModeEnabled()) {
					throw new ConfigurationDataException("cannot resolve class " + clsName, e);
				}
				// Fall through
			}
		}
		return null;
	}

}
