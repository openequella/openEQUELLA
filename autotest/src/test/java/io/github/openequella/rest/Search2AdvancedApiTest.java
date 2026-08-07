package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** This test focuses on the Advanced search criteria. */
@TestInstitution("fiveo")
public class Search2AdvancedApiTest extends AbstractRestApiTest {
  private final String SEARCH_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/search2/advanced";

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(Search2AdvancedApiTest.class);
    }
    return testConfig;
  }

  private ObjectNode buildControlValue(String[] schemaNodes, String[] values, String queryType) {
    ObjectNode controlValue = mapper.createObjectNode();
    controlValue.put("queryType", queryType);
    controlValue.put("schemaNodes", mapper.valueToTree(schemaNodes));
    controlValue.put("values", mapper.valueToTree(values));

    return controlValue;
  }

  private ObjectNode buildPayLoad(Collection<JsonNode> controlValue) {
    ArrayNode body = mapper.createArrayNode();
    body.addAll(controlValue);

    ObjectNode payload = mapper.createObjectNode();
    payload.put("advancedSearchCriteria", body);

    return payload;
  }

  private int getAvailable(JsonNode result) {
    return result.get("available").asInt();
  }

  @Test(description = "search with tokens extracted from a text")
  public void tokenisedTextTest() throws IOException {
    ObjectNode controlValue =
        buildControlValue(
            new String[] {"/item/controls/editbox"},
            new String[] {"boxing and something"},
            "Tokenised");

    JsonNode result = doSearch(Collections.singleton(controlValue));
    // There should be two Items in the result. One Item matches the token 'box' and the other one
    // matches the token 'something'.

    assertEquals(getAvailable(result), 2);
  }

  @Test(description = "search with a control which targets to multiple schema nodes")
  public void multipleSchemaNodesTest() throws IOException {
    ObjectNode controlValue =
        buildControlValue(
            new String[] {"/item/controls/editbox", "/item/name"},
            new String[] {"single box"},
            "Tokenised");

    JsonNode result = doSearch((Collections.singleton(controlValue)));
    // There should be two Items in the result. One Item matches the token 'box' and the other one
    // matches the token 'something'.

    assertEquals(getAvailable(result), 2);
  }

  @Test(description = "search with multiple controls")
  public void multipleControlsTest() throws IOException {
    ObjectNode controlValue1 =
        buildControlValue(
            new String[] {"/item/controls/editbox"}, new String[] {"two boxes"}, "Tokenised");

    ObjectNode controlValue2 =
        buildControlValue(new String[] {"/item/controls/radiogroup"}, new String[] {"1"}, "Phrase");

    ObjectNode controlValue3 =
        buildControlValue(
            new String[] {"/item/controls/calendar/range"},
            new String[] {"2011-04-01", "2011-04-11"},
            "DateRange");

    JsonNode result = doSearch((Arrays.asList(controlValue1, controlValue2, controlValue3)));
    // There should be two Items in the result. One Item matches the token 'box' and the other one
    // matches the token 'something'.
    assertEquals(getAvailable(result), 1);
  }

  @Test(description = "search with a phrase")
  public void singlePhraseTest() throws IOException {
    ObjectNode controlValue =
        buildControlValue(
            new String[] {"/item/controls/editbox"}, new String[] {"An Edit box"}, "Phrase");

    JsonNode result = doSearch((Collections.singleton(controlValue)));
    assertEquals(getAvailable(result), 1);
  }

  @Test(description = "search with multiple phrases")
  public void multiplePhrasesTest() throws IOException {
    ObjectNode controlValue =
        buildControlValue(
            new String[] {"/item/controls/checkboxes"}, new String[] {"1", "2"}, "Phrase");

    JsonNode result = doSearch((Collections.singleton(controlValue)));
    assertEquals(getAvailable(result), 3);
  }

  @DataProvider
  public Object[][] dateRangeProvider() {
    // There is one Item in below date range and another Item out of the range.
    // So when the range is open end, the result should be 2.
    final String START = "2011-04-01";
    final String END = "2011-04-20";
    return new Object[][] {
      {START, END, 1},
      {START, null, 2},
      {START, "", 2},
      {"", END, 1},
      {null, END, 1},
    };
  }

  @Test(description = "search with other params")
  public void searchWithOtherParamsTest() throws IOException {
    ObjectNode controlValue =
        buildControlValue(
            new String[] {"/item/controls/editbox"},
            new String[] {"boxing and something"},
            "Tokenised");

    NameValuePair modifiedAfter = new NameValuePair("modifiedAfter", "2011-04-06");

    JsonNode result = doSearch((Collections.singleton(controlValue)), modifiedAfter);
    assertEquals(getAvailable(result), 1);
  }

  @Test(description = "search with a date range", dataProvider = "dateRangeProvider")
  public void dateRangeTest(String start, String end, int expectNumber) throws IOException {
    ObjectNode controlValue =
        buildControlValue(
            new String[] {"/item/controls/calendar/nodefault"},
            new String[] {start, end},
            "DateRange");

    JsonNode result = doSearch((Collections.singleton(controlValue)));
    assertEquals(getAvailable(result), expectNumber);
  }

  private JsonNode doSearch(Collection<JsonNode> controlValues, NameValuePair... queryVals)
      throws IOException {
    final PostMethod method = new PostMethod(SEARCH_API_ENDPOINT);
    method.setRequestEntity(
        new StringRequestEntity(
            buildPayLoad(controlValues).toString(), "application/json", "UTF-8"));

    final List<NameValuePair> queryParams = new LinkedList<>();
    if (queryVals != null) {
      queryParams.addAll(Arrays.asList(queryVals));
    }
    queryParams.add(new NameValuePair("collections", "0896be21-77d9-1279-90d9-1765e76e5f84"));
    method.setQueryString(queryParams.toArray(new NameValuePair[0]));

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    return mapper.readTree(method.getResponseBodyAsStream());
  }
}
