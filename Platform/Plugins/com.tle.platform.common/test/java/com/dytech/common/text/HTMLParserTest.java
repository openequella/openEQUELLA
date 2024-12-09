/*
 * Created on Nov 5, 2004
 */
package com.dytech.common.text;

import com.dytech.common.text.HTMLParser.Tag;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import junit.framework.TestCase;

@SuppressWarnings("nls")
public class HTMLParserTest extends TestCase {
  public HTMLParserTest() {
    super();
  }

  public void testHtml1Strict() throws Exception {
    parseHtmlFile("html-1-strict.txt", new Html1Tester());
  }

  public void testHtml1Dogdy() throws Exception {
    parseHtmlFile("html-1-dodgy.txt", new Html1Tester());
  }

  public void testHtml2Searching() throws Exception {
    parseHtmlFile(
        "html-2.txt",
        new ParserTester() {
          @Override
          public void parse(HTMLParser parser) throws Exception {
            for (int i = 1; i <= 5; i++) {
              Tag tag = parser.getNextTagForName("findme");
              assertEquals(tag.getAttribute("value"), Integer.toString(i));
            }

            assertNull(parser.getNextTagForName("findme"));
          }
        });
  }

  public void testHtmlInvalid1() throws Exception {
    try {
      parseHtmlFile("html-invalid-1.txt", null);
      assertTrue("The HTML should not have parsed correctly", false);
    } catch (ParseException ex) {
      // This is supposed to happen
    }
  }

  public void testHtmlInvalid2() throws Exception {
    try {
      parseHtmlFile("html-invalid-2.txt", null);
      assertTrue("The HTML should not have parsed correctly", false);
    } catch (ParseException ex) {
      // This is supposed to happen
    }
  }

  public void testHtmlInvalid3() throws Exception {
    try {
      parseHtmlFile("html-invalid-3.txt", null);
      assertTrue("The HTML should not have parsed correctly", false);
    } catch (ParseException ex) {
      // This is supposed to happen
    }
  }

  // // HELPER METHODS //////////////////////////////////////////////////////

  public void parseHtmlFile(String resource, ParserTester parserTest) throws Exception {
    try (InputStream in = HTMLParserTest.class.getResourceAsStream(resource)) {
      if (in == null) {
        throw new FileNotFoundException("Could not find resouce: " + resource);
      }

      HTMLParser parser = new HTMLParser(new InputStreamReader(in));
      if (parserTest == null) {
        parserTest = new DefaultParserTester();
      }
      parserTest.parse(parser);
    }
  }

  private interface ParserTester {
    void parse(HTMLParser parser) throws Exception;
  }

  private static final class DefaultParserTester implements ParserTester {
    @Override
    public void parse(HTMLParser parser) throws Exception {
      // Just read through the whole stream to make sure it parses
      Tag tag = null;
      do {
        tag = parser.getNextTag();
      } while (tag != null);
    }
  }

  private static final class Html1Tester implements ParserTester {
    @Override
    public void parse(HTMLParser parser) throws Exception {
      Tag tag = parser.getNextTag();
      assertTag(tag, "html", 0, false);

      tag = parser.getNextTag();
      assertTag(tag, "head", 0, false);

      tag = parser.getNextTag();
      assertTag(tag, "title", 0, false);

      tag = parser.getNextTag();
      assertTag(tag, "meta", 2, true);
      assertEquals(tag.getAttribute("http-equiv"), "metaname1");
      assertEquals(tag.getAttribute("value"), "metavalue1");

      tag = parser.getNextTag();
      assertTag(tag, "meta", 2, true);
      assertEquals(tag.getAttribute("http-equiv"), "metaname2");
      assertEquals(tag.getAttribute("value"), "metavalue2");

      tag = parser.getNextTag();
      assertTag(tag, "body", 1, false);
      assertEquals(tag.getAttribute("bgcolor"), "black");

      tag = parser.getNextTag();
      assertTag(tag, "ns:p", 1, false);
      assertEquals(tag.getAttribute("nowrap"), "true");

      tag = parser.getNextTag();
      assertTag(tag, "b", 0, false);

      tag = parser.getNextTag();
      assertTag(tag, "a", 1, true);
      assertEquals(tag.getAttribute("href"), "http://fakeurl");

      tag = parser.getNextTag();
      assertNull(tag);
      tag = parser.getNextTag();
      assertNull(tag);
    }

    private void assertTag(Tag tag, String name, int attributeCount, boolean selfClosing) {
      assertEquals(tag.getName(), name);
      assertEquals(tag.isSelfClosing(), selfClosing);
      assertEquals(tag.getAttribues().size(), attributeCount);
    }
  }
}
