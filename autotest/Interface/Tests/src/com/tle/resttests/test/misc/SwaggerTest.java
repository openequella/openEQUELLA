package com.tle.resttests.test.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestInstitution;
import com.tle.resttests.AbstractRestApiTest;
import com.tle.resttests.util.RestTestConstants;
import java.util.Iterator;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "eps")
@TestInstitution("autotest")
@SuppressWarnings("nls")
public class SwaggerTest extends AbstractRestApiTest {
  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>("SwaggerTest", RestTestConstants.USERID_AUTOTEST));
  }

  private List<String> getResources(String token) throws Exception {
    return getResources(context, token);
  }

  private List<String> getResources(PageContext context, String token) throws Exception {
    JsonNode swaggerPage = getEntity(context.getBaseUrl() + "api/resources", token);
    Assert.assertTrue(swaggerPage.has("apis"));

    List<String> resList = Lists.newArrayList();
    Iterator<JsonNode> iterator = swaggerPage.get("apis").iterator();
    while (iterator.hasNext()) {
      JsonNode api = iterator.next();
      resList.add(api.get("description").asText());
    }
    return resList;
  }

  @Test
  public void testAnonSwagger() throws Exception {
    List<String> swaggerPage = getResources(null);

    Assert.assertTrue(swaggerPage.contains("search"));
    Assert.assertFalse(swaggerPage.contains("institution"));
    Assert.assertFalse(swaggerPage.contains("scheduler"));
  }

  @Test
  public void testAuthSwagger() throws Exception {
    List<String> swaggerPage = getResources(getToken());

    Assert.assertTrue(swaggerPage.contains("search"));
    Assert.assertFalse(swaggerPage.contains("institution"));
    Assert.assertFalse(swaggerPage.contains("scheduler"));
  }

  @Test
  public void testSystemAuthSwagger() throws Exception {
    List<String> swaggerPage = getResources(ADMIN_TOKEN);

    Assert.assertTrue(swaggerPage.contains("search"));
    Assert.assertFalse(swaggerPage.contains("institution"));
    Assert.assertTrue(swaggerPage.contains("scheduler"));
  }

  @Test
  public void testInstSwagger() throws Exception {

    PageContext newContext = new PageContext(context, testConfig.getAdminUrl());
    List<String> swaggerPage = getResources(newContext, SYSTEM_TOKEN);

    Assert.assertFalse(swaggerPage.contains("search"));
    Assert.assertTrue(swaggerPage.contains("institution"));
    Assert.assertFalse(swaggerPage.contains("scheduler"));
  }
}
