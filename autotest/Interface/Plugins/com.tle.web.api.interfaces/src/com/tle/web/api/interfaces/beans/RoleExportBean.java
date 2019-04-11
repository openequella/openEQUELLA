package com.tle.web.api.interfaces.beans;

public class RoleExportBean {
  @SuppressWarnings("nls")
  private String exportVersion = "1.0";

  public String getExportVersion() {
    return exportVersion;
  }

  public void setExportVersion(String exportVersion) {
    this.exportVersion = exportVersion;
  }
}
