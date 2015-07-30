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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
final class SimpleKeyNormalizingMap<T> implements Map<String, T> {
	private final Map<String, T> delegate;

	SimpleKeyNormalizingMap(Map<String, T> delegate) {
		this.delegate = delegate;
	}

	protected Object normalized(Object key) {
		if (key instanceof String) {
			return ((String) key).toLowerCase();
		}
		return key;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(normalized(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public T get(Object key) {
		return delegate.get(normalized(key));
	}

	@Override
	public T put(String key, T value) {
		return delegate.put((String) normalized(key), value);
	}

	@Override
	public T remove(Object key) {
		return delegate.remove(normalized(key));
	}

	@Override
	public Collection<T> values() {
		return delegate.values();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public void putAll(Map<? extends String, ? extends T> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<String, T>> entrySet() {
		throw new UnsupportedOperationException();
	}

}
