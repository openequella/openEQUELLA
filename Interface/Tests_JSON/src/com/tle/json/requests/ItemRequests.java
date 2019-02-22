package com.tle.json.requests;

import static com.tle.json.entity.Items.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import java.net.URI;

public class ItemRequests extends AbstractCleanupRequests<ItemId> {
  public static final String PATH = "api/item";

  public ItemRequests(
      URI baseUri,
      TokenProvider tokens,
      ObjectMapper mapper,
      PageContext pageContext,
      CleanupController cleanupController,
      TestConfig testConfig) {
    super(baseUri, tokens, mapper, pageContext, cleanupController, testConfig);
  }

  @Override
  protected String getBasePath() {
    return PATH;
  }

  public void delete(ItemId id, boolean purge, boolean waitForIndex) {
    deleteRequest(true, purge, waitForIndex).delete(getResolvedEntityUri(), getIdParams(id));
  }

  public void delete(ItemId id, boolean purge) {
    deleteRequest(true, purge, true).delete(getResolvedEntityUri(), getIdParams(id));
  }

  public ObjectNode get(ItemId id, String info) {
    return get(successfulRequest().queryParam("info", info), id);
  }

  public ItemId createId(ObjectNode item, boolean waitForIndex) {
    return getId(create(item, waitForIndex));
  }

  public ItemId createId(String collection, String firstParam, Object value, Object... others) {
    return getId(create(json(collection, firstParam, value, others), false));
  }

  public ItemId createId(
      String collection, boolean waitForIndex, String firstParam, Object value, Object... others) {
    return getId(create(json(collection, firstParam, value, others), waitForIndex));
  }

  public ObjectNode create(String collection) {
    return create(json(collection), false);
  }

  public ObjectNode create(String collection, String firstParam, Object value, Object... others) {
    return create(json(collection, firstParam, value, others), false);
  }

  public ObjectNode create(ObjectNode object, Object waitForIndex) {
    return create(createRequest(object, waitForIndex instanceof Boolean ? 45 : waitForIndex));
  }

  public ObjectNode create(
      ObjectNode object, boolean waitForIndex, String firstParam, Object value, Object... others) {
    return create(createRequest(object, waitForIndex, firstParam, value, others));
  }

  public RequestSpecification createRequest(ObjectNode object, Object waitForIndex) {
    RequestSpecification request = createRequest(object);
    if (waitForIndex != null) {
      request.queryParam("waitforindex", waitForIndex);
    }
    return request;
  }

  public RequestSpecification createRequest(
      ObjectNode object, Object waitForIndex, String param, Object paramValue, Object... others) {
    RequestSpecification request = createRequest(object, waitForIndex);
    request.queryParameters(param, paramValue, others);
    return request;
  }

  @Override
  protected RequestSpecification deleteRequest(boolean requireSuccess) {
    return deleteRequest(requireSuccess, true, true);
  }

  protected RequestSpecification deleteRequest(
      boolean requireSuccess, boolean purge, boolean waitForIndex) {
    RequestSpecification request = super.deleteRequest(requireSuccess);
    if (purge) {
      request = request.param("purge", true);
    }
    if (waitForIndex) {
      request = request.param("waitforindex", true);
    }
    return request;
  }

  @Override
  protected String getIdPathString() {
    return "/{uuid}/{version}";
  }

  @Override
  protected Object[] getIdParams(ItemId id) {
    return new Object[] {id.getUuid(), id.getVersion()};
  }

  @Override
  public ItemId getId(ObjectNode entity) {
    String uuid = entity.get("uuid").asText();
    int version = entity.get("version").asInt();
    return new ItemId(uuid, version);
  }

  public JsonNode history(ItemId itemId) {
    return node(successfulRequest().get(getResolvedEntityUri() + "/history", getIdParams(itemId)));
  }

  public void history(RequestSpecification request, ItemId itemId) {
    request.get(getResolvedEntityUri() + "/history", getIdParams(itemId));
  }

  public JsonNode comments(ItemId itemId) {
    return node(successfulRequest().get(getResolvedEntityUri() + "/comment", getIdParams(itemId)));
  }

  public ObjectNode moderation(ItemId itemId) {
    return object(
        successfulRequest().get(getResolvedEntityUri() + "/moderation", getIdParams(itemId)));
  }

  public ObjectNode moderation(RequestSpecification request, ItemId itemId) {
    return object(request.get(getResolvedEntityUri() + "/moderation", getIdParams(itemId)));
  }

  public <T> T untilModeration(final ItemId itemId, final Function<ObjectNode, T> until) {
    return requestWaiter(successfulRequest())
        .until(
            new Function<RequestSpecification, T>() {
              @Override
              public T apply(RequestSpecification req) {
                return until.apply(moderation(req, itemId));
              }
            });
  }

  public ObjectNode readLock(ItemId itemId) {
    return object(successfulRequest().get(getResolvedEntityUri() + "/lock", getIdParams(itemId)));
  }

  public Response nonExistantLock(ItemId itemId) {
    return notFoundRequest().get(getResolvedEntityUri() + "/lock", getIdParams(itemId));
  }

  public String copyFilesForEdit(ItemId itemId) {
    return createRequest()
        .post(
            getResolvedPath()
                + "/copy?uuid="
                + itemId.getUuid()
                + "&version="
                + itemId.getVersion())
        .getHeader("location");
  }

  public RequestSpecification actionSuccess() {
    return successfulRequest();
  }

  public ObjectNode action(ItemId itemId, String action) {
    return action(actionSuccess(), itemId, action);
  }

  public ObjectNode action(RequestSpecification request, ItemId itemId, String action) {
    request.post(
        getResolvedEntityUri() + "/action/{action}", itemId.getUuid(), itemId.getVersion(), action);
    return get(itemId);
  }

  public ObjectNode submit(ItemId itemId, String message) {
    RequestSpecification request = actionSuccess();
    if (message != null) {
      request = request.queryParam("message", message);
    }
    return action(request, itemId, "submit");
  }

  public ObjectNode listFiles(ItemId itemId) {
    return object(
        successfulRequest()
            .get(getResolvedEntityUri() + "/file", itemId.getUuid(), itemId.getVersion()));
  }

  public Response file(RequestSpecification request, ItemId itemId, String filePath) {
    return request.get(
        getResolvedEntityUri() + "/file/" + filePath, itemId.getUuid(), itemId.getVersion());
  }

  public Response headFile(ItemId itemId, String filePath) {
    return successfulRequest()
        .head(getResolvedEntityUri() + "/file/" + filePath, itemId.getUuid(), itemId.getVersion());
  }

  public Response importer(ObjectNode object, String stagingId) {
    RequestSpecification req = importRequest(object);
    if (stagingId != null) {
      req = req.queryParam("file", stagingId);
    }
    return importer(req);
  }
}
