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

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * {@code EmptyResourceBundle}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public final class EmptyResourceBundle extends ResourceBundle {

	private static final EmptyResourceBundle INSTANCE = new EmptyResourceBundle();
	private static final ExtResourceBundle EXT_INSTANCE = new ExtResourceBundle(INSTANCE);

	public static ResourceBundle instance() {
		return INSTANCE;
	}

	public static ExtResourceBundle extInstance() {
		return EXT_INSTANCE;
	}

	@Override
	protected Object handleGetObject(String key) {
		return null;
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.emptyEnumeration();
	}

	private EmptyResourceBundle() {
		// Singleton class
	}

}
