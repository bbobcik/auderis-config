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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class BigIntegerTranslator extends SingleTargetClassTranslator {

	public BigIntegerTranslator() {
		super(BigInteger.class);
	}

	@Override
	public String getId() {
		return "BigInteger translator";
	}

	@Override
	protected Object translate(Object source, DataTranslatorContext context) {
		if (source instanceof BigDecimal) {
			// Such conversion is not supported
			return null;
		} else if (source instanceof Number) {
			final long longVal = ((Number) source).longValue();
			return BigInteger.valueOf(longVal);
		} else if (source instanceof String) {
			try {
				return new BigInteger((String) source);
			} catch (NumberFormatException e) {
				// Ignore exception and fall through
			}
		} else if (source instanceof byte[]) {
			final byte[] sourceBytes = (byte[]) source;
			if (0 != sourceBytes.length) {
				return new BigInteger(sourceBytes);
			}
			// Fall through
		}
		return null;
	}

}
