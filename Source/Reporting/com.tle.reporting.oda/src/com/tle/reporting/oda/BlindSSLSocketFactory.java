/*
 * Created on 1/06/2006
 */

package com.tle.reporting.oda;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * BlindSSLSocketFactoryTest Simple test to show an Active Directory (LDAP) and
 * HTTPS connection without verifying the server's certificate.
 * http://blog.platinumsolutions.com/node/79
 * 
 * @author Mike McKinney, Platinum Solutions, Inc.
 */
public class BlindSSLSocketFactory extends SSLSocketFactory
{
	private static SSLSocketFactory blindFactory = null;

	/**
	 * Builds an all trusting "blind" ssl socket factory.
	 */
	static
	{
		// create a trust manager that will purposefully fall down on the
		// job
		TrustManager[] blindTrustMan = new TrustManager[] { new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			public void checkClientTrusted(X509Certificate[] c, String a)
			{
				// Ignore
			}

			public void checkServerTrusted(X509Certificate[] c, String a)
			{
				// IGNORE
			}
		} };

		// create our "blind" ssl socket factory with our lazy trust manager
		try
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, blindTrustMan, new java.security.SecureRandom());
			blindFactory = sc.getSocketFactory();
		}
		catch( GeneralSecurityException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see javax.net.SocketFactory#getDefault()
	 */
	public static synchronized SocketFactory getDefault()
	{
		return new BlindSSLSocketFactory();
	}

	public static SSLSocketFactory getDefaultSSL()
	{
		return new BlindSSLSocketFactory();
	}

	/**
	 * Easy way of not worrying about stupid SSL validation.
	 */
	public static void register()
	{
		HttpsURLConnection.setDefaultSSLSocketFactory(getDefaultSSL());
		// I'm not sure if you need this...
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
		{
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			}
		});
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
	 */
	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException
	{
		return blindFactory.createSocket(arg0, arg1);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
	 */
	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException
	{
		return blindFactory.createSocket(arg0, arg1);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
	 *      java.net.InetAddress, int)
	 */
	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException,
		UnknownHostException
	{
		return blindFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
	 *      java.net.InetAddress, int)
	 */
	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException
	{
		return blindFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException
	{
		return blindFactory.createSocket(socket, s, i, flag);
	}

	@Override
	public String[] getDefaultCipherSuites()
	{
		return blindFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites()
	{
		return blindFactory.getSupportedCipherSuites();
	}
}
