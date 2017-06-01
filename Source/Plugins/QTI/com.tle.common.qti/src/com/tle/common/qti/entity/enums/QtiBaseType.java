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

package com.tle.common.qti.entity.enums;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public enum QtiBaseType
{
	IDENTIFIER, BOOLEAN, INTEGER, FLOAT, STRING, POINT, PAIR, DIRECTED_PAIR
	{
		@Override
		public String toString()
		{
			return "directedPair";
		}
	},
	DURATION, FILE, URI, INT_OR_IDENTIFIER
	{
		@Override
		public String toString()
		{
			return "intOrIdentifier";
		}
	};

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}

	@Nullable
	public static QtiBaseType fromString(@Nullable String name)
	{
		if( name == null )
		{
			return null;
		}
		if( name.equals("directedPair") )
		{
			return DIRECTED_PAIR;
		}
		if( name.equals("intOrIdentifier") )
		{
			return INT_OR_IDENTIFIER;
		}
		return QtiBaseType.valueOf(name.toUpperCase());
	}
}
