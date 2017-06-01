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

package com.tle.common.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class EnumUtils
{
	/**
	 * Maps enum values to a bit in a bit mask. Can be used to retrieve an Enum
	 * value mapped to a single number, or an EnumSet for a bitmask. A good
	 * example of both uses can be seen n
	 * {code}com.tle.web.remoting.soap.SoapServiceImpl.getComments{code}.
	 * 
	 * @author nick
	 */
	public interface EnumMask<E extends Enum<E>>
	{
		EnumSet<E> enumsForMask(int mask);

		/**
		 * @param bit Bits are numbered starting from one, not zero. It makes
		 *            more sense this way since a bit mask of "1" is indicating
		 *            the first bit, so asking for bit 1 should give you the
		 *            first mapped enum, not the second.
		 */
		E getForBit(int bit);
	}

	public static class EnumMaskBuilder<E extends Enum<E>>
	{
		private Class<E> enumType;
		private List<E> bits = new ArrayList<E>();

		public static <E extends Enum<E>> EnumMaskBuilder<E> with(Class<E> enumType)
		{
			EnumMaskBuilder<E> rv = new EnumMaskBuilder<E>();
			rv.enumType = enumType;
			return rv;
		}

		public EnumMaskBuilder<E> add(E e)
		{
			bits.add(e);
			return this;
		}

		public EnumMaskBuilder<E> add(E... es)
		{
			for( E e : es )
			{
				bits.add(e);
			}
			return this;
		}

		public EnumMaskBuilder<E> add(EnumSet<E> set)
		{
			bits.addAll(set);
			return this;
		}

		public EnumMaskBuilder<E> skipBit()
		{
			bits.add(null);
			return this;
		}

		public EnumMaskBuilder<E> skipToBit(int bit)
		{
			if( bits.size() >= bit )
			{
				throw new IllegalArgumentException("Bit " + bit + " has already been set as " + bits.get(bit - 1));
			}

			// null out bits up to and including the bit before the given
			// argument.
			for( int i = bits.size(); i < bit; i++ )
			{
				bits.add(null);
			}
			return this;
		}

		public EnumMask<E> build()
		{
			// Null out field to invalidate the builder, preventing further
			// changes.
			final List<E> fixedbitmap = bits;
			bits = null;

			return new EnumMask<E>()
			{
				@Override
				public EnumSet<E> enumsForMask(final int mask)
				{
					EnumSet<E> rv = EnumSet.noneOf(enumType);
					int m = mask;
					int i = 0;
					while( m != 0 )
					{
						if( (m & 1) != 0 )
						{
							E e = fixedbitmap.get(i);
							if( e != null )
							{
								rv.add(e);
							}
							else
							{
								throw new IllegalArgumentException("Bit " + i + " for mask " + mask + " is not defined");
							}
						}
						m >>= 1;
						i++;
					}
					return rv;
				}

				@Override
				public E getForBit(int bit)
				{
					E e = fixedbitmap.get(bit - 1);
					if( e != null )
					{
						return e;
					}
					throw new IllegalArgumentException("Bit " + bit + " is not defined");
				}

				@Override
				public String toString()
				{
					StringBuilder sb = new StringBuilder();
					sb.append("EnumMask[");
					sb.append(enumType.getName());
					sb.append("] {\n  Bit, Number, Enum\n");
					for( int i = 0, j = fixedbitmap.size(); i < j; i++ )
					{
						E e = fixedbitmap.get(i);
						if( e != null )
						{
							sb.append("  ");
							sb.append(i + 1);
							sb.append(", ");
							sb.append((int) Math.pow(2, i));
							sb.append(", ");
							sb.append(e);
							sb.append('\n');
						}
					}
					sb.append('}');
					return sb.toString();
				}
			};
		}
	}
}
