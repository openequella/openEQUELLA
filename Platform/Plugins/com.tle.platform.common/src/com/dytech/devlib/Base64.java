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

package com.dytech.devlib;

@SuppressWarnings("nls")
public class Base64
{

	public String encode(byte b[])
	{
		int outputLength = ((b.length + 2) / 3) * 4;
		if( lineLength != 0 )
		{
			int lines = ((outputLength + lineLength) - 1) / lineLength - 1;
			if( lines > 0 )
			{
				outputLength += lines * lineSeparator.length();
			}
		}
		StringBuilder sb = new StringBuilder(outputLength);
		int linePos = 0;
		int len = (b.length / 3) * 3;
		int leftover = b.length - len;
		for( int i = 0; i < len; i += 3 )
		{
			linePos += 4;
			if( linePos > lineLength )
			{
				if( lineLength != 0 )
				{
					sb.append(lineSeparator);
				}
				linePos = 4;
			}
			int combined = b[i + 0] & 0xff;
			combined <<= 8;
			combined |= b[i + 1] & 0xff;
			combined <<= 8;
			combined |= b[i + 2] & 0xff;
			int c3 = combined & 0x3f;
			combined >>>= 6;
			int c2 = combined & 0x3f;
			combined >>>= 6;
			int c1 = combined & 0x3f;
			combined >>>= 6;
			int c0 = combined & 0x3f;
			sb.append(valueToChar[c0]);
			sb.append(valueToChar[c1]);
			sb.append(valueToChar[c2]);
			sb.append(valueToChar[c3]);
		}

		switch( leftover )
		{
			case 1: // '\001'
				linePos += 4;
				if( linePos > lineLength )
				{
					if( lineLength != 0 )
					{
						sb.append(lineSeparator);
					}
					linePos = 4;
				}
				sb.append(encode(new byte[]{b[len], 0, 0}).substring(0, 2));
				sb.append("==");
				break;

			case 2: // '\002'
				linePos += 4;
				if( linePos > lineLength )
				{
					if( lineLength != 0 )
					{
						sb.append(lineSeparator);
					}
					linePos = 4;
				}
				sb.append(encode(new byte[]{b[len], b[len + 1], 0}).substring(0, 3));
				sb.append("=");
				break;

			default:
				// leftover is modulo 3, so it's 0 (no action), 1 or 2
				break;
		}
		if( outputLength != sb.length() )
		{
			System.out.println("oops: minor program flaw: output length mis-estimated");
			System.out.println("estimate:" + outputLength);
			System.out.println("actual:" + sb.length());
		}
		return sb.toString();
	}

	public byte[] decode(String s)
	{
		byte b[] = new byte[(s.length() / 4) * 3];
		int cycle = 0;
		int combined = 0;
		int j = 0;
		int len = s.length();
		int dummies = 0;
		int i = 0;
		do
			if( i < len )
			{
				int c = s.charAt(i);
				int value = c > 255 ? -1 : charToValue[c];
				switch( value )
				{
					case -2:
						value = 0;
						dummies++;
						// fall through

					default:
						switch( cycle )
						{
							case 0: // '\0'
								combined = value;
								cycle = 1;
								break;

							case 1: // '\001'
								combined <<= 6;
								combined |= value;
								cycle = 2;
								break;

							case 2: // '\002'
								combined <<= 6;
								combined |= value;
								cycle = 3;
								break;

							case 3: // '\003'
								combined <<= 6;
								combined |= value;
								b[j + 2] = (byte) combined;
								combined >>>= 8;
								b[j + 1] = (byte) combined;
								combined >>>= 8;
								b[j] = (byte) combined;
								j += 3;
								cycle = 0;
								break;

							default:
								break;
						}
						// fall through

					case -1:
						i++;
						break;
				}
			}
			else
			{
				if( cycle != 0 )
				{
					throw new ArrayIndexOutOfBoundsException(
						"Input to decode not an even multiple of 4 characters; pad with =.");
				}
				j -= dummies;
				if( b.length != j )
				{
					byte b2[] = new byte[j];
					System.arraycopy(b, 0, b2, 0, j);
					b = b2;
				}
				return b;
			}
		while( true );
	}

	public void setLineLength(int length)
	{
		lineLength = (length / 4) * 4;
	}

	public void setLineSeparator(String lineSeparator)
	{
		this.lineSeparator = lineSeparator;
	}

	public static void show(byte b[])
	{
		for( int i = 0; i < b.length; i++ )
		{
			System.out.print(Integer.toHexString(b[i] & 0xff) + " ");
		}
		System.out.println();
	}

	public static void display(byte b[])
	{
		for( int i = 0; i < b.length; i++ )
		{
			System.out.print((char) b[i]);
		}
		System.out.println();
	}

	public Base64()
	{
		lineSeparator = System.getProperty("line.separator");
		lineLength = 72;
	}

	private String lineSeparator;
	private int lineLength;
	static final char valueToChar[];
	static final int charToValue[];
	static final int IGNORE = -1;
	static final int PAD = -2;

	static
	{
		valueToChar = new char[64];
		charToValue = new int[256];
		for( int i = 0; i <= 25; i++ )
		{
			valueToChar[i] = (char) (65 + i);
		}

		for( int i = 0; i <= 25; i++ )
		{
			valueToChar[i + 26] = (char) (97 + i);
		}
		for( int i = 0; i <= 9; i++ )
		{
			valueToChar[i + 52] = (char) (48 + i);
		}
		valueToChar[62] = '+';
		valueToChar[63] = '/';
		for( int i = 0; i < 256; i++ )
		{
			charToValue[i] = -1;
		}
		for( int i = 0; i < 64; i++ )
		{
			charToValue[valueToChar[i]] = i;
		}
		charToValue[61] = -2;
	}
}
