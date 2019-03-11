package com.tle.webtests.test.admin.multidb;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.institution.*;
import com.tle.webtests.test.AbstractTest;
import java.io.IOException;
import org.testng.annotations.Test;

public class DbTest extends AbstractTest {
  private static final String DEFAULT_SCHEMA = "Default schema";

  private String hostname;
  private String username;
  private String password;
  private String prefix;

  public DbTest() throws IOException {
    super();
    hostname = testConfig.getProperty("multidb.dbhost");
    username = testConfig.getProperty("multidb.dbuser");
    password = testConfig.getProperty("multidb.dbpass");
    prefix = testConfig.getProperty("multidb.dbprefix");
  }

  @Override
  protected void prepareBrowserSession() {
    new ServerAdminLogonPage(context)
        .load()
        .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
  }

  @Override
  protected boolean isInstitutional() {
    return false;
  }

  @Test
  public void addNewSchema() {

    DatabasesPage dbPage = new DatabasesPage(context).load();
    DatabaseEditDialog newDb = dbPage.addSchema();
    String hostDbPart = hostname + "/" + prefix + "2";
    newDb.setJdbcUrl("jdbc:postgresql://" + hostDbPart);
    newDb.setUsername(username);
    newDb.setPassword(password);
    dbPage = newDb.finishOnline();
    String name = username + " @ " + hostDbPart;
    assertTrue(dbPage.containsDatabase(name));
    DatabaseRow databaseRow = dbPage.getDatabaseRow(name);
    databaseRow.waitForCheck();
    databaseRow.waitForMigrate();
    databaseRow.assertOnline();
  }

  @Test(dependsOnMethods = "addNewSchema")
  public void migrateMultipleAndProgress() {
    DatabasesPage dbPage = new DatabasesPage(context).load();
    DatabaseEditDialog newDb = dbPage.addSchema();
    String hostDbPart = hostname + "/" + prefix + "3";
    newDb.setJdbcUrl("jdbc:postgresql://" + hostDbPart);
    newDb.setUsername(username);
    newDb.setPassword(password);
    dbPage = newDb.finish();
    String name = username + " @ " + hostDbPart;
    assertTrue(dbPage.containsDatabase(name));
    DatabaseRow db3Row = dbPage.getDatabaseRow(name);
    db3Row.waitForCheck();
    db3Row.assertRequiresMigrating();
    db3Row.setCheckbox(true);
    DatabaseRow defaultRow = dbPage.getDatabaseRow(DEFAULT_SCHEMA);
    defaultRow.setCheckbox(true);
    dbPage.migrateAll();
    defaultRow.assertMigrating();
    db3Row.assertMigrating();
    MigrationProgressDialog progress = db3Row.progress();
    progress.waitForFinish();
    progress.close();
    db3Row.waitForMigrate();
    defaultRow.waitForMigrate();
    db3Row.assertOffline();
  }
}
