package com.tle.web.api.schema.interfaces.beans;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// this breaks in scala for some reason...
// @JsonInclude(NON_DEFAULT)
public class SchemaNodeBean {
  @JsonIgnore
  private Map<String, SchemaNodeBean> children = new LinkedHashMap<String, SchemaNodeBean>();

  @JsonProperty("_indexed")
  @JsonInclude(Include.NON_DEFAULT)
  private boolean indexed;

  @JsonProperty("_field")
  @JsonInclude(Include.NON_DEFAULT)
  private boolean field;

  @JsonProperty("_nested")
  @JsonInclude(Include.NON_DEFAULT)
  private boolean nested;

  @JsonProperty("_type")
  @JsonInclude(Include.NON_DEFAULT)
  private String type;

  @JsonAnyGetter
  public Map<String, SchemaNodeBean> getChildren() {
    return children;
  }

  public SchemaNodeBean getChild(String name) {
    return children.get(name);
  }

  @JsonAnySetter
  public void addChild(String key, SchemaNodeBean child) {
    children.put(key, child);
  }

  public boolean isIndexed() {
    return indexed;
  }

  public void setIndexed(boolean indexed) {
    this.indexed = indexed;
  }

  public boolean isField() {
    return field;
  }

  public void setField(boolean field) {
    this.field = field;
  }

  public boolean isNested() {
    return nested;
  }

  public void setNested(boolean nested) {
    this.nested = nested;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void removeChild(String name) {
    children.remove(name);
  }

  public boolean editChildOrder(Iterable<String> childOrders) {
    boolean edited = false;
    Iterator<String> names = children.keySet().iterator();
    for (String childName : childOrders) {
      if (!names.hasNext()) {
        break;
      }
      if (!names.next().equals(childName)) {
        edited = true;
        break;
      }
    }
    edited |= names.hasNext();
    if (edited) {
      LinkedHashMap<String, SchemaNodeBean> newChildren =
          new LinkedHashMap<String, SchemaNodeBean>();
      for (String child : childOrders) {
        newChildren.put(child, children.get(child));
      }
      children = newChildren;
    }
    return edited;
  }

  public void addAll(Map<String, SchemaNodeBean> children) {
    this.children.putAll(children);
  }
}
