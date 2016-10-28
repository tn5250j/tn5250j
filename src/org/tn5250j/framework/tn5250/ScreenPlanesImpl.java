/*
 * Title: ScreenPlanes.java
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j.framework.tn5250;

import org.tn5250j.TN5250jConstants;

public class ScreenPlanesImpl implements org.tn5250j.api.ScreenPlanes {

  protected char[] textPlane;
  protected char[] attributePlane;
  protected char[] isAttributePlane;
  protected char[] colorPlane;
  protected char[] extendedPlane;
  protected char[] graphicPlane;
  protected char[] fieldPlane;

  public ScreenPlanesImpl(char[] textPlane, char[] attributePlane, char[] isAttributePlane, char[] colorPlane, char[] extendedPlane, char[] graphicPlane, char[] fieldPlane) {
    this.textPlane = textPlane;
    this.attributePlane = attributePlane;
    this.isAttributePlane = isAttributePlane;
    this.colorPlane = colorPlane;
    this.extendedPlane = extendedPlane;
    this.graphicPlane = graphicPlane;
    this.fieldPlane = fieldPlane;
  }

  public ScreenPlanesImpl(Screen5250 screen, int startRow, int startCol, int endRow, int endCol) {
    startRow++;
    startCol++;
    endRow++;
    endCol++;
    int size = ((endCol - startCol) + 1) * ((endRow - startRow) + 1);

    textPlane = new char[size];
    attributePlane = new char[size];
    isAttributePlane = new char[size];
    colorPlane = new char[size];
    extendedPlane = new char[size];
    graphicPlane = new char[size];
    fieldPlane = new char[size];

    if (size == screen.getScreenLength()) {
      screen.getScreen(textPlane, size, TN5250jConstants.PLANE_TEXT);
      screen.getScreen(attributePlane, size, TN5250jConstants.PLANE_ATTR);
      screen.getScreen(isAttributePlane, size, TN5250jConstants.PLANE_IS_ATTR_PLACE);
      screen.getScreen(colorPlane, size, TN5250jConstants.PLANE_COLOR);
      screen.getScreen(extendedPlane, size, TN5250jConstants.PLANE_EXTENDED);
      screen.getScreen(graphicPlane, size, TN5250jConstants.PLANE_EXTENDED_GRAPHIC);
      screen.getScreen(fieldPlane, size, TN5250jConstants.PLANE_FIELD);
    } else {
      screen.getScreenRect(textPlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_TEXT);
      screen.getScreenRect(attributePlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_ATTR);
      screen.getScreenRect(isAttributePlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_IS_ATTR_PLACE);
      screen.getScreenRect(colorPlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_COLOR);
      screen.getScreenRect(extendedPlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_EXTENDED);
      screen.getScreenRect(graphicPlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_EXTENDED_GRAPHIC);
      screen.getScreenRect(fieldPlane, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_FIELD);
    }
  }

  @Override
  public char[] getTextPlane() {
    return textPlane;
  }

  @Override
  public char[] getAttributePlane() {
    return attributePlane;
  }

  @Override
  public char[] getIsAttributePlane() {
    return isAttributePlane;
  }

  @Override
  public char[] getColorPlane() {
    return colorPlane;
  }

  @Override
  public char[] getExtendedPlane() {
    return extendedPlane;
  }

  @Override
  public char[] getGraphicPlane() {
    return graphicPlane;
  }

  @Override
  public char[] getFieldPlane() {
    return fieldPlane;
  }
}
