package com.tle.web.api.interfaces.beans.security;

import com.tle.web.api.interfaces.beans.RestBean;
import java.util.List;

public class BaseEntitySecurityBean implements RestBean {
  private List<TargetListEntryBean> rules;

  public List<TargetListEntryBean> getRules() {
    return rules;
  }

  public void setRules(List<TargetListEntryBean> rules) {
    this.rules = rules;
  }
}
