package com.tle.webtests.test.admin.settings;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class PearsonSCORMServicesTest extends AbstractCleanupTest {
  private static final String BASE_URL = "https://scorm-stg.pearsoned.com/";
  private static final String CONSUMER_KEY = "8ca8f524fc76dda75a1d27da11e88e3f";
  private static final String CONSUMER_SECRET = "18f5b065bea94878ab0881630d21b87b";
  private static final String NAMESPACE = "equella";

  /*
  @Test
  public void testPSSSettings()
  {
  	logon("AutoTest", "automated");
  	SettingsPage sp = new SettingsPage(context).load();
  	PSSSettingsPage psssp = sp.pssSettingsPage();
  	psssp.enablePSS(true);

  	// Save with error
  	psssp = psssp.saveWithoutTest();
  	assertTrue(psssp.hasError("You must successfully test the Pearson SCORM Services settings before saving."));

  	// Test with error
  	psssp = psssp.testFailValidation("URL cannot be blank");

  	// Check blank errors
  	assertTrue(psssp.hasError("URL cannot be blank"));
  	assertTrue(psssp.hasError("Consumer key cannot be blank"));
  	assertTrue(psssp.hasError("Consumer secret cannot be blank"));
  	assertTrue(psssp.hasError("Namespace cannot be blank"));

  	// Set fields
  	psssp.setBaseUrl("derp");
  	psssp.setConsumerKey(CONSUMER_KEY);
  	psssp.setConsumerSecret(CONSUMER_SECRET);
  	psssp.setNamespace(NAMESPACE);
  	psssp = psssp.testFailValidation("URL entered is not a valid URL");

  	// Check invalid errors
  	assertTrue(psssp.hasError("URL entered is not a valid URL"));
  	psssp.setBaseUrl(BASE_URL);

  	// Test
  	psssp = psssp.testSuccess();
  	assertTrue(psssp.hasSuccess("Successfully connected to Pearson SCORM Services"));

  	// Save no errors
  	psssp = psssp.save();
  	String successReceipt = "Pearson SCORM Services settings saved successfully";
  	ReceiptPage.waiter(successReceipt, psssp);

  	// Disable and save
  	psssp = new SettingsPage(context).load().pssSettingsPage();
  	psssp.enablePSS(false);
  	psssp.save();
  	ReceiptPage.waiter(successReceipt, psssp);

  	// Check disabled then enable and save
  	psssp = new SettingsPage(context).load().pssSettingsPage();
  	assertFalse(psssp.isEnabled());
  	psssp.enablePSS(true);
  	psssp = psssp.testSuccess();
  	psssp.save();

  	// Leave disabled... so any SCORM shizzle doesn't upload
  	psssp.enablePSS(false);
  	psssp.save();

  	ReceiptPage.waiter(successReceipt, psssp);
  }*/

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("AutoTest", "automated");
  }
}
