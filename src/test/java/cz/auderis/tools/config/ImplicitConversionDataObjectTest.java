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
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.awt.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@code ImplicitConversionDataObjectTest}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class ImplicitConversionDataObjectTest {

	public interface TestDataObject {
		Color getColor();
		MessageFormat getFormat();
		SimpleDateFormat getDateFormat();
		Locale userLocale();
	}


	@Test
	@Category(UnitTest.class)
	public void shouldImplicitlyConvertToColor() throws Exception {
		final Object[][] dataPoints = {
				{ 0x00FF00, Color.GREEN },
				{ 0xFFFFFF, Color.WHITE },
				{ "0", Color.BLACK },
				{ "255", Color.BLUE },
		};
		for (Object[] dataPoint : dataPoints) {
			final Map<String, Object> dataSource = ImmutableMap.of("color", dataPoint[0]);
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);
			final TestDataObject testObject = ConfigurationData.createConfigurationObject(data, TestDataObject.class);
			assertEquals(dataPoint[1], testObject.getColor());
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldImplicitlyConvertToMsgFormat() throws Exception {
		final Object[][] dataPoints = {
				{ "test:{0}", "test:X" },
				{ "({0})-[{0}]", "(X)-[X]" },
		};
		for (Object[] dataPoint : dataPoints) {
			final Map<String, Object> dataSource = ImmutableMap.of("format", dataPoint[0]);
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);
			final TestDataObject testObject = ConfigurationData.createConfigurationObject(data, TestDataObject.class);
			final MessageFormat fmt = testObject.getFormat();
			assertNotNull(fmt);
			final String fmtResult = fmt.format(new String[]{"X"}, new StringBuffer(), null).toString();
			assertEquals(dataPoint[1], fmtResult);
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldImplicitlyConvertToDateFmt() throws Exception {
		final Object[][] dataPoints = {
				{ "yyyy-MM-dd", "2000-02-29", new Date(100, 1, 29) },
				{ "MM/dd/yy", "05/20/11", new Date(111, 4, 20) },
		};
		for (Object[] dataPoint : dataPoints) {
			final Map<String, Object> dataSource = ImmutableMap.of("dateFormat", dataPoint[0]);
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);
			final TestDataObject testObject = ConfigurationData.createConfigurationObject(data, TestDataObject.class);
			final DateFormat dateFmt = testObject.getDateFormat();
			assertNotNull(dateFmt);
			final String textDate = (String) dataPoint[1];
			final Date realDate = (Date) dataPoint[2];
			assertEquals(realDate, dateFmt.parse(textDate));
			assertEquals(textDate, dateFmt.format(realDate));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldImplicitlyConvertToLocale() throws Exception {
		final Object[][] dataPoints = {
				{ "en", Locale.ENGLISH },
				{ "cs", Locale.forLanguageTag("cs") },
		};
		for (Object[] dataPoint : dataPoints) {
			final Map<String, Object> dataSource = ImmutableMap.of("userLocale", dataPoint[0]);
			final ConfigurationDataProvider data = ConfigurationData.getMapDataProvider(dataSource);
			final TestDataObject testObject = ConfigurationData.createConfigurationObject(data, TestDataObject.class);
			assertEquals(dataPoint[1], testObject.userLocale());
		}
	}

}
