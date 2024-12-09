package com.tle.webtests.pageobject;

public class ClassPrefixedName implements PrefixedName {
  protected final String name;
  protected final Class<?> c;

  public ClassPrefixedName(Class<?> c, String name) {
    this.name = name;
    this.c = c;
  }

  @Override
  public String toString() {
    return c.getSimpleName() + " - " + name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof String) {
      return toString().equals(obj);
    }
    if (obj instanceof PrefixedName) {
      return toString().equals(obj.toString());
    }
    return false;
  }
}
