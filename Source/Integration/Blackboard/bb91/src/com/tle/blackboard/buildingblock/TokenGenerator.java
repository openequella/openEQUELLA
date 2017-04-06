package com.tle.blackboard.buildingblock;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

@SuppressWarnings("nls")
public class TokenGenerator
{
	public static String createSecureToken(String token, String id, String sharedSecret, String data)
		throws IOException
	{
		return createSecureToken(token, id, sharedSecret, data, System.currentTimeMillis());
	}

	public static String createSecureToken(String token, String id, String sharedSecret, String data, long curTime)
		throws IOException
	{
		if( id == null )
		{
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
		b.append(new String(Base64.encodeBase64(getMd5Bytes(toMd5))));
		if( data != null && data.length() > 0 )
		{
			b.append(':');
			b.append(data);
		}
		return b.toString();
	}

	public static byte[] getMd5Bytes(String str)
	{
		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5");
			digest.update(str.getBytes("UTF-8"));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		return digest.digest();
	}

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private TokenGenerator()
	{
		throw new Error();
	}
}
