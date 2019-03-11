package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.UserBean;

public class ItemLockBean extends AbstractExtendableBean {
  private String uuid;
  private UserBean owner;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public UserBean getOwner() {
    return owner;
  }

  public void setOwner(UserBean owner) {
    this.owner = owner;
  }
}
