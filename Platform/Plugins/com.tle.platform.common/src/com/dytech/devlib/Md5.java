/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.devlib;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * A simple class wrapping MD5 for ease of use.
 *
 * <p>It has been around for a long time and originally seems to have simply been from decompiling
 * some other implementation. That decompiled code was removed in 2021 and now simply wraps standard
 * implementations.
 */
public class Md5 {

  private InputStream in;
  private byte[] digest;

  /**
   * Simple converts an array of bytes to its hexadecimal string representation. Technically has
   * nothing to do with MD5 and is just a convenience function.
   *
   * @param buf The array to convert to a hexadecimal string
   * @return a hexadecimal string representation of {@code buf}
   */
  public static String stringify(byte[] buf) {
    return Hex.encodeHexString(buf);
  }

  /**
   * Get an MD5 hash of the value provided during instantiation. Will only calculate the first time
   * called.
   *
   * @return a byte array representation of the resulting hash.
   * @throws IOException if issues reading provided input from class instantiation
   */
  public byte[] getDigest() throws IOException {
    if (digest == null) {
      digest = new DigestUtils(MD5).digest(in);
    }
    return digest;
  }

  /**
   * Uses the digest created by {@link #getDigest}, but returns a hexadecimal string representation.
   *
   * @return hexadecimal string representation of the resultant MD5 hash
   */
  public String getStringDigest() {
    if (digest == null) {
      try {
        getDigest();
      } catch (IOException ioe) {
        throw new RuntimeException(
            "Error when attempting to read the provided input: " + ioe.getMessage());
      }
    }
    return stringify(digest);
  }

  /**
   * Instantiates with the provided {@code input} which will be parsed to bytes using the specified
   * string encoding ({@code enc}).
   *
   * @param input A string to be hashed
   * @param enc The encoding of the provided string.
   */
  public Md5(String input, String enc) {
    byte[] bytes;
    try {
      bytes = input.getBytes(enc);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Specified encoding method (" + enc + ") is not supported.");
    }

    setIn(new ByteArrayInputStream(bytes));
  }

  /**
   * Instantiates with the provided byte array ready for hashing.
   *
   * @param bytes the bytes to be hashed.
   */
  public Md5(byte[] bytes) {
    this(new ByteArrayInputStream(bytes));
  }

  /**
   * Instantiates with the provided {@code input} which will be parsed to bytes using the specified
   * UTF-8 string encoding.
   *
   * @param input A string to be hashed
   */
  public Md5(String input) {
    this(input, "UTF8");
  }

  /**
   * Instantiates with the provided {@code InputStream} ready for hashing.
   *
   * @param in the stream to be hashed.
   */
  public Md5(InputStream in) {
    setIn(in);
  }

  private void setIn(InputStream in) {
    digest = null;
    this.in = in;
  }

  /**
   * It's unclear if there's a need for this class to be runnable, or whether this just came across
   * with the decompilation of the original implementation back in the day. For now, keeping just
   * for good measure.
   */
  public static void main(String args[]) throws IOException {
    if (args.length != 1) {
      System.out.println("Md5 <file>");
      System.exit(1);
    }
    Md5 md5 = new Md5(new FileInputStream(args[0]));
    System.out.println(stringify(md5.getDigest()));
  }
}
