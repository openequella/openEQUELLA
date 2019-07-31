package com.tle.web.api.item.tasks.interfaces.beans;

import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.interfaces.I18NString;

public class TaskBean {
  private String uuid;
  private I18NString name;
  private I18NString description;
  private int priority;
  private boolean unanimous;
  private BaseEntityReference workflow;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public boolean isUnanimous() {
    return unanimous;
  }

  public void setUnanimous(boolean unanimous) {
    this.unanimous = unanimous;
  }

  public I18NString getName() {
    return name;
  }

  public void setName(I18NString name) {
    this.name = name;
  }

  public I18NString getDescription() {
    return description;
  }

  public void setDescription(I18NString description) {
    this.description = description;
  }

  public BaseEntityReference getWorkflow() {
    return workflow;
  }

  public void setWorkflow(BaseEntityReference workflow) {
    this.workflow = workflow;
  }
}
