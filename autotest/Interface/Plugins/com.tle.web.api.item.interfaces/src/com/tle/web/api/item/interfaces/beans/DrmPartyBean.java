package com.tle.web.api.item.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

/** @author Aaron */
@XmlRootElement
public class DrmPartyBean {
  private String userId;
  private String name;
  private String email;
  private boolean owner;

  public DrmPartyBean() {}

  public DrmPartyBean(String userId, String name, String email, boolean owner) {
    this.userId = userId;
    this.name = name;
    this.email = email;
    this.owner = owner;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isOwner() {
    return owner;
  }

  public void setOwner(boolean owner) {
    this.owner = owner;
  }
}
