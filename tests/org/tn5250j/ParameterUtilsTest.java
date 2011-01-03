/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: MKirst
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.tn5250j.gui.model.EmulSessionProfile;
import org.tn5250j.interfaces.ConfigureFactory;

/**
 */
public class ParameterUtilsTest {

	private List<String> testStrings = new ArrayList<String>();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Properties sessionProperties = ConfigureFactory.getInstance().getProperties(ConfigureFactory.SESSIONS);

		// load every session into a list
		for (Object key : sessionProperties.keySet()) {
			String k = key.toString();
			if (!k.startsWith("emul.")) {
				final String property = sessionProperties.getProperty(k);
				testStrings.add(property);
			}
		}
	}

	/**
	 * Test method for {@link org.tn5250j.ParameterUtils#loadSessionFromArguments(java.lang.String)}.
	 */
	@Test
	public void testLoadAndSaveSessionFromArguments() {
		for (String s : testStrings) {
			final EmulSessionProfile expected = ParameterUtils.loadSessionFromArguments(s);
			final String temp = ParameterUtils.safeEmulSessionToString(expected);
			final EmulSessionProfile actual = ParameterUtils.loadSessionFromArguments(temp);
			assertEquals("TEST: " + temp, expected, actual);
			System.out.println(temp);
		}
	}

}
