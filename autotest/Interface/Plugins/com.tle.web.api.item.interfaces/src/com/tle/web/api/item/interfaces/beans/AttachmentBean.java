package com.tle.web.api.item.interfaces.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class AttachmentBean extends AbstractExtendableBean {
  private String uuid;
  private String description;
  private String viewer;
  private boolean preview;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getViewer() {
    return viewer;
  }

  public void setViewer(String viewer) {
    this.viewer = viewer;
  }

  @JsonIgnore
  public abstract String getRawAttachmentType();

  public boolean isPreview() {
    return preview;
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
  }
}
