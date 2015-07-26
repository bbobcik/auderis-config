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
import cz.auderis.tools.config.DataTranslator;
import cz.auderis.tools.config.DataTranslatorContext;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * {@code InetAddressTranslator}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class InetAddressTranslator implements DataTranslator {

	@Override
	public String getId() {
		return "inet address translator";
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public int getTargetClassSupportPriority(Class<?> targetClass, DataTranslatorContext context) {
		if (targetClass.isAssignableFrom(InetAddress.class)) {
			return DataTranslator.PRIORITY_NORMAL_SUPPORT;
		} else if (targetClass.isAssignableFrom(Inet4Address.class)) {
			return DataTranslator.PRIORITY_NORMAL_SUPPORT + 1;
		} else if (targetClass.isAssignableFrom(Inet6Address.class)) {
			return DataTranslator.PRIORITY_NORMAL_SUPPORT + 1;
		}
		return DataTranslator.PRIORITY_NOT_SUPPORTED;
	}


	@Override
	public Object translateToClass(Object source, Class<?> targetClass, DataTranslatorContext context) {
		if (null == source) {
			return null;
		}
		final Class<?> sourceClass = source.getClass();
		if (targetClass.isAssignableFrom(sourceClass)) {
			return source;
		}
		InetAddress result = null;
		if (source instanceof byte[]) {
			final byte[] addrBytes = (byte[]) source;
			try {
				result = InetAddress.getByAddress(addrBytes);
			} catch (UnknownHostException e) {
				if (context.isStrictModeEnabled()) {
					throw new ConfigurationDataException("cannot parse internet address '"
							+ Arrays.toString(addrBytes) + "'", e);
				}
				// Otherwise fall through
			}
		} else if (source instanceof String) {
			final String addrText = (String) source;
			if (addrText.trim().isEmpty()) {
				return NULL_OBJECT;
			}
			try {
				result = InetAddress.getByName((String) source);
			} catch (Exception e) {
				if (context.isStrictModeEnabled()) {
					throw new ConfigurationDataException("cannot parse internet address '" + addrText + "'", e);
				}
				// Otherwise fall through
			}
		}
		if (null == result) {
			return NULL_OBJECT;
		} else if (targetClass.isAssignableFrom(result.getClass())) {
			return result;
		} else if (context.isStrictModeEnabled()) {
			throw new ConfigurationDataException("invalid type of internet address " + result + ", expected type "
					+ targetClass.getName());
		}
		return null;
	}

}
