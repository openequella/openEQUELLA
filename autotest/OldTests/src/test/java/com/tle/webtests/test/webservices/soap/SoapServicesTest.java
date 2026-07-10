package com.tle.webtests.test.webservices.soap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.framework.soap.SoapHarvesterService;
import com.tle.webtests.framework.soap.SoapInterfaceV2;
import com.tle.webtests.test.AbstractTest;
import javax.servlet.http.HttpServletResponse;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.transport.http.HTTPException;
import org.testng.annotations.Test;

/**
 * This test is deprecated in 2026.1, alongside the deprecation of the entire SOAP Service. The test
 * cases have been updated to make sure only the Harvester Service is still available.
 *
 * <p>Note: this test requires the optional config 'soapapi.enabled' to be `false`.
 */
@TestInstitution("vanilla")
@Deprecated
public class SoapServicesTest extends AbstractTest {
  private SoapInterfaceV2 soapService;
  private SoapHarvesterService harvesterService;

  @Test(description = "SOAP services are no longer available.")
  public void soapServiceNotAvailable() throws Exception {
    SoapHelper soapHelper = new SoapHelper(context);
    soapService =
        soapHelper.createSoap(
            SoapInterfaceV2.class,
            "services/SoapInterfaceV2",
            "http://remoting.core.tle.com",
            null);

    Fault fault = expectThrows(Fault.class, () -> soapService.login("AutoTest", "automated"));

    assertTrue(fault.getCause() instanceof HTTPException);

    HTTPException exception = (HTTPException) fault.getCause();
    assertEquals(exception.getResponseCode(), HttpServletResponse.SC_NOT_FOUND);
  }

  @Test(
      description =
          "The SoapHarvesterService remains available even when the legacy SOAP API is disabled.")
  public void soapHarvesterServiceStillAvailable() throws Exception {
    SoapHelper soapHelper = new SoapHelper(context);
    harvesterService =
        soapHelper.createSoap(
            SoapHarvesterService.class,
            "services/SoapHarvesterService",
            "http://soap.harvester.core.tle.com",
            null);

    String userXml = harvesterService.login("AutoTest", "automated");

    assertTrue(userXml.contains("AutoTest"));
  }
}
