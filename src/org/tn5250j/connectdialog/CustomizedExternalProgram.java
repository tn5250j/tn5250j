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
