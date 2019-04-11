package com.tle.webtests.test.webservices.rest;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONEntity extends EntityTemplate {

  public JSONEntity(ObjectMapper mapper, JsonNode jsonNode) {
    super(new JSONEntityProducer(mapper, jsonNode));
    setContentType("application/json");
  }

  public static class JSONEntityProducer implements ContentProducer {
    private final JsonNode jsonNode;
    private final ObjectMapper mapper;

    public JSONEntityProducer(ObjectMapper mapper, JsonNode jsonNode) {
      this.mapper = mapper;
      this.jsonNode = jsonNode;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
      mapper.writeValue(out, jsonNode);
    }
  }
}
