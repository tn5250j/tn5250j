package org.tn5250j.tools.filters;

/*
 * @(#)iOhioSession.java
 * Copyright:    Copyright (c) 2001
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

import java.io.*;
import java.util.ArrayList;

public interface OutputFilterInterface {


   public void createFileInstance(String fileName) throws
                              FileNotFoundException;
   public abstract void writeHeader(String fileName, String host,
                                       ArrayList ffd, char decSep);
   public abstract void writeFooter(ArrayList ffd);
   public abstract void parseFields(byte[] cByte, ArrayList ffd,StringBuffer rb);
   public abstract boolean isCustomizable();
   public abstract void setCustomProperties();
}