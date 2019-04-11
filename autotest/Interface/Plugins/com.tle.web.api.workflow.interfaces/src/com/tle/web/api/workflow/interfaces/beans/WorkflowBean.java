package com.tle.web.api.workflow.interfaces.beans;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkflowBean extends BaseEntityBean {
  private WorkflowNodeBean root;
  private boolean moveLive;

  public WorkflowNodeBean getRoot() {
    return root;
  }

  public void setRoot(WorkflowNodeBean root) {
    this.root = root;
  }

  public boolean isMoveLive() {
    return moveLive;
  }

  public void setMoveLive(boolean moveLive) {
    this.moveLive = moveLive;
  }
}
