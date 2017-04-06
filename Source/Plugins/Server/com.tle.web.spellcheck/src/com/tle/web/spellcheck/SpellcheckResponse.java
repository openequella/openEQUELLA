package com.tle.web.spellcheck;

import java.util.List;

public class SpellcheckResponse
{
	private String id;
	private List<String> result;

	public String getId()
	{
		return null;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public List<String> getResult()
	{
		return result;
	}

	public void setResult(List<String> result)
	{
		this.result = result;
	}

	public List<String> getError()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "id: \n" + id + "\n\n results: \n " + getResult().toString() + "\n\n error: \n " + getError();
	}
}
