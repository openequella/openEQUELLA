package com.tle.web.api.item.interfaces.beans;

import com.tle.common.interfaces.UuidReference;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import javax.xml.bind.annotation.XmlRootElement;

/** @author Aaron */
@XmlRootElement
public class NavigationTabBean extends AbstractExtendableBean {
  private String name;
  private UuidReference attachment;
  private String viewer;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UuidReference getAttachment() {
    return attachment;
  }

  public void setAttachment(UuidReference attachment) {
    this.attachment = attachment;
  }

  public String getViewer() {
    return viewer;
  }

  public void setViewer(String viewer) {
    this.viewer = viewer;
  }
}
