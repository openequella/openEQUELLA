package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RelationBean extends AbstractExtendableBean {
  private Long id;
  private String relation;
  private ItemBean from;
  private ItemBean to;
  private String fromResource;
  private String toResource;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public ItemBean getFrom() {
    return from;
  }

  public void setFrom(ItemBean from) {
    this.from = from;
  }

  public ItemBean getTo() {
    return to;
  }

  public void setTo(ItemBean to) {
    this.to = to;
  }

  public String getFromResource() {
    return fromResource;
  }

  public void setFromResource(String fromResource) {
    this.fromResource = fromResource;
  }

  public String getToResource() {
    return toResource;
  }

  public void setToResource(String toResource) {
    this.toResource = toResource;
  }
}
