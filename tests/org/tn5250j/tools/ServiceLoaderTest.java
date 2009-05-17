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
package org.tn5250j.tools;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for {@link ServiceLoader}.
 * 
 * @author maki
 */
public class ServiceLoaderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLoad() {
		ServiceLoader<org.tn5250j.tools.IEmptyTestBean> sl = ServiceLoader.load(org.tn5250j.tools.IEmptyTestBean.class);
		Iterator<IEmptyTestBean> beaniterator = sl.iterator();
		assertTrue("There should be at least one class available", beaniterator.hasNext());		
		while (beaniterator.hasNext()) {
			IEmptyTestBean bean = beaniterator.next();
			assertNotNull("Of course nulls are not allowed!", bean);
			assertNotNull("Of course nulls are not allowed!", bean.getName());
		}
	}

}
