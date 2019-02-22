package com.tle.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class BaseJSONCreator {
  protected static final ObjectMapper mapper = new ObjectMapper();

  public static ObjectNode parse(String json) {
    try {
      return (ObjectNode) mapper.readTree(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
