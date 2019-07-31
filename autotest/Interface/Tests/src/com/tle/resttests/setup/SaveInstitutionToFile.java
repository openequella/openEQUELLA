package com.tle.resttests.setup;

/**
 * @author dustin<br>
 *     <br>
 *     To be used in conjunction with CreateFromScratchTest
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.TestConfig;
import com.tle.json.requests.BaseEntityRequests;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

public class SaveInstitutionToFile extends AbstractImportExport {
  boolean bucket = false;

  public SaveInstitutionToFile(File baseDir, String shortName, String password) {
    super(
        new TestConfig(SaveInstitutionToFile.class, true), shortName, shortName, password, baseDir);
  }

  public SaveInstitutionToFile(
      File baseDir, String shortName, String password, String fullUrl, boolean bucket) {
    super(
        new TestConfig(SaveInstitutionToFile.class, true),
        shortName,
        shortName,
        password,
        password,
        baseDir,
        fullUrl);
    this.bucket = bucket;
  }

  public static void main(String[] args) throws Exception {
    SaveInstitutionToFile instSaver =
        new SaveInstitutionToFile(new File(args[0]), args[1], args[2]);
    instSaver.export();
  }

  public void export() throws Exception {
    export(false);
  }

  public void export(boolean ignoreFiles) throws Exception {
    for (Entry<String, BaseEntityRequests> entry : entityRequests.entrySet()) {
      exportEntities(entry.getKey(), entry.getValue());
    }
    exportItems(ignoreFiles);
    exportUsers();
    exportGroups();
    exportRoles();
    exportAcls();
  }

  private void exportAcls() throws Exception {
    File aclDir = getAclDir();
    aclDir.mkdirs();
    ObjectNode results = aclRequests.list();
    MAPPER.writeValue(getInstitutionAclFile(aclDir), results);
  }

  private void exportGroups() throws Exception {
    File groupsDir = getGroupDir();
    groupsDir.mkdirs();
    ObjectNode results = groupRequests.export();
    for (JsonNode userJson : results.withArray("results")) {
      File userFile = new File(groupsDir, userJson.get("id").asText() + ".json");
      MAPPER.writeValue(userFile, userJson);
    }
  }

  private void exportRoles() throws Exception {
    File rolesDir = getRoleDir();
    rolesDir.mkdirs();
    ObjectNode results = roleRequests.export();
    for (JsonNode userJson : results.withArray("results")) {
      File userFile = new File(rolesDir, userJson.get("id").asText() + ".json");
      MAPPER.writeValue(userFile, userJson);
    }
  }

  private void exportEntities(String name, BaseEntityRequests req) throws Exception {
    File entityDir = new File(baseDir, name);
    entityDir.mkdirs();
    ObjectNode results = req.listForExport();
    for (JsonNode entityJson : results.withArray("results")) {
      ObjectNode entity = (ObjectNode) entityJson;
      String id = req.getId(entity);
      File file = new File(entityDir, id + ".json");
      MAPPER.writeValue(file, entity);
    }
    ObjectNode acls = req.listAcls();
    MAPPER.writeValue(getEntityAclsFile(entityDir), acls);
  }

  private void exportUsers() throws Exception {
    File usersDir = getUserDir();
    usersDir.mkdirs();
    ObjectNode results = userRequests.export();
    for (JsonNode userJson : results.withArray("results")) {
      File userFile = new File(usersDir, userJson.get("id").asText() + ".json");
      MAPPER.writeValue(userFile, userJson);
    }
  }

  protected void exportItems() throws Exception {
    exportItems(false);
  }

  protected void exportItems(boolean ignoreFiles) throws Exception {
    File itemsDir = new File(baseDir, "item");
    itemsDir.mkdirs();
    if (testConfig.isEquella()) {
      int offset = 0;
      int available = Integer.MAX_VALUE;
      while (offset < available) {
        Integer[] ret = exportFromEquella(offset, itemsDir, ignoreFiles);
        offset = ret[0];
        available = ret[1];
      }
    } else {
      String offset = "true";
      while (offset != null) {
        offset = exportFrom(offset, itemsDir, ignoreFiles);
      }
    }
  }

  protected String exportFrom(String offset, final File itemsDir) throws Exception {
    return exportFrom(offset, itemsDir, false);
  }

  protected String exportFrom(String offset, final File itemsDir, final boolean ignoreFiles)
      throws Exception {
    return exportFrom(
        offset,
        itemsDir,
        new Function<ObjectNode, Void>() {
          @Override
          public Void apply(ObjectNode entity) {
            ItemId itemId = itemRequests.getId(entity);
            String folderName = itemId.getUuid() + "-" + itemId.getVersion();
            File parent;

            if (bucket) {
              parent = new File(itemsDir, itemId.getUuid().substring(0, 2));
              parent.mkdirs();
            } else {
              parent = itemsDir;
            }
            File file = new File(parent, folderName + ".json");
            try {
              if (!ignoreFiles) {
                saveFiles(itemId, new File(itemsDir, folderName));
              }
              writeJson(entity, file);
            } catch (Exception e) {
              Throwables.propagate(e);
            }
            return null;
          }
        });
  }

  protected Integer[] exportFromEquella(int offset, File itemsDir, boolean ignoreFiles)
      throws Exception {
    return exportFromEquellaPerItem(
        offset,
        itemsDir,
        new Function<ObjectNode, Void>() {
          @Override
          public Void apply(ObjectNode entity) {
            ItemId itemId = itemRequests.getId(entity);
            String folderName = itemId.getUuid() + '-' + itemId.getVersion();
            File file = new File(itemsDir, folderName + ".json");
            try {
              saveFiles(itemId, new File(itemsDir, folderName));
              writeJson(entity, file);
            } catch (Exception e) {
              Throwables.propagate(e);
            }
            return null;
          }
        });
  }

  /**
   * The EQUELLA version of exportFrom
   *
   * @param offset
   * @param itemsDir
   * @param perItem
   * @return
   * @throws Exception
   */
  protected Integer[] exportFromEquellaPerItem(
      int offset, File itemsDir, Function<ObjectNode, Void> perItem) throws Exception {
    ImmutableMap<String, ?> params =
        ImmutableMap.of("start", offset, "info", "export", "length", 50, "showall", true);
    ObjectNode results =
        searchRequests.search(searchRequests.searchRequest("", "modified").parameters(params));
    int available = results.get("available").asInt();
    int length = results.get("length").asInt();
    for (JsonNode entityJson : results.withArray("results")) {
      ObjectNode entity = (ObjectNode) entityJson;
      perItem.apply(entity);
    }
    return new Integer[] {offset + length, available};
  }

  /**
   * The EPS version of exportFrom
   *
   * @param offset
   * @param itemsDir
   * @param perItem
   * @return
   * @throws Exception
   */
  protected String exportFrom(String offset, File itemsDir, Function<ObjectNode, Void> perItem)
      throws Exception {
    ImmutableMap<String, ?> params = ImmutableMap.of("info", "export", "scrollid", offset);
    ObjectNode results = searchRequests.scan(searchRequests.successfulRequest().parameters(params));
    if (!results.withArray("results").has(0)) {
      return null;
    }
    for (JsonNode entityJson : results.withArray("results")) {
      ObjectNode entity = (ObjectNode) entityJson;
      perItem.apply(entity);
    }
    return results.has("scrollId") ? results.get("scrollId").asText() : null;
  }

  protected void saveFiles(final ItemId itemId, File filesDir) throws IOException {
    ObjectNode files = itemRequests.listFiles(itemId);
    ArrayNode fileList = files.withArray("files");
    if (fileList.size() > 0) {
      filesDir.mkdirs();
      for (JsonNode file : fileList) {
        final String name = file.get("name").asText();
        ByteSource src =
            new ByteSource() {
              @Override
              public InputStream openStream() throws IOException {
                return itemRequests
                    .file(itemRequests.successfulRequest(), itemId, name)
                    .body()
                    .asInputStream();
              }
            };
        File destFile = new File(filesDir, name);
        destFile.getParentFile().mkdirs();
        src.copyTo(Files.asByteSink(destFile));
      }
    }
  }
}
