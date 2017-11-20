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

package com.tle.qti.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * QTIItem - An item contains the question, the available responses, the correct
 * answers.
 * 
 * @author will
 */
public class QTIItem implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String id;
	private String title;
	private String type;
	private QTIMaterial question;
	private Map<String, QTIResponse> responses = new LinkedHashMap<String, QTIResponse>();
	private List<QTIAnswer> answers = new ArrayList<QTIAnswer>();
	private Map<String, QTIMaterial> responseFeedback = new LinkedHashMap<String, QTIMaterial>();
	private List<QTIMaterial> generalFeedback = new ArrayList<QTIMaterial>();

	public QTIItem(String id, String title, QTIMaterial question, String type)
	{
		this.id = id;
		this.title = title;
		this.question = question;
		this.type = type;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public QTIMaterial getQuestion()
	{
		return question;
	}

	public void setQuestion(QTIMaterial question)
	{
		this.question = question;
	}

	public Map<String, QTIResponse> getResponses()
	{
		return responses;
	}

	public void setResponses(Map<String, QTIResponse> responses)
	{
		this.responses = responses;
	}

	public void putResponse(String id, QTIResponse resp)
	{
		responses.put(id, resp);
	}

	public void setAnswers(List<QTIAnswer> feedbacks)
	{
		this.answers = feedbacks;
	}

	public List<QTIAnswer> getAnswers()
	{
		return answers;
	}

	public void setResponseFeedback(Map<String, QTIMaterial> responseFeedback)
	{
		this.responseFeedback = responseFeedback;
	}

	public Map<String, QTIMaterial> getResponseFeedback()
	{
		return responseFeedback;
	}

	public void addResponseFeedback(String id, QTIMaterial responseFeedback)
	{
		this.responseFeedback.put(id, responseFeedback);
	}

	public void setGeneralFeedback(List<QTIMaterial> generalFeedback)
	{
		this.generalFeedback = generalFeedback;
	}

	public List<QTIMaterial> getGeneralFeedback()
	{
		return generalFeedback;
	}

	public void addGeneralFeedback(QTIMaterial generalFeedback)
	{
		this.generalFeedback.add(generalFeedback);
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}
}