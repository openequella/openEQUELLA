package com.dytech.edge.cache;

public class ItemId {
  private String uuid;
  private int version;

  public ItemId(String uuid, int version) {
    this.uuid = uuid;
    this.version = version;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return uuid + '/' + version;
  }
}
