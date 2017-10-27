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

package com.tle.core.xstream.mapping;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class SetMapping extends CollectionMapping
{
	public SetMapping(String name, String node)
	{
		super(name, node);
	}

	public SetMapping(String name, String node, Class<?> type)
	{
		super(name, node, type);
	}

	public SetMapping(String name, String node, Class<?> type, Class<?> eltype)
	{
		super(name, node, type, eltype);
	}

	public SetMapping(String name, String node, Class<?> type, AbstractMapping converter)
	{
		super(name, node, type, converter);
	}

	@Override
	public Class<?> getRequiredType()
	{
		return Set.class;
	}

	// Presumably the intent is to return the implementation class, so we
	// ignore Sonar's "loose coupling" warning
	@Override
	public Class<?> getDefaultType()
	{
		return HashSet.class; // NOSONAR
	}
}
