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

import cz.auderis.test.category.UnitTest;
import cz.auderis.tools.config.annotation.ConfigurationEntries;
import cz.auderis.tools.config.annotation.ConfigurationEntry;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@code ConfigurationEntryNamingTest}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class ConfigurationEntryNamingTest {

	public interface TestDataObject {
		// Getter names
		String getName();
		boolean isEnabled();
		int getCount();

		// Plain names
		String getterName();
		boolean isystemActive();
		int isDelta();
		Boolean isArmed();

		// Override
		@ConfigurationEntry(name = "getId") String getId();
	}

	@ConfigurationEntries(prefix = "test1")
	public interface TestDataParent {
		String a();
		String b();
		String c();
		String d();
	}

	public interface TestDataMiddle extends TestDataParent {
		@Override String b();
		String e();
		String f();
		String g();
	}

	@ConfigurationEntries(prefix = ConfigurationEntries.CLASS_NAME_PREFIX)
	public interface TestDataMiddle2 extends TestDataMiddle {
		@Override String c();
		@Override String f();
		String h();
		String i();
	}

	@ConfigurationEntries(prefix = "test2")
	public interface TestDataChild extends TestDataMiddle2 {
		@Override String d();
		@Override String g();
		@Override String i();
		String j();
	}


	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyResolveGetterNames() throws Exception {
		Object[][] dataPoints = {
				{ "a", false, 5 },
				{ "x", true, 10 },
		};
		for (Object[] dataPoint : dataPoints) {
			Map<String, Object> dataSource = new HashMap<String, Object>();
			dataSource.put("name", dataPoint[0]);
			dataSource.put("enabled", dataPoint[1]);
			dataSource.put("count", dataPoint[2]);
			dataSource.put("getName", "X" + dataPoint[0]);
			dataSource.put("isEnabled", !((Boolean) dataPoint[1]));
			dataSource.put("getCount", 1+((Integer) dataPoint[2]));
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);
			final TestDataObject testObject = ConfigurationData.createConfigurationObject(data, TestDataObject.class);
			assertThat(testObject.getName(), is(dataPoint[0]));
			assertThat(testObject.isEnabled(), is(dataPoint[1]));
			assertThat(testObject.getCount(), is(dataPoint[2]));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyResolveNonGetterNames() throws Exception {
		Object[][] dataPoints = {
				{ "a", false, 5, false, "9988" },
				{ "x", true, 10, true, "abcd" },
		};
		for (Object[] dataPoint : dataPoints) {
			Map<String, Object> dataSource = new HashMap<String, Object>();
			dataSource.put("getterName", dataPoint[0]);
			dataSource.put("isystemActive", dataPoint[1]);
			dataSource.put("isDelta", dataPoint[2]);
			dataSource.put("armed", dataPoint[3]);
			dataSource.put("getId", dataPoint[4]);
			dataSource.put("id", "XXX" + dataPoint[4]);
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);
			//
			final TestDataObject testObject = ConfigurationData.createConfigurationObject(data, TestDataObject.class);
			//
			assertThat(testObject.getterName(), is(dataPoint[0]));
			assertThat(testObject.isystemActive(), is(dataPoint[1]));
			assertThat(testObject.isDelta(), is(dataPoint[2]));
			assertThat(testObject.isArmed(), nullValue());
			assertThat(testObject.getId(), is(dataPoint[4]));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldUseSimplePrefixForFlatInterface() throws Exception {
		// Given
		final ConfigurationDataProvider cfgData = identityProvider("test1", "X", "a", "b", "c", "d");
		// When
		final TestDataParent testObject = ConfigurationData.createConfigurationObject(cfgData, TestDataParent.class);
		// Then
		assertThat(testObject.a(), is("aX"));
		assertThat(testObject.b(), is("bX"));
		assertThat(testObject.c(), is("cX"));
		assertThat(testObject.d(), is("dX"));
	}

	@Test
	@Category(UnitTest.class)
	public void shouldInheritPrefixFromInterfaceAnnotation() throws Exception {
		// Given
		final ConfigurationDataProvider data1 = identityProvider("test1", "X", "a", "b", "c", "d");
		final ConfigurationDataProvider data2 = identityProvider(null, "Y", "b", "e", "f", "g");
		final ConfigurationDataProvider cfgData = union(data1, data2);
		// When
		final TestDataMiddle testObject = ConfigurationData.createConfigurationObject(cfgData, TestDataMiddle.class);
		// Then
		assertThat(testObject.a(), is("aX"));
		assertThat(testObject.b(), is("bY"));
		assertThat(testObject.c(), is("cX"));
		assertThat(testObject.d(), is("dX"));
		assertThat(testObject.e(), is("eY"));
		assertThat(testObject.f(), is("fY"));
		assertThat(testObject.g(), is("gY"));
	}

	@Test
	@Category(UnitTest.class)
	public void shouldNameEntriesByDeclaringClass() throws Exception {
		// Given
		final ConfigurationDataProvider data1 = identityProvider("test1", "K", "a", "b", "c", "d");
		final ConfigurationDataProvider data2 = identityProvider(null, "L", "b", "e", "f", "g");
		final ConfigurationDataProvider data3 = identityProvider("TestDataMiddle2", "M", "c", "f", "h", "i");
		final ConfigurationDataProvider data4 = identityProvider("test2", "N", "d", "g", "i", "j");
		//
		final ConfigurationDataProvider cfgData = union(data1, data2, data3, data4);
		// When
		final TestDataChild testObject = ConfigurationData.createConfigurationObject(cfgData, TestDataChild.class);
		// Then
		assertThat(testObject.a(), is("aK"));
		assertThat(testObject.b(), is("bL"));
		assertThat(testObject.c(), is("cM"));
		assertThat(testObject.d(), is("dN"));
		assertThat(testObject.e(), is("eL"));
		assertThat(testObject.f(), is("fM"));
		assertThat(testObject.g(), is("gN"));
		assertThat(testObject.h(), is("hM"));
		assertThat(testObject.i(), is("iN"));
		assertThat(testObject.j(), is("jN"));
	}



	private static ConfigurationDataProvider union(final ConfigurationDataProvider... providers) {
		return new ConfigurationDataProvider() {
			@Override
			public boolean containsKey(String key) {
				for (final ConfigurationDataProvider provider : providers) {
					if (provider.containsKey(key)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Object getRawObject(String key) {
				for (final ConfigurationDataProvider provider : providers) {
					if (provider.containsKey(key)) {
						return provider.getRawObject(key);
					}
				}
				return null;
			}
		};
	}

	private static ConfigurationDataProvider identityProvider(String prefix, String suffix, final String... keys) {
		final String keyPrefix;
		if ((null == prefix) || prefix.trim().isEmpty()) {
			keyPrefix = "";
		} else {
			keyPrefix = prefix + '.';
		}
		final String valueSuffix;
		if ((null == suffix) || suffix.trim().isEmpty()) {
			valueSuffix = "";
		} else {
			valueSuffix = suffix;
		}
		return new ConfigurationDataProvider() {
			@Override
			public boolean containsKey(String key) {
				for (final String k : keys) {
					if (key.equals(keyPrefix + k)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Object getRawObject(String key) {
				for (final String k : keys) {
					if (key.equals(keyPrefix + k)) {
						return k + valueSuffix;
					}
				}
				return null;
			}
		};
	}

}
