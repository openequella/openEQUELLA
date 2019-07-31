package com.tle.resttests.setup;

/**
 * @author dustin<br>
 *     <br>
 *     To be used in conjunction with SaveInstitutionToFile
 */
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.resttests.AbstractRestAssuredTest;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateFromScratchTest extends AbstractRestAssuredTest {

  private ImportInstitution importer;

  @Override
  protected boolean isInstitutional() {
    return false;
  }

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    importer =
        new ImportInstitution(
            "AutoTest",
            "autotest",
            context.getTestConfig().getAdminPassword(),
            new File(testConfig.getTestFolder(), "institution/autotest"));
  }

  @Test
  public void createInstitution() throws Exception {
    importer.deleteIfExists();
    importer.createInstitution();
  }

  @Test(dependsOnMethods = "createInstitution")
  public void acl() throws JsonParseException, JsonMappingException, IOException {
    importer.importAcls();
  }

  @Test(dataProvider = "users", groups = "ump", dependsOnMethods = "acl")
  public void localuser(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importUser(file);
  }

  @Test(dataProvider = "groups", groups = "ump", dependsOnMethods = "acl")
  public void localgroup(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importGroup(file);
  }

  @Test(dataProvider = "roles", groups = "ump", dependsOnMethods = "acl")
  public void localrole(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importRole(file);
  }

  @Test(dataProvider = "oauths", dependsOnGroups = "ump")
  public void oauth(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importEntity("oauth", file);
  }

  @Test(dataProvider = "schemas", dependsOnMethods = "oauth")
  public void schema(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importEntity("schema", file);
  }

  @Test(dataProvider = "workflows", dependsOnMethods = "schema", groups = "workflow")
  public void workflow(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importEntity("workflow", file);
  }

  @Test(dataProvider = "collections", dependsOnGroups = "workflow")
  public void collection(File file) throws JsonParseException, JsonMappingException, IOException {
    importer.importEntity("collection", file);
  }

  @Test(dataProvider = "entityTypes", dependsOnMethods = "collection")
  public void entityAcls(String type) throws JsonParseException, JsonMappingException, IOException {
    importer.importEntityAcls(type);
  }

  @Test(dataProvider = "items", dependsOnMethods = "collection")
  public void item(File item) throws JsonParseException, JsonMappingException, IOException {
    importer.importItem(item);
  }

  @Test(dependsOnMethods = "item")
  public void waitUntilIndexed() throws Exception {

    final int items = importer.getItemCount();
    RequestSpecification req =
        importer.searches().searchRequest("", null).queryParam("showall", true);
    importer
        .searches()
        .waitUntilIgnoreError(
            req,
            new Function<ObjectNode, Boolean>() {
              @Override
              public Boolean apply(ObjectNode input) {
                return input.get("available").asInt() == items;
              }
            });
  }

  @DataProvider(parallel = true, name = "oauths")
  public Object[][] oauthList() {
    return fileProvider(importer.getEntityDir("oauth"));
  }

  @DataProvider(parallel = true, name = "schemas")
  public Object[][] schemaList() {
    return fileProvider(importer.getEntityDir("schema"));
  }

  @DataProvider(parallel = true, name = "workflows")
  public Object[][] workflowList() {
    return fileProvider(importer.getEntityDir("workflow"));
  }

  @DataProvider(parallel = true, name = "collections")
  public Object[][] collectionList() {
    return fileProvider(importer.getEntityDir("collection"));
  }

  @DataProvider(parallel = true, name = "users")
  public Object[][] userList() {
    return fileProvider(importer.getUserDir());
  }

  @DataProvider(parallel = true, name = "groups")
  public Object[][] groupList() {
    return fileProvider(importer.getGroupDir());
  }

  @DataProvider(parallel = true, name = "roles")
  public Object[][] roleList() {
    return fileProvider(importer.getRoleDir());
  }

  @DataProvider(parallel = true, name = "items")
  public Object[][] itemList() {
    return fileProvider(importer.getItemDir());
  }

  @DataProvider(parallel = true, name = "entityTypes")
  public Object[][] entityTypes() {
    Set<String> types = importer.getEntityTypes();
    Object[][] data = new Object[types.size()][];
    int i = 0;
    for (String type : types) {
      data[i++] = new Object[] {type};
    }
    return data;
  }

  public Object[][] fileProvider(File rootDir) {
    File[] files = importer.getJsonFiles(rootDir);
    Object[][] data = new Object[files.length][];
    int i = 0;
    for (File file : files) {
      data[i++] = new Object[] {file};
    }
    return data;
  }
}
