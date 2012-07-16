/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
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
package org.tn5250j.encoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author master_jaf
 */
public class JavaCodePageTest {

	/**
	 * Test method for {@link org.tn5250j.encoding.JavaCodePageFactory#ebcdic2uni(int)}.
	 */
	@Test
	public void testEbcdic2uni() {
		ICodePage jcp = JavaCodePageFactory.getCodePage("ASCII");
		assertNotNull("At least an ASCII Codepage should be available.", jcp);
		
		char actual = jcp.ebcdic2uni(97);
		assertEquals("simple test for character 'a'", 'a', actual);
	}

	/**
	 * Test method for {@link org.tn5250j.encoding.JavaCodePageFactory#uni2ebcdic(char)}.
	 */
	@Test
	public void testUni2ebcdic() {
		ICodePage jcp = JavaCodePageFactory.getCodePage("ASCII");
		assertNotNull("At least an ASCII Codepage should be available.", jcp);
		
		byte actual = jcp.uni2ebcdic('a');
		assertEquals("simple test for character 'a' = bytecode 97", 97, actual);
	}

	/**
	 * Test for a not existing codepage
	 */
	@Test
	public void testNotExistingCodePage() {
		ICodePage jcp = JavaCodePageFactory.getCodePage("FOOBAR");
		assertNull("There should be no such Codepage available", jcp);
	}		
}
