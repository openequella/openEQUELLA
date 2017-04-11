package com.dytech.common.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.Pair;

public class NetworkUtils
{
	/**
	 * @return a bunch of InetAddresses that are "real", ie, not virtual,
	 *         loopback, multicast and are up.
	 */
	public static List<Pair<NetworkInterface, InetAddress>> getInetAddresses()
	{
		List<Pair<NetworkInterface, InetAddress>> addrs = Lists.newArrayList();
		try
		{
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while( nis.hasMoreElements() )
			{
				NetworkInterface ni = nis.nextElement();
				if( !ni.isLoopback() && !ni.isVirtual() && ni.isUp() )
				{
					for( Enumeration<InetAddress> ias = ni.getInetAddresses(); ias.hasMoreElements(); )
					{
						InetAddress ia = ias.nextElement();
						if( !ia.isMulticastAddress() && !ia.isLoopbackAddress() )
						{
							addrs.add(new Pair<NetworkInterface, InetAddress>(ni, ia));
						}
					}
				}
			}
		}
		catch( SocketException e )
		{
			// Carry on
		}
		return addrs;
	}

	private NetworkUtils()
	{
		throw new Error();
	}
}
