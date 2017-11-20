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

package com.tle.core.connectors.brightspace;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A convenience class to help with the signature generation used in the D2L authentication system
 */
public class D2LSigner
{

	/**
	 * Provides the D2L custom encoded version of hmacSha256 hash of the data provided using the key provided
	 * @param key The key to use to calculate the hash
	 * @param data The data to use to calculate the hash
	 * @return 
	 */
	public static String getBase64HashString(String key, String data)
	{
		byte[] keyBytes = getBytes(key);
		byte[] dataBytes = getBytes(data);
		byte[] hash = computeHash(keyBytes, dataBytes);
		return new String(org.apache.commons.codec.binary.Base64.encodeBase64(hash, false, true));
	}

	/**
	 * Provides the byte value of the given String
	 * @param key The String to return the bytes of
	 * @return The bytes representing the given string
	 */
	private static byte[] getBytes(String key)
	{
		return key.getBytes();
	}

	/**
	 * Computes the hmacSha256 hash of the data using the key given
	 * @param keyBytes The key to use to calculate the hash
	 * @param dataBytes The data to use to calculate the hash
	 * @return The hmacSha256 hash of the data using the key
	 */
	private static byte[] computeHash(byte[] keyBytes, byte[] dataBytes)
	{
		try
		{
			Mac hmacSha256 = Mac.getInstance("HmacSHA256");
			SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
			hmacSha256.init(key);
			byte[] b = hmacSha256.doFinal(dataBytes);

			return b;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return null;
	}
}
