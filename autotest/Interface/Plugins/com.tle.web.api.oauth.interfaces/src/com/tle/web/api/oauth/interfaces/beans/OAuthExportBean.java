package com.tle.web.api.oauth.interfaces.beans;

import com.tle.web.api.interfaces.beans.BaseEntityExportBean;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OAuthExportBean extends BaseEntityExportBean {
  private List<OAuthTokenBean> tokens;

  public List<OAuthTokenBean> getTokens() {
    return tokens;
  }

  public void setTokens(List<OAuthTokenBean> tokens) {
    this.tokens = tokens;
  }
}
