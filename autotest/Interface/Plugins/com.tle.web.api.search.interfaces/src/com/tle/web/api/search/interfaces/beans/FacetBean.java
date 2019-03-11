package com.tle.web.api.search.interfaces.beans;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FacetBean extends AbstractExtendableBean {
  private String term;
  private int count;
  private List<FacetBean> innerFacets;

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public List<FacetBean> getInnerFacets() {
    return innerFacets;
  }

  public void setInnerFacet(List<FacetBean> innerFacet) {
    this.innerFacets = innerFacet;
  }
}
