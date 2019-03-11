package com.tle.web.api.item.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DrmBean {
  private DrmOptionsBean options;

  public DrmOptionsBean getOptions() {
    return options;
  }

  public void setOptions(DrmOptionsBean options) {
    this.options = options;
  }
}
