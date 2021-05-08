/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2018
 * Company:
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.tools.logging;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author SaschaS93
 */
public class TN5250jLogFactoryTest {

    private static final String LOGGER_MOCK_CLASS_NAME = "org.tn5250j.tools.logging.CustomTN5250jLoggerMock";

    @Test
    public void testCustomLogger() {
        System.setProperty(TN5250jLogFactory.class.getName(), LOGGER_MOCK_CLASS_NAME);
        TN5250jLogger logger = TN5250jLogFactory.getLogger(this.getClass());

        String msg = "The loaded logger must be an instance of " + LOGGER_MOCK_CLASS_NAME;
        assertTrue(msg, logger instanceof CustomTN5250jLoggerMock);
    }

}
