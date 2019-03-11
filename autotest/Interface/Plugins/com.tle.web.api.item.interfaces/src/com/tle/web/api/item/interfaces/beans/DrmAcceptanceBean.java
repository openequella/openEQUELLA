package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.UserBean;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/** @author Aaron */
@XmlRootElement
public class DrmAcceptanceBean {
  private UserBean user;
  private Date date;

  public DrmAcceptanceBean() {}

  public DrmAcceptanceBean(UserBean user, Date date) {
    this.user = user;
    this.date = date;
  }

  public UserBean getUser() {
    return user;
  }

  public void setUser(UserBean user) {
    this.user = user;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
