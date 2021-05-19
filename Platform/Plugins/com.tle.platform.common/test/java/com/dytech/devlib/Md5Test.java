package com.dytech.devlib;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class Md5Test {

  @Test
  public void simpleDigestGeneration() {
    assertEquals(
        "Incorrect digest returned",
        "702edca0b2181c15d457eacac39de39b",
        new Md5("This is a test!").getStringDigest());
  }

  /**
   * The second most used function of this class, that arguably shouldn't even be specifically in an
   * MD5 class.
   */
  @Test
  public void stringifyBytes() {
    assertEquals(
        "Incorrect string representation of bytes",
        "205f2d4142432d5f20",
        Md5.stringify(" _-ABC-_ ".getBytes(StandardCharsets.UTF_8)));
  }
}
