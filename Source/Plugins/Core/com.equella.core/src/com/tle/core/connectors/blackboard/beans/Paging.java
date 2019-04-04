package com.tle.core.connectors.blackboard.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Paging {
  private String nextPage;

  public String getNextPage() {
    return nextPage;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }
}
