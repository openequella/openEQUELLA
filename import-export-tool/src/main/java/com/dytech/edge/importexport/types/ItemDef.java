package com.dytech.edge.importexport.types;

import com.dytech.devlib.PropBagEx;
import java.io.File;

public class ItemDef {
  private final String name;
  private final String uuid;
  private final boolean system;

  private File path;
  private File template;

  public ItemDef(PropBagEx xml) {
    uuid = xml.getNode("uuid"); // $NON-NLS-1$
    name = xml.getNode("name"); // $NON-NLS-1$
    system = xml.isNodeTrue("system"); // $NON-NLS-1$
  }

  public File getPath() {
    return path;
  }

  public void setPath(File path) {
    this.path = path;
  }

  public File getTemplate() {
    return template;
  }

  public void setTemplate(File template) {
    this.template = template;
  }

  public String getName() {
    return name;
  }

  public String getUuid() {
    return uuid;
  }

  public boolean isSystem() {
    return system;
  }

  @Override
  public String toString() {
    return name;
  }
}
