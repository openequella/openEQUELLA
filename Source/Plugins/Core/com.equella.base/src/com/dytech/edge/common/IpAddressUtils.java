/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.edge.common;

import com.tle.common.Check;
import com.tle.common.Pair;

/**
 * @author Nicholas Read
 */
public final class IpAddressUtils
{
	public static boolean matches(String ipAddress, String cidrAddress)
	{
		Check.checkNotNull(ipAddress, cidrAddress);

		int ipAddressBits = ipAddressToBits(ipAddress);
		Pair<Integer, Integer> cidrBits = splitCidrIpAddress(cidrAddress);
		int mask = cidrBits.getSecond();
		return (ipAddressBits & mask) == (cidrBits.getFirst() & mask);
	}

	public static Matcher matchRangesAgainstIpAddress(final String ipAddress)
	{
		Check.checkNotNull(ipAddress);

		return new Matcher()
		{
			private final int ipAddressBits = ipAddressToBits(ipAddress);

			/*
			 * (non-Javadoc)
			 * @see
			 * com.dytech.edge.common.IpAddressUtils.Matcher#matches(java.lang
			 * .String)
			 */
			@Override
			public boolean matches(String cidrIpAddress)
			{
				Pair<Integer, Integer> cidrBits = splitCidrIpAddress(cidrIpAddress);
				int mask = cidrBits.getSecond();
				return (ipAddressBits & mask) == (cidrBits.getFirst() & mask);
			}
		};
	}

	public interface Matcher
	{
		boolean matches(String address);
	}

	private static Pair<Integer, Integer> splitCidrIpAddress(String address)
	{
		int slash = address.indexOf('/');
		int subnet = Integer.parseInt(address.substring(slash + 1));

		int bits = ipAddressToBits(address.substring(0, slash));
		int mask = ~((1 << (32 - subnet)) - 1);

		return new Pair<Integer, Integer>(bits, mask);
	}

	private static int ipAddressToBits(String ipAddress)
	{
		String[] parts = ipAddress.split("\\."); //$NON-NLS-1$
		if( parts.length != 4 )
		{
			throw new IllegalArgumentException("IP Address did not have 4 parts");
		}

		int results = Integer.parseInt(parts[3]) & 0xFF;
		results |= (Integer.parseInt(parts[2]) & 0xFF) << 8;
		results |= (Integer.parseInt(parts[1]) & 0xFF) << 16;
		results |= (Integer.parseInt(parts[0]) & 0xFF) << 24;

		return results;
	}

	private IpAddressUtils()
	{
		throw new Error();
	}
}
