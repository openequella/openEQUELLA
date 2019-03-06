package com.tle.webtests.pageobject.wizard.controls;

import static org.testng.Assert.assertTrue;

import java.util.Collection;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.testng.annotations.BeforeClass;
import org.w3c.dom.Element;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.soap.SoapService50;
import com.tle.webtests.test.AbstractCleanupTest;

public abstract class AbstractWizardControlsTest extends AbstractCleanupTest
{
	protected SoapService50 soap;

	@BeforeClass
	public void setUp() throws Exception
	{
		SoapHelper soapHelper = new SoapHelper(context);
		soap = soapHelper.createSoap(SoapService50.class, "services/SoapService50", "http://soap.remoting.web.tle.com",
			null);
		setDeleteCredentials("AutoTest", "automated");
	}

	protected void checkExists(XPath xpath, String expr, Element rootElement) throws XPathExpressionException
	{
		Object node = xpath.evaluate(expr, rootElement, XPathConstants.NODE);
		assertTrue(node != null);
	}

	protected void assertEquals(PropBagEx xml, String path, Object expected)
	{
		Object actual = expected instanceof Collection ? xml.getNodeList(path) : xml.getNode(path);
		if( !actual.equals(expected) )
		{
			throw new AssertionError("Expected '" + expected + "' at path '" + path + "' but got '" + actual
				+ "' full xml is:" + xml);
		}
	}

}
