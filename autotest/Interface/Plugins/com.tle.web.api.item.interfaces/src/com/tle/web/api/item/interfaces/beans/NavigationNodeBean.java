package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/** @author Aaron */
@XmlRootElement
public class NavigationNodeBean extends AbstractExtendableBean {
  private String uuid;
  private String name;
  private String icon;
  private String imsId;
  private List<NavigationTabBean> tabs;
  private List<NavigationNodeBean> nodes;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getImsId() {
    return imsId;
  }

  public void setImsId(String imsId) {
    this.imsId = imsId;
  }

  public List<NavigationTabBean> getTabs() {
    return tabs;
  }

  public void setTabs(List<NavigationTabBean> tabs) {
    this.tabs = tabs;
  }

  public List<NavigationNodeBean> getNodes() {
    return nodes;
  }

  public void setNodes(List<NavigationNodeBean> nodes) {
    this.nodes = nodes;
  }
}
