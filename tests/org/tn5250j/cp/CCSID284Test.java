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
package org.tn5250j.cp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.tn5250j.encoding.CharMappings;
import org.tn5250j.encoding.CodePage;

/**
 * Testing the correctness of {@link CCSID284Ex} and comparing with existing implementation.
 * 
 * @author master_jaf
 */
public class CCSID284Test {

	private char[] TESTSTRING = new char[255];	
	
	@Before
	public void setUp() {
		for (int i=1; i<=255; i++) {
			TESTSTRING[i-1] = (char) i;
		}			
	}
	
	/**
	 * Correctness test for old implementation ....
	 */
	@Test
	public void testOldConverter284() {
		
		CodePage cp = CharMappings.getCodePage("284");
		assertNotNull("At least an ASCII Codepage should be available.", cp);
		
		for (int i=0; i<TESTSTRING.length; i++) {
			final char beginvalue = TESTSTRING[i];
			final byte converted = cp.uni2ebcdic(beginvalue);
			final char afterall = cp.ebcdic2uni(converted & 0xFF);
			assertEquals("Testing item #" + i, beginvalue, afterall);
		}
		
	}

	/**
	 * Correctness test for new implementation ...
	 */
	@Test
	public void testNewConverter284() {
		CCSID284 cp = new CCSID284();
		cp.init();
		assertNotNull("At least an ASCII Codepage should be available.", cp);
		
		for (int i=0; i<TESTSTRING.length; i++) {
			final char beginvalue = TESTSTRING[i];
			final byte converted = cp.uni2ebcdic(beginvalue);
			final char afterall = cp.ebcdic2uni(converted & 0xFF);
			assertEquals("Testing item #" + i, beginvalue, afterall);
		}
	}

	/**
	 * Testing for Correctness both implementations ...
	 */
	@Test
	public void testBoth() {
		final CodePage cp = CharMappings.getCodePage("284");
		final CCSID284 cpex = new CCSID284();
		cpex.init();
		assertNotNull("At least an ASCII Codepage should be available.", cpex);
		
		for (int i=0; i<TESTSTRING.length; i++) {
			
			final char beginvalue = TESTSTRING[i];
			assertEquals("Testing to EBCDIC item #" + i, cp.uni2ebcdic(beginvalue), cpex.uni2ebcdic(beginvalue));
			final byte converted = cp.uni2ebcdic(beginvalue);
			assertEquals("Testing to UNICODE item #" + i, cp.ebcdic2uni(converted & 0xFF), cpex.ebcdic2uni(converted & 0xFF));
			final char afterall = cp.ebcdic2uni(converted & 0xFF);
			assertEquals("Testing before and after item #" + i, beginvalue, afterall);
		}
	}
	
}
