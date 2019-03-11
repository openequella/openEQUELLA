package com.tle.json.entity;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tle.json.BaseJSONCreator;
import java.util.Collection;
import java.util.Date;

@SuppressWarnings("nls")
public class Items extends BaseJSONCreator {
  public static ObjectNode json(String collection) {
    return jsonXml(collection, null);
  }

  public static ObjectNode editMetadata(
      ObjectNode item, String firstParam, Object value, Object... others) {
    PropBagEx metadata = new PropBagEx(item.get("metadata").asText());
    editXmlMetadata(metadata, firstParam, value, others);
    item.put("metadata", metadata.toString());
    return item;
  }

  public static ObjectNode addAttachment(ObjectNode item, String filename) {
    ObjectNode attachment = mapper.createObjectNode();
    attachment.put("type", "file");
    attachment.put("filename", filename);
    attachment.put("description", filename);
    item.withArray("attachments").add(attachment);
    return item;
  }

  private static PropBagEx editXmlMetadata(
      PropBagEx metadata, String firstParam, Object value, Object... others) {
    metadata.setNode(firstParam, value.toString());
    for (int i = 0; i < others.length; i++) {
      metadata.setNode(others[i].toString(), others[++i].toString());
    }
    return metadata;
  }

  public static ObjectNode json(
      String collection, String firstParam, Object value, Object... others) {
    PropBagEx metadata = new PropBagEx();
    editXmlMetadata(metadata, firstParam, value, others);
    return jsonXml(collection, metadata.toString());
  }

  public static ObjectNode jsonXml(String collection, String xml) {
    ObjectNode item = mapper.createObjectNode();
    item.with("collection").put("uuid", collection);
    item.put("metadata", xml);
    return item;
  }

  public static ObjectNode history(String userId, Date date, String event, String state) {
    return history(userId, date, event, state, null, null, null, null, null);
  }

  public static ObjectNode history(
      String userId,
      Date date,
      String event,
      String state,
      String msg,
      String step,
      String toStep,
      String stepName,
      String toStepName) {
    ObjectNode hevent = mapper.createObjectNode();
    hevent.with("user").put("id", userId);
    hevent.put("date", ISO8601Utils.format(date));
    hevent.put("type", event);
    hevent.put("state", state);
    hevent.put("comment", msg);
    hevent.put("step", step);
    hevent.put("toStep", toStep);
    hevent.put("stepName", stepName);
    hevent.put("toStepName", toStepName);
    return hevent;
  }

  public static ObjectNode importStatus(String uuid, char status) {
    ObjectNode ns = mapper.createObjectNode();
    ns.put("uuid", uuid);
    ns.put("status", Character.toString(status));
    return ns;
  }

  public static ObjectNode statusMsg(char type, String message, String userId, Date date) {
    String typeStr;
    switch (type) {
      case 'a':
        typeStr = "accept";
        break;
      case 'r':
        typeStr = "reject";
        break;
      case 's':
        typeStr = "submit";
        break;
      case 'c':
        typeStr = "comment";
        break;
      default:
        throw new Error("Unsupported type: " + type);
    }
    ObjectNode msg = mapper.createObjectNode();
    msg.put("type", typeStr);
    msg.put("message", message);
    msg.put("date", ISO8601Utils.format(date));
    msg.with("user").put("id", userId);
    return msg;
  }

  public static ObjectNode importTaskStatus(
      String uuid, char status, Date started, Date due, Collection<String> accepted) {
    ObjectNode ns = importStatus(uuid, status);
    ns.put("started", ISO8601Utils.format(started));
    ns.put("due", due != null ? ISO8601Utils.format(due) : null);
    ArrayNode users = ns.withArray("acceptedUsers");
    for (String user : accepted) {
      users.add(user);
    }
    return ns;
  }
}
