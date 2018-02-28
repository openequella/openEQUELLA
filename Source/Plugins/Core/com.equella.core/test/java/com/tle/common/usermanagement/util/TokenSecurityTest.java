/*
 * Created on May 11, 2005
 */
package com.tle.common.usermanagement.util;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;

import com.dytech.devlib.Md5;
import com.tle.common.usermanagement.util.TokenSecurity.Token;
import com.tle.common.util.TokenGenerator;

import junit.framework.TestCase;

public class TokenSecurityTest extends TestCase
{
	private static final String SHARED_SECRET = "abcdefd";
	private static final String MESSAGE = "abc:$";
	private static final String TOKEN = "def:%";
	private static final String ID = "test:";

	private String stoken;
	private Token data;
	private String failureMessage;

	@Override
	protected void setUp() throws Exception
	{
		stoken = TokenGenerator.createSecureToken(TOKEN, ID, SHARED_SECRET, MESSAGE);
		data = TokenSecurity.getInsecureToken(stoken);
		failureMessage = "Token was " + stoken;
	}

	@Override
	protected void tearDown() throws Exception
	{
		stoken = null;
		data = null;
		failureMessage = null;
	}

	public void testIsSecureToken() throws IOException
	{
		assertTrue(failureMessage, TokenSecurity.isSecureToken(data, SHARED_SECRET));
	}

	public void testTokenCorrect() throws IOException
	{
		assertEquals(failureMessage, TOKEN, data.getInsecure());
	}

	public void testIDCorrect() throws IOException
	{
		assertEquals(failureMessage, ID, data.getId());
	}

	public void testMessageCorrect() throws IOException
	{
		assertEquals(failureMessage, MESSAGE, data.getData());
	}

	public void testOldStyleToken() throws IOException
	{
		String time = Long.toString(System.currentTimeMillis());
		String username = "jolse";
		String toMd5 = username + time + "secret";
		byte[] bytes = new Md5(toMd5).getDigest();

		StringBuilder b = new StringBuilder();
		b.append(URLEncoder.encode(username, "UTF-8"));
		b.append(':');
		b.append(time);
		b.append(':');
		b.append(new String(Base64.encodeBase64(bytes), "ASCII"));

		Token insecureToken = TokenSecurity.getInsecureToken(b.toString());
		assertEquals(insecureToken.getInsecure(), username);
		assertEquals(TokenSecurity.isSecureToken(insecureToken, "secret"), true);
	}

	public void testDontDie() throws IOException
	{
		Token insecureToken = TokenSecurity.getInsecureToken("CAS:sfasfsdf:sdas");
		assert (insecureToken == null);
	}
}
