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

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name: Md5.java

package com.dytech.devlib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@SuppressWarnings("nls")
public class Md5 {
  public static String stringify(byte buf[]) {
    StringBuilder sb = new StringBuilder(2 * buf.length);
    for (int i = 0; i < buf.length; i++) {
      int h = (buf[i] & 0xf0) >> 4;
      int l = buf[i] & 0xf;
      sb.append(Character.valueOf((char) (h <= 9 ? 48 + h : (97 + h) - 10)));
      sb.append(Character.valueOf((char) (l <= 9 ? 48 + l : (97 + l) - 10)));
    }

    return sb.toString();
  }

  private int f(int x, int y, int z) {
    return x & y | ~x & z;
  }

  private int g(int x, int y, int z) {
    return x & z | y & ~z;
  }

  private int h(int x, int y, int z) {
    return x ^ y ^ z;
  }

  private int i(int x, int y, int z) {
    return y ^ (x | ~z);
  }

  private int rotateLeft(int x, int n) {
    return x << n | x >>> 32 - n;
  }

  private int ff(int a, int b, int c, int d, int x, int s, int ac) {
    a += f(b, c, d) + x + ac;
    a = rotateLeft(a, s);
    a += b;
    return a;
  }

  private int gg(int a, int b, int c, int d, int x, int s, int ac) {
    a += g(b, c, d) + x + ac;
    a = rotateLeft(a, s);
    a += b;
    return a;
  }

  private int hh(int a, int b, int c, int d, int x, int s, int ac) {
    a += h(b, c, d) + x + ac;
    a = rotateLeft(a, s);
    a += b;
    return a;
  }

  private int ii(int a, int b, int c, int d, int x, int s, int ac) {
    a += i(b, c, d) + x + ac;
    a = rotateLeft(a, s);
    a += b;
    return a;
  }

  private void decode(int output[], byte input[], int off, int len) {
    int i = 0;
    for (int j = 0; j < len; j += 4) {
      output[i] =
          input[off + j] & 0xff
              | (input[off + j + 1] & 0xff) << 8
              | (input[off + j + 2] & 0xff) << 16
              | (input[off + j + 3] & 0xff) << 24;
      i++;
    }
  }

  private void transform(byte block[], int offset) {
    int a = state[0];
    int b = state[1];
    int c = state[2];
    int d = state[3];
    int x[] = new int[16];
    decode(x, block, offset, 64);
    a = ff(a, b, c, d, x[0], 7, 0xd76aa478);
    d = ff(d, a, b, c, x[1], 12, 0xe8c7b756);
    c = ff(c, d, a, b, x[2], 17, 0x242070db);
    b = ff(b, c, d, a, x[3], 22, 0xc1bdceee);
    a = ff(a, b, c, d, x[4], 7, 0xf57c0faf);
    d = ff(d, a, b, c, x[5], 12, 0x4787c62a);
    c = ff(c, d, a, b, x[6], 17, 0xa8304613);
    b = ff(b, c, d, a, x[7], 22, 0xfd469501);
    a = ff(a, b, c, d, x[8], 7, 0x698098d8);
    d = ff(d, a, b, c, x[9], 12, 0x8b44f7af);
    c = ff(c, d, a, b, x[10], 17, -42063);
    b = ff(b, c, d, a, x[11], 22, 0x895cd7be);
    a = ff(a, b, c, d, x[12], 7, 0x6b901122);
    d = ff(d, a, b, c, x[13], 12, 0xfd987193);
    c = ff(c, d, a, b, x[14], 17, 0xa679438e);
    b = ff(b, c, d, a, x[15], 22, 0x49b40821);
    a = gg(a, b, c, d, x[1], 5, 0xf61e2562);
    d = gg(d, a, b, c, x[6], 9, 0xc040b340);
    c = gg(c, d, a, b, x[11], 14, 0x265e5a51);
    b = gg(b, c, d, a, x[0], 20, 0xe9b6c7aa);
    a = gg(a, b, c, d, x[5], 5, 0xd62f105d);
    d = gg(d, a, b, c, x[10], 9, 0x2441453);
    c = gg(c, d, a, b, x[15], 14, 0xd8a1e681);
    b = gg(b, c, d, a, x[4], 20, 0xe7d3fbc8);
    a = gg(a, b, c, d, x[9], 5, 0x21e1cde6);
    d = gg(d, a, b, c, x[14], 9, 0xc33707d6);
    c = gg(c, d, a, b, x[3], 14, 0xf4d50d87);
    b = gg(b, c, d, a, x[8], 20, 0x455a14ed);
    a = gg(a, b, c, d, x[13], 5, 0xa9e3e905);
    d = gg(d, a, b, c, x[2], 9, 0xfcefa3f8);
    c = gg(c, d, a, b, x[7], 14, 0x676f02d9);
    b = gg(b, c, d, a, x[12], 20, 0x8d2a4c8a);
    a = hh(a, b, c, d, x[5], 4, 0xfffa3942);
    d = hh(d, a, b, c, x[8], 11, 0x8771f681);
    c = hh(c, d, a, b, x[11], 16, 0x6d9d6122);
    b = hh(b, c, d, a, x[14], 23, 0xfde5380c);
    a = hh(a, b, c, d, x[1], 4, 0xa4beea44);
    d = hh(d, a, b, c, x[4], 11, 0x4bdecfa9);
    c = hh(c, d, a, b, x[7], 16, 0xf6bb4b60);
    b = hh(b, c, d, a, x[10], 23, 0xbebfbc70);
    a = hh(a, b, c, d, x[13], 4, 0x289b7ec6);
    d = hh(d, a, b, c, x[0], 11, 0xeaa127fa);
    c = hh(c, d, a, b, x[3], 16, 0xd4ef3085);
    b = hh(b, c, d, a, x[6], 23, 0x4881d05);
    a = hh(a, b, c, d, x[9], 4, 0xd9d4d039);
    d = hh(d, a, b, c, x[12], 11, 0xe6db99e5);
    c = hh(c, d, a, b, x[15], 16, 0x1fa27cf8);
    b = hh(b, c, d, a, x[2], 23, 0xc4ac5665);
    a = ii(a, b, c, d, x[0], 6, 0xf4292244);
    d = ii(d, a, b, c, x[7], 10, 0x432aff97);
    c = ii(c, d, a, b, x[14], 15, 0xab9423a7);
    b = ii(b, c, d, a, x[5], 21, 0xfc93a039);
    a = ii(a, b, c, d, x[12], 6, 0x655b59c3);
    d = ii(d, a, b, c, x[3], 10, 0x8f0ccc92);
    c = ii(c, d, a, b, x[10], 15, 0xffeff47d);
    b = ii(b, c, d, a, x[1], 21, 0x85845dd1);
    a = ii(a, b, c, d, x[8], 6, 0x6fa87e4f);
    d = ii(d, a, b, c, x[15], 10, 0xfe2ce6e0);
    c = ii(c, d, a, b, x[6], 15, 0xa3014314);
    b = ii(b, c, d, a, x[13], 21, 0x4e0811a1);
    a = ii(a, b, c, d, x[4], 6, 0xf7537e82);
    d = ii(d, a, b, c, x[11], 10, 0xbd3af235);
    c = ii(c, d, a, b, x[2], 15, 0x2ad7d2bb);
    b = ii(b, c, d, a, x[9], 21, 0xeb86d391);
    state[0] += a;
    state[1] += b;
    state[2] += c;
    state[3] += d;
  }

