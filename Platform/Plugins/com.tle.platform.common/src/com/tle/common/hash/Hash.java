/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.common.hash;

import org.apache.commons.codec.digest.DigestUtils;

import com.dytech.devlib.Md5;
import com.tle.common.Check;

public class Hash
{
	protected Hash()
	{
		// not to be instantiated, hence nothing to construct except a token
		// hidden constructor to silence Sonar
	}

	public static enum Digester
	{
		SHA256(new SHA1Digester()), MD5(new MD5Digester());

		// // END OF VALUES ////

		private final HashDigester digester;

		Digester(HashDigester digester)
		{
			this.digester = digester;
		}

		public String getDigest(String value)
		{
			return digester.getDigest(value);
		}
	}

	public static String rawHash(Digester digester, String value)
	{
		return digester.getDigest(value);
	}

	public static String hashPassword(String password)
	{
		return PREFERRED.toString() + ':' + rawHash(PREFERRED, password);
	}

	public static boolean isHashed(String value)
	{
		if( !Check.isEmpty(value) )
		{
			return value.startsWith(PREFERRED.toString() + ':') || value.startsWith(FALLBACK.toString() + ':');
		}
		return false;
	}

	@SuppressWarnings("nls")
	public static boolean checkPasswordMatch(String hashValue, String password)
	{
		Digester algo = FALLBACK;

		String[] parts = hashValue.split(":", 2);
		if( parts.length == 2 )
		{
			for( Digester d : Digester.values() )
			{
				if( d.toString().equals(parts[0]) )
				{
					algo = d;
				}
			}
			hashValue = parts[1];
		}

		return rawHash(algo, password).equals(hashValue);
	}

	// PRIVATE STUFF! //

	private static final Digester PREFERRED = Digester.SHA256;
	private static final Digester FALLBACK = Digester.MD5;

	private interface HashDigester
	{
		String getDigest(String value);
	}

	private static class SHA1Digester implements HashDigester
	{
		@SuppressWarnings("nls")
		private static final String customSalt = System.getProperty("equella.salt");
		private static final String SHA256_SALT = customSalt == null ? "TimeIsAnIllusion.LunchtimeDoublySo." : customSalt;

		@Override
		public String getDigest(String value)
		{
			return DigestUtils.sha256Hex(SHA256_SALT + value);
		}
	}

	private static class MD5Digester implements HashDigester
	{
		@Override
		public String getDigest(String value)
		{
			return new Md5(value).getStringDigest();
		}
	}
}
