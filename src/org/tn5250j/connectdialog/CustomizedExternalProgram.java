/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2016
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
package org.tn5250j.connectdialog;

class CustomizedExternalProgram implements Comparable<CustomizedExternalProgram> {
  private final String name;
  private final String wCommand;
  private final String uCommand;

  CustomizedExternalProgram(String name, String wCommand, String uCommand) {
    this.name = name;
    this.wCommand = wCommand;
    this.uCommand = uCommand;
  }

  public String toString() {
    return this.name;
  }


  public String getName() {
    return name;
  }


  String getUCommand() {
    return uCommand;
  }


  String getWCommand() {
    return wCommand;
  }

  @Override
  public int compareTo(CustomizedExternalProgram o) {
    return this.name.compareTo(o.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CustomizedExternalProgram that = (CustomizedExternalProgram) o;

    return name != null ? name.equals(that.name) : that.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
