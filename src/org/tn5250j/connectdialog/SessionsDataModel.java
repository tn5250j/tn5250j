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
