package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.Test;

public class TaxonomyApiTest extends AbstractRestApiTest {

  private final String TAXONOMY_SET_TERM_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/taxonomy/";
  private final String SET_TEMR_TAXONOMY =
      "a8475ae1-0382-a258-71c3-673e4597c3d2/term/5f43a1ff-edd3-44cc-a8df-a808daaf80ea/data";

  @Test
  public void testSetTermWithMultipleDataKeyValue() throws IOException {
    final PutMethod method = new PutMethod(TAXONOMY_SET_TERM_API_ENDPOINT + SET_TEMR_TAXONOMY);
    ObjectNode body = mapper.createObjectNode();
    body.put("data_key_1", "data_value_1");
    body.put("data_key_2", "data_value_2");
    body.put("data_key_n", "data_value_n");
    method.setRequestEntity(new StringRequestEntity(body.toString(), "application/json", "UTF-8"));
    assertEquals(HttpStatus.SC_CREATED, makeClientRequest(method));
  }

  @Test
  public void testSetTermWithSingleDataKeyValue() throws IOException {
    final PutMethod method = new PutMethod(TAXONOMY_SET_TERM_API_ENDPOINT + SET_TEMR_TAXONOMY);
    ObjectNode body = mapper.createObjectNode();
    body.put("single_data_key", "single_data_value");
    method.setRequestEntity(new StringRequestEntity(body.toString(), "application/json", "UTF-8"));
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
  }
}
