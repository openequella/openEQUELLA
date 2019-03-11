package com.tle.webtests.pageobject;

/**
 * Forces people to use prefixed names This is for cloned entities where the name ends up as "Copy
 * of XXXX"
 *
 * @author Aaron
 */
public class ClonedPrefixedName extends ClassPrefixedName {
  private final int cloneTimes;

  public ClonedPrefixedName(ClassPrefixedName copyOf) {
    this(copyOf, 1);
  }

  public ClonedPrefixedName(ClassPrefixedName copyOf, int cloneTimes) {
    super(copyOf.c, copyOf.name);
    this.cloneTimes = cloneTimes;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cloneTimes; i++) {
      sb.append("Copy of ");
    }
    sb.append(super.toString());
    return sb.toString();
  }
}
