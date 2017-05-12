package com.tle.webtests.test.admin.multidb;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.tle.webtests.pageobject.institution.DatabaseEditDialog;
import com.tle.webtests.pageobject.institution.DatabaseRow;
import com.tle.webtests.pageobject.institution.DatabasesPage;
import com.tle.webtests.pageobject.institution.MigrationProgressDialog;
import com.tle.webtests.pageobject.multidb.InstallPage;
import com.tle.webtests.test.AbstractTest;

public class DbTest extends AbstractTest
{
	private static final String DEFAULT_SCHEMA = "Default schema";
	private final String licenseKey;

	private String hostname;
	private String username;
	private String password;
	private String prefix;

	public DbTest() throws IOException
	{
		super();
		hostname = testConfig.getProperty("multidb.dbhost");
		username = testConfig.getProperty("multidb.dbuser");
		password = testConfig.getProperty("multidb.dbpass");
		prefix = testConfig.getProperty("multidb.dbprefix");
		licenseKey = Resources.toString(getClass().getResource("dev_license.txt"), Charsets.US_ASCII);
	}

	@Override
	protected boolean isInstitutional()
	{
		return false;
	}

	@Test
	public void installFirstTime()
	{
		String emails = "noreply@equella.com;test@equella.com";
		InstallPage installPage = new InstallPage(context).load();
		installPage.setPassword("");
		installPage.setPasswordConfirm("");
		installPage.setLicenseKey("");
		installPage.setEmails("");
		installPage.setSmtpServer("");
		installPage = installPage.installInvalid();
		Assert.assertTrue(installPage.isPasswordError());
		Assert.assertTrue(installPage.isEmailsError());
		Assert.assertTrue(installPage.isStmpError());
		Assert.assertTrue(installPage.isLicenseError());
		installPage.setPassword("tle010");
		installPage.setPasswordConfirm("tle010");
		installPage.setLicenseKey("broken");
		installPage.setEmails("invalidemail");
		installPage.setSmtpServer("localhost");
		installPage = installPage.installInvalid();
		assertFalse(installPage.isPasswordError());
		assertTrue(installPage.isLicenseError());
		assertTrue(installPage.isEmailsError());
		installPage.setPassword("tle010");
		installPage.setLicenseKey(licenseKey);
		installPage.setEmails(emails);
		installPage.setSmtpServer("mail.google.com");
		DatabasesPage dbPage = installPage.install();
		assertTrue(dbPage.containsDatabase(DEFAULT_SCHEMA));
		new AutoTestSetupPage(context).load().clearData();
	}

	@Test(dependsOnMethods = "installFirstTime")
	public void addNewSchema()
	{
		DatabasesPage dbPage = new DatabasesPage(context, "tle010").load();
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
	public void migrateMultipleAndProgress()
	{
		DatabasesPage dbPage = new DatabasesPage(context, "tle010").load();
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
