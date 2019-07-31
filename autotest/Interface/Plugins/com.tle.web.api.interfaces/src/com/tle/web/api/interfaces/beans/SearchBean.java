package com.tle.web.api.interfaces.beans;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/** @author Aaron */
@XmlRootElement
public class SearchBean<T> extends AbstractExtendableBean {
  private int start;
  private int length;
  private int available;
  private List<T> results;

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getAvailable() {
    return available;
  }

  public void setAvailable(int available) {
    this.available = available;
  }

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }
}
