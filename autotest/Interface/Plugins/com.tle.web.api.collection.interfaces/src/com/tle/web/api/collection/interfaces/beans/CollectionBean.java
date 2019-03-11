package com.tle.web.api.collection.interfaces.beans;

import com.tle.common.interfaces.BaseEntityReference;
import com.tle.web.api.interfaces.beans.BaseEntityBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CollectionBean extends BaseEntityBean {
  private BaseEntityReference schema;
  private BaseEntityReference workflow;
  private Integer reviewPeriod;
  private CollectionSecurityBean security;
  private String filestoreId;

  public BaseEntityReference getSchema() {
    return schema;
  }

  public void setSchema(BaseEntityReference schema) {
    this.schema = schema;
  }

  public BaseEntityReference getWorkflow() {
    return workflow;
  }

  public void setWorkflow(BaseEntityReference workflow) {
    this.workflow = workflow;
  }

  @Override
  public CollectionSecurityBean getSecurity() {
    return security;
  }

  public void setSecurity(CollectionSecurityBean security) {
    this.security = security;
  }

  public Integer getReviewPeriod() {
    return reviewPeriod;
  }

  public void setReviewPeriod(Integer reviewPeriod) {
    this.reviewPeriod = reviewPeriod;
  }

  public String getFilestoreId() {
    return filestoreId;
  }

  public void setFilestoreId(String filestoreId) {
    this.filestoreId = filestoreId;
  }
}
