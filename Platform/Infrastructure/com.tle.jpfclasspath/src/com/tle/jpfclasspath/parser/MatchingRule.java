/*****************************************************************************
 * Java Plug-in Framework (JPF) Copyright (C) 2007 Dmitry Olshansky This library
 * is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *****************************************************************************/
package com.tle.jpfclasspath.parser;

/**
 * Version identifier matching modes.
 */
public enum MatchingRule
{
	/**
	 * Version identifier matching rule constant.
	 */
	EQUAL
	{
		/**
		 * @see org.java.plugin.registry.MatchingRule#toCode()
		 */
		@Override
		public String toCode()
		{
			return "equal"; //$NON-NLS-1$
		}
	},

	/**
	 * Version identifier matching rule constant.
	 */
	EQUIVALENT
	{
		/**
		 * @see org.java.plugin.registry.MatchingRule#toCode()
		 */
		@Override
		public String toCode()
		{
			return "equivalent"; //$NON-NLS-1$
		}
	},

	/**
	 * Version identifier matching rule constant.
	 */
	COMPATIBLE
	{
		/**
		 * @see org.java.plugin.registry.MatchingRule#toCode()
		 */
		@Override
		public String toCode()
		{
			return "compatible"; //$NON-NLS-1$
		}
	},

	/**
	 * Version identifier matching rule constant.
	 */
	GREATER_OR_EQUAL
	{
		/**
		 * @see org.java.plugin.registry.MatchingRule#toCode()
		 */
		@Override
		public String toCode()
		{
			return "greater-or-equal"; //$NON-NLS-1$
		}
	};

	/**
	 * @return constant code to be used in plug-in manifest
	 */
	public abstract String toCode();

	/**
	 * Converts plug-in manifest string code to matching rule constant value.
	 * 
	 * @param code code from plug-in manifest
	 * @return matching rule constant value
	 */
	public static MatchingRule fromCode(final String code)
	{
		for( MatchingRule item : MatchingRule.values() )
		{
			if( item.toCode().equals(code) )
			{
				return item;
			}
		}
		throw new IllegalArgumentException("unknown matching rule code " + code); //$NON-NLS-1$
	}
}