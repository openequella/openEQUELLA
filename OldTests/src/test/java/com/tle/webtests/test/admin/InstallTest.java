package com.tle.webtests.test.admin;

import com.tle.webtests.pageobject.institution.DatabaseRow;
import com.tle.webtests.pageobject.institution.DatabasesPage;
import com.tle.webtests.pageobject.multidb.InstallPage;
import com.tle.webtests.test.AbstractTest;
import com.tle.webtests.test.admin.multidb.AutoTestSetupPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InstallTest extends AbstractTest {

    private static final String DEFAULT_SCHEMA = "Default schema";

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
        installPage.setEmails("");
        installPage.setSmtpServer("");
        installPage.setNoReply("");
        installPage = installPage.installInvalid(InstallPage::isPasswordError);
        Assert.assertTrue(installPage.isPasswordError());
        Assert.assertTrue(installPage.isEmailsError());
        Assert.assertTrue(installPage.isStmpError());
        Assert.assertTrue(installPage.isNoReplyError());
        installPage.setPassword(testConfig.getAdminPassword());
        installPage.setPasswordConfirm(testConfig.getAdminPassword());
        installPage.setEmails("invalidemail");
        installPage.setSmtpServer("localhost");
        installPage.setNoReply("noreply@noreply.com");
        installPage = installPage.installInvalid(ip -> !ip.isPasswordError());
        assertFalse(installPage.isPasswordError());
        assertTrue(installPage.isEmailsError());
        installPage.setPassword(testConfig.getAdminPassword());
        installPage.setEmails(emails);
        installPage.setSmtpServer("mail.google.com");
        DatabasesPage dbPage = installPage.install();
        assertTrue(dbPage.containsDatabase(DEFAULT_SCHEMA));
        DatabaseRow dbRow = dbPage.getDatabaseRow(DEFAULT_SCHEMA);
        dbRow.initialise();
        dbRow.waitForMigrate();
    }
}
