package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.UserBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ItemStatusBean {
  private String status;
  private String rejectedMessage;
  private UserBean rejectedBy;
  private ItemNodeStatusBean nodes;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public ItemNodeStatusBean getNodes() {
    return nodes;
  }

  public void setNodes(ItemNodeStatusBean nodes) {
    this.nodes = nodes;
  }

  public UserBean getRejectedBy() {
    return rejectedBy;
  }

  public void setRejectedBy(UserBean rejectedBy) {
    this.rejectedBy = rejectedBy;
  }

  public String getRejectedMessage() {
    return rejectedMessage;
  }

  public void setRejectedMessage(String rejectedMessage) {
    this.rejectedMessage = rejectedMessage;
  }
}
