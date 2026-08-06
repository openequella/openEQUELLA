/*
 * Created on Jul 7, 2005
 */
package com.tle.core.xstream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLCompareTest {
  private XMLCompare comparer;

  @BeforeEach
  void setUp() {
    comparer = new XMLCompare();
    comparer.setTrimTextValues(true);
  }

  @Test
  public void testUnordered() throws Exception {
    int testNumber = 1;
    String baseName = baseName(testNumber);
    Document base = getDocument(baseName);
    assertNotNull(base, "Could not find " + baseName);

    while (base != null) {
      compareAgainstVariations(base, "good", testNumber, true);
      compareAgainstVariations(base, "bad", testNumber, false);

      testNumber++;
      baseName = baseName(testNumber);
      base = getDocument(baseName);
    }
  }

  /**
   * Compares the base document against every numbered variation of the given kind, of which there
   * may be none, asserting that each one either matches or differs as its name says it should.
   */
  private void compareAgainstVariations(
      Document base, String kind, int testNumber, boolean shouldMatch) throws Exception {
    int varNumber = 1;
    Document variation;
    do {
      String varName = "unordered/" + kind + "-" + varNumber + "-for-" + testNumber + ".xml";
      variation = getDocument(varName);
      if (variation != null) {
        boolean matched = comparer.compare(base, variation);
        if (shouldMatch) {
          assertTrue(matched, varName + " should have matched " + baseName(testNumber));
        } else {
          assertFalse(matched, varName + " should have differed from " + baseName(testNumber));
        }
      }
      varNumber++;
    } while (variation != null);
  }

  private static String baseName(int testNumber) {
    return "unordered/base-" + testNumber + ".xml";
  }

  private Document getDocument(String filename)
      throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
    try (InputStream in = XMLCompareTest.class.getResourceAsStream("/xmlcompare/" + filename)) {
      if (in == null) {
        return null;
      } else {
        return XMLCompare.getDocument(new InputStreamReader(in, "UTF-8"));
      }
    }
  }
}
