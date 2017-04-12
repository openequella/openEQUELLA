package com.tle.core.qti.parse.v1x;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author will
 */
public class QTIQuiz implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String title;
	private String id;
	private Map<String, QTIItem> questions = new LinkedHashMap<String, QTIItem>();

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void putQuestion(String key, QTIItem item)
	{
		questions.put(key, item);
	}

	public Map<String, QTIItem> getQuestions()
	{
		return questions;
	}

	public void setQuestions(Map<String, QTIItem> questions)
	{
		this.questions = questions;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}
}