  public final void update(byte input[], int len) {
    int index = (int) (count >> 3) & 0x3f;
    count += len << 3;
    int partLen = 64 - index;
    int i = 0;
    if (len >= partLen) {
      System.arraycopy(input, 0, buffer, index, partLen);
      transform(buffer, 0);
      for (i = partLen; i + 63 < len; i += 64) {
        transform(input, i);
      }
      index = 0;
    } else {
      i = 0;
    }
    System.arraycopy(input, i, buffer, index, len - i);
  }

  public byte[] end() {
    byte bits[] = new byte[8];
    for (int i = 0; i < 8; i++) {
      bits[i] = (byte) (int) (count >>> i * 8 & 255L);
    }

    int index = (int) (count >> 3) & 0x3f;
    int padlen = index >= 56 ? 120 - index : 56 - index;
    update(padding, padlen);
    update(bits, 8);
    return encode(state, 16);
  }

  private byte[] encode(int input[], int len) {
    byte output[] = new byte[len];
    int i = 0;
    for (int j = 0; j < len; j += 4) {
      output[j] = (byte) (input[i] & 0xff);
      output[j + 1] = (byte) (input[i] >> 8 & 0xff);
      output[j + 2] = (byte) (input[i] >> 16 & 0xff);
      output[j + 3] = (byte) (input[i] >> 24 & 0xff);
      i++;
    }

    return output;
  }

  public byte[] getDigest() throws IOException {
    byte buffer[] = new byte[1024];
    int got = -1;
    if (digest != null) {
      return digest;
    }
    for (got = in.read(buffer); got > 0; got = in.read(buffer)) {
      update(buffer, got);
    }
    digest = end();
    return digest;
  }

  public byte[] processString() {
    if (!stringp) {
      throw new RuntimeException(getClass().getName() + "[processString]" + " not a string.");
    }
    try {
      return getDigest();
    } catch (IOException e) {
      throw new RuntimeException(
          getClass().getName() + "[processString]" + ": implementation error.");
    }
  }

  public String getStringDigest() {
    if (digest == null) {
      try {
        getDigest();
      } catch (IOException ioe) {
        throw new RuntimeException("IO error");
      }
    }
    return stringify(digest);
  }

  public Md5() {
    in = null;
    stringp = false;
    state = null;
    count = 0L;
    buffer = null;
    digest = null;
    init();
  }

  public Md5(String input, String enc) {
    in = null;
    stringp = false;
    state = null;
    count = 0L;
    buffer = null;
    digest = null;
    byte bytes[] = null;
    try {
      bytes = input.getBytes(enc);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("no " + enc + " encoding!!!");
    }
    initStream(new ByteArrayInputStream(bytes));
    stringp = true;
  }

  public Md5(byte bytes[]) {
    this(new ByteArrayInputStream(bytes));
  }

  public Md5(String input) {
    this(input, "UTF8");
  }

  public Md5(InputStream in) {
    this.in = null;
    stringp = false;
    state = null;
    count = 0L;
    buffer = null;
    digest = null;
    stringp = false;
    initStream(in);
  }

  public void initStream(InputStream in) {
    this.in = in;
    init();
  }

  public void init() {
    state = new int[4];
    buffer = new byte[64];
    count = 0L;
    state[0] = 0x67452301;
    state[1] = 0xefcdab89;
    state[2] = 0x98badcfe;
    state[3] = 0x10325476;
  }

  public static void main(String args[]) throws IOException {
    if (args.length != 1) {
      System.out.println("Md5 <file>");
      System.exit(1);
    }
    Md5 md5 = new Md5(new FileInputStream(new File(args[0])));
    byte b[] = md5.getDigest();
    System.out.println(stringify(b));
  }

  private static byte padding[] = {
    -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0
  };
  private InputStream in;
  private boolean stringp;
  private int state[];
  private long count;
  private byte buffer[];
  private byte digest[];
}
