package com.tle.web.api.schema.interfaces.beans;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SchemaBean extends BaseEntityBean {
  private String namePath;
  private String descriptionPath;
  private Map<String, SchemaNodeBean> definition;

  public String getNamePath() {
    return namePath;
  }

  public void setNamePath(String namePath) {
    this.namePath = namePath;
  }

  public String getDescriptionPath() {
    return descriptionPath;
  }

  public void setDescriptionPath(String descriptionPath) {
    this.descriptionPath = descriptionPath;
  }

  public Map<String, SchemaNodeBean> getDefinition() {
    return definition;
  }

  public void setDefinition(Map<String, SchemaNodeBean> definition) {
    this.definition = definition;
  }
}
