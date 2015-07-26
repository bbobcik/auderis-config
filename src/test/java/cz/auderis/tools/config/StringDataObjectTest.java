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
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.math.BigDecimal;
import java.util.Locale;

import static cz.auderis.tools.config.SingleItemDataProvider.itemProvider;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@code StringDataObjectTest}
 *
 * @author Boleslav Bobcik &lt;bbobcik@gmail.com&gt;
 * @version 1.0
 */
public class StringDataObjectTest {

	public interface TextDataObject {

		String normalText();

		String greetings(String name);

		String complexMessage(String name, int count, BigDecimal value);

	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyReturnNormalText() throws Exception {
		final String[] points = {"a", "abc", "abcdef", "", "x\ty\rz\nA\fB\bC"};
		for (String point : points) {
			final ConfigurationDataProvider data = itemProvider("normalText", point);
			final TextDataObject testObject = ConfigurationData.createConfigurationObject(data, TextDataObject.class);
			assertThat(testObject.normalText(), is(point));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyFormatSimpleMessage() throws Exception {
		final String[][] points = {
				{"Hello {0}", "world", "Hello world"},
				{"{0} bye", "Good", "Good bye"},
				{"{0} = {0}", "xyz", "xyz = xyz"},
		};
		for (String[] point : points) {
			final ConfigurationDataProvider data = itemProvider("greetings", point[0]);
			final TextDataObject testObject = ConfigurationData.createConfigurationObject(data, TextDataObject.class);
			final String name = point[1];
			assertThat(testObject.greetings(name), is(point[2]));
		}
	}

	@Test
	@Category(UnitTest.class)
	public void shouldCorrectlyFormatComplexMessage() throws Exception {
		Locale.setDefault(Locale.forLanguageTag("und"));
		final Object[][] points = {
				{"Vehicle {0}: {2,number,##000.00}, {1}x", "Vehicle Mazda: 1143.32, 5x", "Mazda", 5, new BigDecimal("1143.322") },
				{"Vehicle {0}: {2,number,##000.00}, {1}x", "Vehicle Volvo: 084.44, 10x", "Volvo", 10, new BigDecimal("84.438") },
				{"Vehicle {0}: {2,number,##000.00}, {1}x", "Vehicle Ford: null, -5x", "Ford", -5, null },
		};
		for (Object[] point : points) {
			final ConfigurationDataProvider data = itemProvider("complexMessage", point[0]);
			final TextDataObject testObject = ConfigurationData.createConfigurationObject(data, TextDataObject.class);
			final String message = testObject.complexMessage((String) point[2], (Integer) point[3], (BigDecimal) point[4]);
			assertThat(message, is(point[1]));
		}
	}

}
