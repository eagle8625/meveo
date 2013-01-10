/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.manaty.utils;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * String utils class tests.
 * 
 * @author Ignas
 */
public class StringUtilsTest {
	
	@Test(groups = { "unit" })
    public void testIsArrayContainsStringWhenTrue() {
		String value = "AAA";
		String[] stringArray = {"BBB", "CCC", "AAA"};
		Assert.assertTrue(StringUtils.isArrayContainingString(value, stringArray));
	}
	
	@Test(groups = { "unit" })
    public void testIsArrayContainsStringWhenFalse() {
		String value = "DDD";
		String[] stringArray = {"BBB", "CCC", "AAA"};
		Assert.assertFalse(StringUtils.isArrayContainingString(value, stringArray));

	}

}