package com.tle.web.api.collection.interfaces.beans;

import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;
import java.util.List;
import java.util.Map;

public class AllCollectionsSecurityBean extends BaseEntitySecurityBean {
  private Map<String, List<TargetListEntryBean>> statuses;

  public Map<String, List<TargetListEntryBean>> getStatuses() {
    return statuses;
  }

  public void setStatuses(Map<String, List<TargetListEntryBean>> statuses) {
    this.statuses = statuses;
  }
}
