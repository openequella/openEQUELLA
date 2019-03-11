package com.tle.web.api.interfaces.beans.security;

import java.util.List;

public class DynamicRuleBean {
  private String name;
  private String path;
  private String type;
  private List<TargetListEntryBean> targetList;

  public String getName() {
    return name;
  }

  public DynamicRuleBean setName(String name) {
    this.name = name;
    return this;
  }

  public String getPath() {
    return path;
  }

  public DynamicRuleBean setPath(String path) {
    this.path = path;
    return this;
  }

  public String getType() {
    return type;
  }

  public DynamicRuleBean setType(String type) {
    this.type = type;
    return this;
  }

  public List<TargetListEntryBean> getTargetList() {
    return targetList;
  }

  public DynamicRuleBean setTargetList(List<TargetListEntryBean> targetList) {
    this.targetList = targetList;
    return this;
  }
}
