package com.tle.webtests.test.payment.backend;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.payment.backend.StoreSetupPage;
import com.tle.webtests.pageobject.payment.backend.StoreSetupPage.Fields;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.files.Attachments;

/**
 * @author Dustin
 * @see DTEC: #017948
 **/
// Because we're changing system wide settings I'll run this in flakey so it
// doesn't interfere with current/future ecommerce tests
@TestInstitution("flakey")
public class StoreSettingsTest extends AbstractSessionTest
{
	@Test
	public void testStoreSettings()
	{
		final String NAME = "name";
		final String DESC = "description";
		final String C_NAME = "contactname";
		final String C_NUM = "123456";
		final String C_EMAIL = "a@b.com";
		final List<String> detailsList = new ArrayList<String>();

		detailsList.add(NAME);
		detailsList.add(DESC);
		detailsList.add(C_NAME);
		detailsList.add(C_NUM);
		detailsList.add(C_EMAIL);

		logon("AutoTest", "automated");

		StoreSetupPage settings = new SettingsPage(context).load().storeSetupPage();

		Assert.assertTrue(settings.isFormHidden());
		settings.setEnabled(true);
		Assert.assertFalse(settings.isFormHidden());

		settings.setDetails("", "", "", "", "");
		settings = settings.saveWithFail();
		// test validation
		Assert.assertTrue(settings.isFieldInvalid(Fields.NAME));
		Assert.assertTrue(settings.isFieldInvalid(Fields.DESCRIPTION));
		Assert.assertTrue(settings.isFieldInvalid(Fields.CONTACT_NAME));
		Assert.assertTrue(settings.isFieldInvalid(Fields.CONTACT_NUMBER));
		Assert.assertTrue(settings.isFieldInvalid(Fields.CONTACT_EMAIL));
		settings.setDetails(NAME, DESC, C_NAME, C_NUM, C_EMAIL);
		settings.uploadIcon(Attachments.get("shopicon.png"));
		settings.uploadImage(Attachments.get("shopimage.jpeg"));
		settings.save();
		List<String> details = settings.getDetails();
		assertListEquals(details, detailsList);

		settings.setDetails("", "", "", "", "");
		settings.deleteIcon();
		settings.deleteImage();
		settings.setEnabled(false);
		settings.save();
	}
}
