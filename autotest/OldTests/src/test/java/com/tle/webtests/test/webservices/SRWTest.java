package com.tle.webtests.test.webservices;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import gov.loc.www.zing.srw.RecordType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;
import gov.loc.www.zing.srw.SearchRetrieveResponseType;
import gov.loc.www.zing.srw.interfaces.SRWPort;
import gov.loc.www.zing.srw.service.SRWSampleServiceLocator;
import java.net.URL;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SRWTest extends AbstractCleanupTest {
  public SRWTest() {
    setDeleteCredentials(AUTOTEST_LOGON, AUTOTEST_PASSWD);
  }

  @Test
  public void searchSrwEndpoint() throws Exception {
    String fullName = context.getFullName("an item");

    SearchRetrieveResponseType response = searchSRW(fullName);
    Assert.assertTrue(response.getNumberOfRecords().intValue() == 0);

    logon("AutoTest", "automated");
    WizardPageTab wiz = new ContributePage(context).load().openWizard("SOAP and Harvesting");

    wiz.editbox(1, fullName);
    wiz.save().publish();

    response = searchSRW(fullName);
    Assert.assertTrue(response.getNumberOfRecords().intValue() == 1);

    RecordType record = response.getRecords().getRecord(0);
    PropBagEx itemXml = new PropBagEx(record.getRecordData().get_any()[0].getAsDOM());

    Assert.assertTrue(itemXml.getNode("item/name").equals(fullName));
  }

  private SearchRetrieveResponseType searchSRW(String qs) throws Exception {
    SRWSampleServiceLocator service = new SRWSampleServiceLocator();
    URL url = new URL(context.getBaseUrl() + "srw/");
    SRWPort port = service.getSRW(url);

    SearchRetrieveRequestType request = new SearchRetrieveRequestType();
    request.setVersion("1.1");
    request.setQuery('"' + qs + '"');

    request.setStartRecord(new PositiveInteger("1"));
    request.setMaximumRecords(new NonNegativeInteger("10"));
    request.setRecordPacking("xml");
    request.setRecordSchema("tle");

    SearchRetrieveResponseType response = port.searchRetrieveOperation(request);

    return response;
  }
}
