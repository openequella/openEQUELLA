package com.dytech.devlib;

import java.io.StringWriter;
import junit.framework.TestCase;

@SuppressWarnings("nls")
public class BadCharacterFilterWriterTest extends TestCase {
  private static final String PLAIN = "ABCD\u0009EFGHIJKLMNOPQRSTUVWXYZ0123456789";

  public void testUnicodeGoodAndBad() throws Exception {
    final StringWriter stringWriter = new StringWriter();
    final BadCharacterFilterWriter wtr = new BadCharacterFilterWriter(stringWriter);
    wtr.write("ABCD\u0009EFGH\u0011IJKLMNOPQRSTUVWXYZ0123456789");
    final String result = stringWriter.toString();

    assertTrue(result, result.equals(PLAIN));
    assertTrue("Didn't discard!", wtr.didDiscard());

    wtr.close();
  }
}
