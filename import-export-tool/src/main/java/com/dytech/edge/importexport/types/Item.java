package com.dytech.edge.importexport.types;

import com.dytech.devlib.PropBagEx;
import java.io.File;

public class Item {
  private String name;
  private String uuid;
  private int version;
  private final String collectionUuid;
  private String ownerId;

  private File xmlFile;
  private File attachmentsFolder;
  private PropBagEx xml;

  // public Item()
  // {
  // }

  public Item(String name, String uuid, int version, String collectionUuid, String ownerId) {
    this.name = name;
    this.uuid = uuid;
    this.version = version;
    this.collectionUuid = collectionUuid;
    this.ownerId = ownerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public File getXmlFile() {
    return xmlFile;
  }

  public void setXmlFile(File path) {
    this.xmlFile = path;
  }

  public File getAttachmentsFolder() {
    return attachmentsFolder;
  }

  public void setAttachmentsFolder(File attachmentsFolder) {
    this.attachmentsFolder = attachmentsFolder;
  }

  public String getCollectionUuid() {
    return collectionUuid;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public PropBagEx getXml() {
    return xml;
  }

  public void setXml(PropBagEx xml) {
    this.xml = xml;
  }
}
