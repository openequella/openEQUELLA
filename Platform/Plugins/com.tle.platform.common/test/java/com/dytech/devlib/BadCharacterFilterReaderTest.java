package com.dytech.devlib;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.TestCase;

@SuppressWarnings("nls")
public class BadCharacterFilterReaderTest extends TestCase {
  private static final String PLAIN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final String PLAIN_BIG =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789GHGHGHGHGJGHHH"
          + "GGHGHGGHGHGHJGHDSFDGFASAAGSHGAHGSAGSSGHASGSGHAGS"
          + "GHSGASGHAGSASGSGASGSAGSGASASGHASASASG9999990";

  public void testReadToEnd() throws Exception {
    exec(PLAIN);
  }

  public void testReadBigToEnd() throws Exception {
    exec(PLAIN_BIG);
  }

  public void testReadPotentialSuspect() throws Exception {
    exec("ABCDEFG&amp;HIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadMultiplePotentialSuspect() throws Exception {
    exec("&amp;ABC&amp;DEFG&amp;&amp;HIJKLM&amp;NOPQRSTUVWXYZ0123456789&amp;");
  }

  public void testReadPotentialSuspectHalfFinishedSuspectBorder() throws Exception {
    exec("ABCDEFGHI&somesuspect;JKLMNOPQRSTUVWXYZ0123456789", 11);
  }

  public void testReadPotentialSuspectContainingAND() throws Exception {
    exec("ABCD&so&mesuspect;EFGHIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadContainingSEMICOLONS() throws Exception {
    exec("ABCD;EFGHIJKLMNOPQRST;UVWXYZ0123456789");
  }

  public void testReadPotentialSuspectUnbounded() throws Exception {
    exec("ABCDEFG&ampHIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadInvalidHexEncoded() throws Exception {
    exec("ABCDEFG&#x0B;HIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true);
  }

  public void testReadInvalidHexEncodedAtStart() throws Exception {
    exec("&#x0B;ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true);
  }

  public void testReadInvalidHexEncodedAtEnd() throws Exception {
    exec("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789&#x0B;", PLAIN, true);
  }

  public void testReadValidHexEncoded() throws Exception {
    exec("ABCDEFG&#x09;HIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadInvalidHexEncodedMiniBuffer() throws Exception {
    exec("ABCDEFG&#x0B;HIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true, 11);
  }

  public void testReadInvalidHexEncodedMaxiBuffer() throws Exception {
    exec("ABCDEFG&#x0B;HIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true, 10000);
  }

  public void testReadInvalidIntEncoded() throws Exception {
    exec("ABCDEFG&#011;HIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true);
  }

  public void testReadInvalidIntEncodedAtStart() throws Exception {
    exec("&#011;ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true);
  }

  public void testReadInvalidIntEncodedAtEnd() throws Exception {
    exec("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789&#011;", PLAIN, true);
  }

  public void testReadValidIntEncoded() throws Exception {
    exec("ABCDEFG&#009;HIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadInvalidUnicodeChar() throws Exception {
    exec("ABCDEFG\u0011HIJKLMNOPQRSTUVWXYZ0123456789", PLAIN, true);
  }

  public void testReadValidUnicodeChar() throws Exception {
    exec("ABCDEFG\u0009HIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadValidAndInvalidUnicodeChar() throws Exception {
    exec(
        "\u0011\u0009ABCDEFG\u0009\u0011HIJKLMNOPQRSTUVWX\u0011YZ0123456789",
        "\u0009ABCDEFG\u0009HIJKLMNOPQRSTUVWXYZ0123456789",
        true);
  }

  public void testReadImpossiblyShortEscapeSeq() throws Exception {
    exec("ABCDEFG&;HIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadImpossiblyShortEscapeSeq2() throws Exception {
    exec("ABCDEFG&#;HIJKLMNOPQRSTUVWXYZ0123456789");
  }

  public void testReadKnownProblemFile() throws Exception {
    doReal("real.xml");
  }

  public void testReadKnownProblemFile2() throws Exception {
    doReal("real2.xml");
  }

  public void testReadKnownProblemFileViaPropBag() throws Exception {
    Reader is = null;
    try {
      is =
          new InputStreamReader(
              BadCharacterFilterReaderTest.class.getResourceAsStream("real2.xml"), "UTF-8");
      PropBagEx pbag = new PropBagEx(is);
      assertTrue("Error creating prob bag", pbag.toString().length() > 0);
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  private void doReal(String realFilename) throws Exception {
    Reader is = null;
    BadCharacterFilterReader rdr = null;
    try {
      final char[] cbuf = new char[100];
      is =
          new InputStreamReader(
              BadCharacterFilterReaderTest.class.getResourceAsStream(realFilename), "UTF-8");

      final StringBuilder original = new StringBuilder();
      int read = is.read(cbuf);
      while (read >= 0) {
        original.append(cbuf, 0, read);
        read = is.read(cbuf);
      }

      rdr =
          new BadCharacterFilterReader(
              new InputStreamReader(
                  BadCharacterFilterReaderTest.class.getResourceAsStream(realFilename), "UTF-8"));
      final StringBuilder result = new StringBuilder();
      read = rdr.read(cbuf);
      while (read >= 0) {
        result.append(cbuf, 0, read);
        read = rdr.read(cbuf);
      }

      assertTrue(result.toString(), result.toString().equals(original.toString()));
    } finally {
      if (is != null) {
        is.close();
      }
      if (rdr != null) {
        rdr.close();
      }
    }
  }

  private void exec(final String originalAndExpected) throws Exception {
    exec(originalAndExpected, originalAndExpected, false);
  }

  private void exec(final String originalAndExpected, final int bufferSize) throws Exception {
    exec(originalAndExpected, originalAndExpected, false, bufferSize);
  }

  private void exec(final String original, final String expected, final boolean expectDiscard)
      throws Exception {
    final StringReader str = new StringReader(original);
    final BadCharacterFilterReader rdr = new BadCharacterFilterReader(str);

    final String result = read(rdr);
    assertTrue(result, result.equals(expected));
    assertTrue(
        (expectDiscard ? "No discard!" : "Discarded when shouldn't!"),
        rdr.didDiscard() == expectDiscard);
  }

  private void exec(
      final String original,
      final String expected,
      final boolean expectDiscard,
      final int bufferSize)
      throws Exception {
    final StringReader str = new StringReader(original);
    final BadCharacterFilterReader rdr = new BadCharacterFilterReader(str, bufferSize);

    final String result = read(rdr);
    assertTrue(result, result.equals(expected));
    assertTrue("No discard!", rdr.didDiscard() == expectDiscard);
  }

  private String read(final BadCharacterFilterReader rdr) throws Exception {
    final StringBuilder result = new StringBuilder();
    final char[] cbuf = new char[100];
    int read = rdr.read(cbuf);
    while (read >= 0) {
      result.append(cbuf, 0, read);
      read = rdr.read(cbuf);
    }
    return result.toString();
  }
}
