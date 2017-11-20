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

package com.tle.mycontent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jmaginnis
 */
public class MyContentFilter
{
	private final String name;
	private final String icon;
	private final List<String> contentTypes;
	private final List<String> mimeTypes;

	public MyContentFilter(String name, String icon, String... types)
	{
		this(name, icon, new ArrayList<String>(), Arrays.asList(types));
	}

	public MyContentFilter(String name, String icon, List<String> contentTypes, List<String> types)
	{
		this.name = name;
		this.icon = icon;
		this.contentTypes = contentTypes;
		this.mimeTypes = types;
	}

	public String getName()
	{
		return name;
	}

	public String getIcon()
	{
		return icon;
	}

	public List<String> getContentTypes()
	{
		return contentTypes;
	}

	public List<String> getMimeTypes()
	{
		return mimeTypes;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
