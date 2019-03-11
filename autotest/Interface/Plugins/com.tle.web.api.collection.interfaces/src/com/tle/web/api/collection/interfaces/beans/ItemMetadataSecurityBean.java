package com.tle.web.api.collection.interfaces.beans;

import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;
import java.util.List;

public class ItemMetadataSecurityBean {
  private String name;
  private String script;
  private List<TargetListEntryBean> entries;

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public List<TargetListEntryBean> getEntries() {
    return entries;
  }

  public void setEntries(List<TargetListEntryBean> entries) {
    this.entries = entries;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
