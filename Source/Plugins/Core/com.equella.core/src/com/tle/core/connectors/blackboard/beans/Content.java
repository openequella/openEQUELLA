package com.tle.core.connectors.blackboard.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Content {
  private String id;
  private String parentId;
  private String title;
  private String description;
  private String created;
  private Integer position;
  private Boolean hasChildren;
  private Availability availability;
  private ContentHandler contentHandler;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public Boolean getHasChildren() {
    return hasChildren;
  }

  public void setHasChildren(Boolean hasChildren) {
    this.hasChildren = hasChildren;
  }

  public Availability getAvailability() {
    return availability;
  }

  public void setAvailability(Availability availability) {
    this.availability = availability;
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void setContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  @XmlRootElement
  public static class ContentHandler {
    public static final String RESOURCE_FOLDER = "resource/x-bb-folder";
    public static final String RESOURCE_LTI_LINK = "resource/x-bb-blti-link";
    private String id; // resource/x-bb-folder
    private String url;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }
}
