package com.tle.webtests.test.users;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.LoginNoticePage;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.AddIPAddressPage;
import com.tle.webtests.pageobject.settings.LoginSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("login")
public class LoginSettingsTest extends AbstractSessionTest
{
	public static final String LOGIN_LINK_TITLE = LoginSettingsPage.LOGIN_SETTINGS;
	private static final String USER = "AutoTest";
	private static final String IP_ADDRESS_WILD = "*.*.*.*";
	private static final String DISCARDABLE_IP_ADDRESS = "127.126.125.124";
	private static final String INVALID_IP_ADDRESS = "256.126.125.124";
	private static final String LOGIN_NOTICE = "Do you accept this?";
	private static final String INVALID_USER = "You must select a user";
	private static final String INVALID_IP = "You must select a user";
	private static final String INVALID_IP_MSG = "Invalid address! Must be a numeric IP address (wildcards allowed)";

	@Test
	public void testEnableViaIP()
	{
		// Login
		LoginSettingsPage lsp = logonToLoginSettingsPage(false);

		assertTrue(lsp.controlsHidden());
		lsp.setEnableViaIp(true);
		assertTrue(!lsp.controlsHidden());

		lsp.setDisableAutoLogin(true);
		lsp.setDoNotStoreDrm(true);
		lsp.setDisallowUserEdit(true);
		lsp.setNotice(LOGIN_NOTICE);

		// Save
		lsp.saveFailure();

		// Check for errors (User and IP)
		assertTrue(lsp.hasError(INVALID_USER));
		assertTrue(lsp.hasError(INVALID_IP));

		// Fix mandatory
		lsp.setUser(USER);
		assertFalse(lsp.hasError(INVALID_USER));

		lsp.addIPAddress(DISCARDABLE_IP_ADDRESS);
		lsp.addIPAddress(IP_ADDRESS_WILD);

		AddIPAddressPage aipd = lsp.openAddIPDialog();
		assertEquals(aipd.setIpAddress(INVALID_IP_ADDRESS).okWithError(), INVALID_IP_MSG);
		lsp = aipd.cancel();

		assertFalse(lsp.hasError(INVALID_IP));

		// Check successful

		lsp.saveSuccess();

		// Navigate away and return. Ensure settings
		new HomePage(context).load();
		lsp = new LoginSettingsPage(context).load();
		assertTrue(lsp.isEnableViaIp());
		assertTrue(lsp.isDisableAutoLogin());
		assertTrue(lsp.isDoNotStoreDrm());
		assertTrue(lsp.isDisallowUserEdit());
		assertTrue(lsp.hasUser(USER));
		assertTrue(lsp.hasIpAddress(IP_ADDRESS_WILD));

		// Test delete IP
		lsp.deleteIpAddress(DISCARDABLE_IP_ADDRESS);

		lsp.saveSuccess();

		logout();

		// Check for Auto Login link
		LoginPage lp = new LoginPage(context).load();
		assertTrue(lp.hasAutoLogin());

		// Login and check notice
		LoginNoticePage lnp = lp.loginWithNotice(USER, "automated");
		assertEquals(lnp.getNoticeText(), LOGIN_NOTICE);
		lnp.acceptNotice();

		logout();
	}

	private LoginSettingsPage logonToLoginSettingsPage(boolean hasNotice)
	{
		if( hasNotice )
		{
			logonWithNotice(USER, "automated");
		}
		else
		{
			logon(USER, "automated");
		}
		SettingsPage sp = new SettingsPage(context).load();
		return sp.loginSettings();
	}
}
