package com.tle.oauthserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.io.CharStreams;

@SuppressWarnings("nls")
public class OAuthRedirectorServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static final String PARAM_STATE = "state";
	private static final String PARAM_CODE = "code";
	private static final String PARAM_RESPONSE_TYPE = "response_type";
	private static final String PARAM_ERROR = "error";
	private static final String PARAM_ERROR_DESCRIPTION = "error_description";

	private final ConcurrentMap<String, TestInfo> session = new MapMaker().expireAfterAccess(30, TimeUnit.MINUTES)
		.makeMap();

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		// dynamic values, based on the test being performed
		final String testClientId = request.getParameter("test_client_id");
		String state = request.getParameter(PARAM_STATE);

		final String clientId;
		final String clientSecret;
		final String equellaUrl;
		if( testClientId != null )
		{
			clientId = testClientId;
			clientSecret = request.getParameter("test_client_secret");
			equellaUrl = request.getParameter("test_equella_url");

			state = UUID.randomUUID().toString();
			final TestInfo conf = new TestInfo();
			conf.clientId = clientId;
			conf.clientSecret = clientSecret;
			conf.equellaUrl = equellaUrl;
			session.put(state, conf);
		}
		else if( !Strings.isNullOrEmpty(state) )
		{
			// state must be given then
			final TestInfo conf = session.get(state);
			clientId = conf.clientId;
			clientSecret = conf.clientSecret;
			equellaUrl = conf.equellaUrl;
		}
		else
		{
			// bang
			throw new ServletException("test_client_id not supplied");
		}

		final String home = request.getRequestURL().toString();

		final String error = request.getParameter(PARAM_ERROR);
		if( error != null )
		{
			Map<String, String> vals = ImmutableMap.of(PARAM_ERROR, error, PARAM_ERROR_DESCRIPTION, request.getParameter(PARAM_ERROR_DESCRIPTION));
			writeMapResponse(response, vals);
			return;
		}
		final String code = request.getParameter(PARAM_CODE);
		if( code == null )
		{
			String responseType = request.getParameter(PARAM_RESPONSE_TYPE);
			if( responseType == null )
			{
				responseType = "code";
			}
			final String authUrl = equellaUrl + "oauth/authorise?response_type=" + responseType + "&client_id="
				+ clientId + "&redirect_uri=" + urlEncode(home) + "&state=" + urlEncode(state);

			response.sendRedirect(authUrl);
			return;
		}

		if( session.containsKey(state) )
		{
			final String tokenUrl = equellaUrl + "oauth/access_token?grant_type=authorization_code&client_id="
				+ clientId + "&redirect_uri=" + urlEncode(home) + "&client_secret=" + clientSecret + "&code="
				+ urlEncode(code);

			final URL tokUrl = new URL(tokenUrl);
			final URLConnection openConnection = tokUrl.openConnection();
			openConnection.getDoInput();

			final InputStream inputStream = openConnection.getInputStream();
			final String token;
			try
			{
				token = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));
			}
			finally
			{
				inputStream.close();
			}

			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode rootNode = mapper.readValue(token, JsonNode.class);
			final String tokenData = rootNode.get("access_token").getTextValue();

			// kill session, done with it
			session.remove(state);

			// just blat out the token as read
			writeMapResponse(response, ImmutableMap.of("access_token", tokenData));
			return;
		}

		throw new RuntimeException("The state does not match. You may be a victim of CSRF.");
	}

	private void writeMapResponse(HttpServletResponse response, Map<String, String> vals) throws IOException
	{
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");

		PrintWriter writer = response.getWriter();
		writer.println("<html><body id=\"redirectresponse\">");
		for( Entry<String, String> entry : vals.entrySet() )
		{
			writer.write("<div>");
			writer.write(ent(entry.getKey()+": "));
			writer.write("<span id=\"");
			writer.write(ent(entry.getKey()));
			writer.write("\">");
			writer.write(ent(entry.getValue()));
			writer.write("</span></div>");
		}
		writer.println("</body></html>");
	}

	private String urlEncode(String value)
	{
		try
		{
			return URLEncoder.encode(value, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	private static class TestInfo
	{
		public String clientId;
		public String clientSecret;
		public String equellaUrl;
	}
	
	public static String ent(String szStr)
	{
		if( Strings.isNullOrEmpty(szStr) )
		{
			return "";
		}

		StringBuilder szOut = new StringBuilder();
		final char[] chars = szStr.toCharArray();
		for( final char ch : chars )
		{
			switch( ch )
			{
				case '<':
					szOut.append("&lt;");
					break;

				case '>':
					szOut.append("&gt;");
					break;

				case '&':
					szOut.append("&amp;");
					break;

				case '"':
					szOut.append("&quot;");
					break;

				default:
					// http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
					// regular displayable ASCII:
					if( ch == 0xA || ch == 0xD || ch == 0x9 || (ch >= 0x20 && ch <= 0x007F) )
					{
						szOut.append(ch);
					}
					else if( (ch > 0x007F && ch <= 0xD7FF) || (ch >= 0xE000 && ch <= 0xFFFD)
						|| (ch >= 0x10000 && ch <= 0x10FFFF) )
					{
						szOut.append("&#x");
						final String hexed = Integer.toHexString(ch);
						// wooo, unrolled loops
						switch( 4 - hexed.length() )
						{
							case 3:
								szOut.append('0');
							case 2:
								szOut.append('0');
							case 1:
								szOut.append('0');
						}
						szOut.append(hexed);
						szOut.append(';');
					}
					// else we discard the character entirely.
					// It CANNOT be placed in XML
					break;
			}
		}

		return szOut.toString();
	}

}
