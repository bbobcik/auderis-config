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
import cz.auderis.tools.config.annotation.PatternMode;

import java.lang.reflect.AnnotatedElement;
import java.util.regex.Pattern;

/**
 * {@code PatternTranslator}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class PatternTranslator extends SingleTargetClassTranslator {

	public PatternTranslator() {
		super(Pattern.class);
	}

	@Override
	public String getId() {
		return "pattern translator";
	}

	@Override
	protected Object translate(Object source, DataTranslatorContext context) {
		if (source instanceof String) {
			final int regexFlags = determinePatternMode(context);
			try {
				@SuppressWarnings("MagicConstant")
				final Pattern pattern = Pattern.compile((String) source, regexFlags);
				return pattern;
			} catch (Exception e) {
				// Exception consumed
			}
		}
		return null;
	}

	private int determinePatternMode(DataTranslatorContext context) {
		final AnnotatedElement element = (null != context) ? context.getTargetElement() : null;
		final PatternMode modeAnnotation = (element != null) ? element.getAnnotation(PatternMode.class) : null;
		final int mode = (null != modeAnnotation) ? modeAnnotation.value() : 0;
		return mode;
	}

}
