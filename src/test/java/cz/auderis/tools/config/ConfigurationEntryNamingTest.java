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

}
