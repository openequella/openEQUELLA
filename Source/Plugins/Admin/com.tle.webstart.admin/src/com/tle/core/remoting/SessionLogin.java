package com.tle.core.remoting;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.tle.common.URLUtils;
import com.tle.exceptions.BadCredentialsException;

public class SessionLogin
{

	public static void postLogin(URL endpointUrl, Map<String, String> params)
		throws IOException, BadCredentialsException
	{
		HttpURLConnection conn = (HttpURLConnection) new URL(endpointUrl, "session").openConnection();
		String postData = URLUtils.getParameterString(params);
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
		conn.setRequestProperty("Content-Length", Integer.toString(postBytes.length));
		conn.setUseCaches(false);
		try( OutputStream wr = conn.getOutputStream() )
		{
			wr.write(postBytes);
		}

		int code = conn.getResponseCode();
		if( code == 403 || code == 401 )
		{
			throw new BadCredentialsException("Bad credentials");
		}
		else if (code != 200)
		{
			throw new RuntimeException("Error launching console: " + code);
		}
	}
}
