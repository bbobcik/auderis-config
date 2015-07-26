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

import com.google.common.collect.ImmutableMap;
import cz.auderis.test.category.UnitTest;
import cz.auderis.tools.config.annotation.ConfigurationEntry;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static cz.auderis.tools.config.SingleItemDataProvider.itemProvider;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * {@code StringDataObjectTest}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class PluginDataObjectTest {

	public interface NumericPluginDataObject {
		BigInteger getBigInteger();
		BigDecimal getBigDecimal();
	}

	public interface CorePluginDataObject {
		Pattern thePattern();
		InetAddress inetAddress();
		@ConfigurationEntry(name = "addr.ipv4") Inet4Address inetAddressIPv4();
		@ConfigurationEntry(name = "addr.ipv6") Inet6Address inetAddressIPv6();
		UUID theUUID();
	}

	public interface MultiPluginDataObject {
		TestClassA resultA();
		TestClassB resultB();
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyParseBigInteger() throws Exception {
		final Object[][] points = {
				{ "123", BigInteger.valueOf(123L) },
				{ (byte) 10, BigInteger.valueOf(10L) },
				{
						"123456789123456789123456789123456789123456789123456789123456789123456789",
						new BigInteger("123456789123456789123456789123456789123456789123456789123456789123456789")
						},

		};
		for (final Object[] point : points) {
			final ConfigurationDataProvider provider = itemProvider("bigInteger", point[0]);
			final BigInteger referenceValue = (BigInteger) point[1];
			final NumericPluginDataObject testObject = ConfigurationData.createConfigurationObject(provider, NumericPluginDataObject.class);
			assertThat(testObject.getBigInteger(), is(referenceValue));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyParseBigDecimal() throws Exception {
		final Object[][] points = {
				{ "123", BigDecimal.valueOf(123L) },
				{ "123.456", new BigDecimal("123.456") },
				{ 98.76D, BigDecimal.valueOf(98.76D) }
		};
		for (final Object[] point : points) {
			final ConfigurationDataProvider provider = itemProvider("bigDecimal", point[0]);
			final BigDecimal referenceValue = (BigDecimal) point[1];
			final NumericPluginDataObject testObject = ConfigurationData.createConfigurationObject(provider, NumericPluginDataObject.class);
			assertThat(testObject.getBigDecimal(), is(referenceValue));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyReturnValueViaPlugin() throws Exception {
		final Object[][] points = {
				{ "a\\[\\d{3}\\]", "8.8.8.8", "8.4.8.4", "2001:db8:85a3:0:0:8a2e:370:7334", "f669dd2a-6722-11e4-b116-123b93f75cba", "xa[001]x" },
		};
		for (Object[] point : points) {
			// Given
			final Object matchPattern = point[0];
			final Object referenceInetAddr = point[1];
			final Object referenceIPv4 = point[2];
			final Object referenceIPv6 = point[3];
			final String referenceUUID = (String) point[4];
			final CharSequence referenceMatch = (CharSequence) point[5];
			final Map<String, ?> dataSource = ImmutableMap.of(
					"thePattern", matchPattern,
					"inetAddress", referenceInetAddr,
					"addr.ipv4", referenceIPv4,
					"addr.ipv6", referenceIPv6,
					"theUUID", referenceUUID
			);
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);

			// When
			final CorePluginDataObject testObject = ConfigurationData.createConfigurationObject(data,
					CorePluginDataObject.class);

			// Then
			assertThat(testObject.thePattern(), instanceOf(Pattern.class));
			assertThat(testObject.inetAddress(), instanceOf(InetAddress.class));
			assertThat(testObject.inetAddressIPv4(), instanceOf(Inet4Address.class));
			assertThat(testObject.inetAddressIPv6(), instanceOf(Inet6Address.class));
			assertThat(testObject.theUUID(), instanceOf(UUID.class));
			//
			assertTrue("reference match", testObject.thePattern().matcher(referenceMatch).find());
			assertThat("generic IP address", testObject.inetAddress().getHostAddress(), is(referenceInetAddr));
			assertThat("IPv4 address", testObject.inetAddressIPv4().getHostAddress(), is(referenceIPv4));
			assertThat("IPv6 address", testObject.inetAddressIPv6().getHostAddress(), is(referenceIPv6));
			assertThat("UUID", testObject.theUUID().toString(), equalToIgnoringCase(referenceUUID));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldUseMultipleCandidatePluginsInCorrectOrder() throws Exception {
		// Given
		final Map<String, ?> dataSource = ImmutableMap.of(
				"resultA", this,
				"resultB", this
		);
		final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);

		// When
		final MultiPluginDataObject testObject = ConfigurationData.createConfigurationObject(data, MultiPluginDataObject.class);

		// Then
		assertThat("resultA", testObject.resultA(), notNullValue());
		assertThat("resultB", testObject.resultB(), notNullValue());
		assertThat(testObject.resultA().source, is(HighPriorityTranslator.ID));
		assertThat(testObject.resultB().source, is(LowPriorityTranslator.ID));
	}

	public static final class TestClassA {
		public final String source;

		public TestClassA(String source) {
			this.source = source;
		}
	}

	public static final class TestClassB {
		public final String source;

		public TestClassB(String source) {
			this.source = source;
		}
	}

	public static final class LowPriorityTranslator implements DataTranslator {

		public static final String ID = "test translator (A+B, low priority)";

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public int getTargetClassSupportPriority(Class<?> targetClass, DataTranslatorContext context) {
			if ((targetClass == TestClassA.class) || (targetClass == TestClassB.class)) {
				return 5;
			}
			return PRIORITY_NOT_SUPPORTED;
		}

		@Override
		public Object translateToClass(Object source, Class<?> targetClass, DataTranslatorContext context) {
			if (targetClass == TestClassA.class) {
				return new TestClassA(getId());
			} else if (targetClass == TestClassB.class) {
				return new TestClassB(getId());
			}
			return null;
		}
	}

	public static final class MidPriorityTranslator implements DataTranslator {

		public static final String ID = "test translator (A only, middle priority)";

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public int getTargetClassSupportPriority(Class<?> targetClass, DataTranslatorContext context) {
			if (targetClass == TestClassA.class) {
				return 10;
			}
			return PRIORITY_NOT_SUPPORTED;
		}

		@Override
		public Object translateToClass(Object source, Class<?> targetClass, DataTranslatorContext context) {
			if (targetClass == TestClassA.class) {
				return new TestClassA(getId());
			}
			return null;
		}
	}

	public static final class HighPriorityTranslator implements DataTranslator {

		public static final String ID = "test translator (A only, high priority)";

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public int getTargetClassSupportPriority(Class<?> targetClass, DataTranslatorContext context) {
			if ((targetClass == TestClassA.class) || (targetClass == TestClassB.class)) {
				return 20;
			}
			return PRIORITY_NOT_SUPPORTED;
		}

		@Override
		public Object translateToClass(Object source, Class<?> targetClass, DataTranslatorContext context) {
			if (targetClass == TestClassA.class) {
				return new TestClassA(getId());
			}
			return null;
		}
	}

}
