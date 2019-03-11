package com.tle.web.api.interfaces.beans;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

/** @author Aaron */
public abstract class AbstractExtendableBean implements RestBean {
  private final Map<String, Object> extras = new HashMap<String, Object>();

  public Object get(String name) {
    return extras.get(name);
  }

  @JsonAnyGetter
  public Map<String, Object> any() {
    return extras;
  }

  @JsonAnySetter
  public void set(String key, Object value) {
    extras.put(key, value);
  }
}
