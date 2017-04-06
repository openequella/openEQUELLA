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
