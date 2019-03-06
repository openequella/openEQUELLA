package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.MailSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("fiveo")
public class MailSettingsTest extends AbstractSessionTest
{
	// https://mailtrap.io/inboxes/115472/messages
	private static final String MAIL_ADDRESS = "@equella.com";
	private static final String MAIL_SERVER = "mailtrap.io:2525";
	private static final String MAIL_USERNAME = "aa3f9947e6ffe5";
	private static final String MAIL_PASSWORD = "2a447edbb38dc6";
	private static final String MAIL_INBOX_ID = "115472";
	private static final String API_KEY = "4c09d79967f1b6971e89ed6aac336f44";

	@Test
	public void testMailSettings() throws InterruptedException, HttpException, IOException
	{
		logon("AutoTest", "automated");
		SettingsPage sp = new SettingsPage(context).load();
		MailSettingsPage msp = sp.mailSettingsPage();
		msp.setServer(MAIL_SERVER);
		String fromEmail = String.valueOf(Math.random()) + MAIL_ADDRESS;
		msp.setEmailAddress(fromEmail);
		msp.setUserName(MAIL_USERNAME);
		msp.setPassword(MAIL_PASSWORD);
		msp.setDisplayName("disp");
		msp.setTestEmailAddress("recpient" + MAIL_ADDRESS);
		msp.testButtonClick();
		assertEquals(msp.getEmailStatus(), "Email successfully sent");

		// allow 10 seconds for the email to arrive
		Thread.sleep(10000);
		org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
		GetMethod emailGet = new GetMethod("http://mailtrap.io/api/v1/inboxes/" + MAIL_INBOX_ID
			+ "/messages?page=1&api_token=" + API_KEY);
		client.executeMethod(emailGet);
		String response = emailGet.getResponseBodyAsString();
		// check response has the randomly generated display name
		assertTrue(response.contains(fromEmail));
		// TODO TLS testing when and if mailtrap implements it
	}
}
