package com.tle.webtests.pageobject.wizard.controls;

import static org.testng.Assert.assertNotNull;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.test.webservices.rest.AbstractItemApiTest;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;

public abstract class AbstractWizardControlsTest extends AbstractItemApiTest {

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<>(getClass().getSimpleName(), "AutoTest"));
  }

  protected void checkExists(XPath xpath, String expr, Element rootElement)
      throws XPathExpressionException {
    Object node = xpath.evaluate(expr, rootElement, XPathConstants.NODE);
    assertNotNull(node);
  }

  protected void assertEquals(PropBagEx xml, String path, Object expected) {
    Object actual = expected instanceof Collection ? xml.getNodeList(path) : xml.getNode(path);
    if (!actual.equals(expected)) {
      throw new AssertionError(
          "Expected '"
              + expected
              + "' at path '"
              + path
              + "' but got '"
              + actual
              + "' full xml is:"
              + xml);
    }
  }

  protected PropBagEx getItemXml(ItemId itemId) throws IOException {
    String token = getToken();
    ObjectNode itemInfo = getItem(itemId.getUuid(), itemId.getVersion(), "all", token);

    return new PropBagEx(itemInfo.get("metadata").asText());
  }
}
