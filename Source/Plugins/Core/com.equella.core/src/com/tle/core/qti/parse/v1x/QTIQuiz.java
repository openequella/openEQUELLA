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