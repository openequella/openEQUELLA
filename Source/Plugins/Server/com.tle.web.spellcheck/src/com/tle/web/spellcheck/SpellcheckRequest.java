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

package com.tle.web.spellcheck;

import java.util.List;

public class SpellcheckRequest
{
	private String id;
	private String method;
	private SpellcheckRequestParams spellParam;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public SpellcheckRequestParams getParams()
	{
		return spellParam;
	}

	public void setParams(SpellcheckRequestParams spellParam)
	{
		this.spellParam = spellParam;
	}

	public static class SpellcheckRequestParams
	{
		private String lang;
		private List<String> stringList;

		public String getLang()
		{
			return lang;
		}

		public void setLang(String lang)
		{
			this.lang = lang;
		}

		public List<String> getStringList()
		{
			return stringList;
		}

		public void setStringList(List<String> stringList)
		{
			this.stringList = stringList;
		}
	}
}
