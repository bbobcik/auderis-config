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

import cz.auderis.tools.config.DataTranslatorContext;

import java.util.UUID;

/**
 * {@code UuidTranslator}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class UuidTranslator extends SingleTargetClassTranslator {

	public UuidTranslator() {
		super(UUID.class);
	}

	@Override
	public String getId() {
		return "UUID translator";
	}

	@Override
	protected Object translate(Object source, DataTranslatorContext context) {
		if (source instanceof String) {
			try {
				final UUID result = UUID.fromString((String) source);
				return result;
			} catch (Exception e) {
				// Exception silently consumed
			}
		}
		return null;
	}

}
