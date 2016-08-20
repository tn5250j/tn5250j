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

/**
 * Simple data model representing rows within the {@link SessionsTableModel}.
 */
class SessionsDataModel {
  final String name;
  final String host;
  final Boolean deflt;

  SessionsDataModel(String name, String host, Boolean deflt) {
    this.name = name;
    this.host = host;
    this.deflt = deflt;
  }
}
