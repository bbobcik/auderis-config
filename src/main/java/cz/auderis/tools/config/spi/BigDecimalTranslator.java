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
import cz.auderis.tools.config.StandardJavaTranslator;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class BigDecimalTranslator extends SingleTargetClassTranslator {

	public BigDecimalTranslator() {
		super(BigDecimal.class);
	}

	@Override
	public String getId() {
		return "BigDecimal translator";
	}

	@Override
	protected Object translate(Object source, DataTranslatorContext context) {
		if (source instanceof BigInteger) {
			return new BigDecimal((BigInteger) source);
		} else if (source instanceof Number) {
			final Number numSource = (Number) source;
			if (StandardJavaTranslator.instance().isPrimitiveOrBoxedFloatType(numSource.getClass())) {
				return BigDecimal.valueOf(numSource.doubleValue());
			}
			return BigDecimal.valueOf(numSource.longValue());
		} else if (source instanceof String) {
			try {
				return new BigDecimal((String) source);
			} catch (NumberFormatException e) {
				// Ignore exception and fall through
			}
		} else if (source instanceof char[]) {
			final char[] sourceChars = (char[]) source;
			try {
				return new BigDecimal(sourceChars);
			} catch (NumberFormatException e) {
				// Ignore exception and fall through
			}
		}
		return null;
	}

}
