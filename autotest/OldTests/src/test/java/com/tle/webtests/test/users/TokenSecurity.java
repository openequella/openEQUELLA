/*
 * Created on Apr 18, 2005 For "The Learning Edge"
 */
package com.tle.webtests.test.users;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class TokenSecurity {
  public static long timedifmax = 30 * TimeUnit.MINUTES.toMillis(1);

  public static void setTimeDifferenceMax(long minutes) {
    timedifmax = minutes * TimeUnit.MINUTES.toMillis(minutes);
  }

  public static void main(String[] args) throws Exception {
    System.err.println(
        URLEncoder.encode(
            TokenSecurity.createSecureToken("TLELDAP", null, "secret", null), "UTF-8"));
  }

  public static String createSecureToken(String token, String id, String sharedSecret, String data)
      throws IOException {
    return createSecureToken(token, id, sharedSecret, data, System.currentTimeMillis());
  }

  public static String createSecureToken(
      String token, String id, String sharedSecret, String data, long curTime) throws IOException {
    if (id == null) {
      id = "";
    }

    String time = Long.toString(curTime);
    String toMd5 = token + id + time + sharedSecret;

    StringBuilder b = new StringBuilder();
    b.append(URLEncoder.encode(token, "UTF-8"));
    b.append(':');
    b.append(URLEncoder.encode(id, "UTF-8"));
    b.append(':');
    b.append(time);
    b.append(':');
    b.append(Base64.getEncoder().encodeToString(getMd5Bytes(toMd5)));
    if (data != null && data.length() > 0) {
      b.append(':');
      b.append(data);
    }
    return b.toString();
  }

  public static byte[] getMd5Bytes(String str) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
      digest.update(str.getBytes("UTF-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return digest.digest();
  }

  public static class Token {
    private String insecure;
    private String id;
    private long time;
    private String base64;
    private String data;

    public Token(String insecure, String id, long time, String base64, String data) {
      super();
      this.insecure = insecure;
      this.id = id;
      this.time = time;
      this.base64 = base64;
      this.data = data;
    }

    public String getInsecure() {
      return insecure;
    }

    public void setInsecure(String insecure) {
      this.insecure = insecure;
    }

    public String getBase64() {
      return base64;
    }

    public void setBase64(String base64) {
      this.base64 = base64;
    }

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }
  }
}
