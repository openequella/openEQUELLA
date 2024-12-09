package com.dytech.edge.cache;

import java.io.Serializable;
import java.util.StringTokenizer;

public class ItemKey implements Serializable {
  private static final long serialVersionUID = 1L;
  private String uuid;
  private int version;
  private String itemdef;

  public ItemKey() {
    super();
  }

  public ItemKey(String uuid, int version) {
    setUuid(uuid);
    setVersion(version);
  }

  public ItemKey(String uuid, int version, String itemdef) {
    this(uuid, version);
    setItemdef(itemdef);
  }

  /**
   * @param value <quot>item_uuid:itemdef:item_version</quot>.
   */
  public ItemKey(String value) {
    setFromString(value);
  }

  /**
   * @param value <quot>item_uuid:itemdef:item_version</quot>.
   */
  public void setFromString(String value) {
    final StringTokenizer stok = new StringTokenizer(value, ":");
    uuid = stok.nextToken();
    itemdef = stok.nextToken();
    version = Integer.parseInt(stok.nextToken());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    ItemKey rhs = null;
    if (obj instanceof ItemKey) {
      rhs = (ItemKey) obj;
    } else if (obj instanceof String) {
      try {
        rhs = new ItemKey((String) obj);
      } catch (final Exception ex) {
        return false;
      }
    } else {
      return false;
    }

    return rhs.uuid.equals(uuid) && rhs.itemdef.equals(itemdef) && rhs.version == version;
  }

  @Override
  public String toString() {
    return uuid + ':' + itemdef + ':' + version;
  }

  /**
   * Returns a string of format: uuid/version Note that this is different to the toString() method
   *
   * @return
   */
  public String getKey() {
    return uuid + '/' + version;
  }

  /**
   * Since the constructor expects a string of format uuid:collectionuuid:version, this method
   * exists to create an ItemKey from a string of format uuid/version Note that for: ItemKey newKey
   * = ItemKey.generateKey(originalKey.getKey()); then it is true that: newKey.equals(originalKey);
   *
   * @param value Item key in format: uuid/version
   * @return
   */
  public static ItemKey generateKey(String value) {
    final StringTokenizer stok = new StringTokenizer(value, "/"); // $NON-NLS-1$
    final String uuid = stok.nextToken();
    final int version = Integer.parseInt(stok.nextToken());
    return new ItemKey(uuid, version);
  }

  @Override
  public int hashCode() {
    return (uuid + itemdef + version).hashCode();
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    if (uuid == null) {
      throw new IllegalArgumentException("UUID must not be null");
    }
    this.uuid = uuid;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Version must be greater than or equals to zero");
    }
    this.version = version;
  }

  public String getItemdef() {
    return itemdef;
  }

  public void setItemdef(String itemdef) {
    if (itemdef == null) {
      throw new IllegalArgumentException("Itemdef must not be null");
    }
    this.itemdef = itemdef;
  }
}
