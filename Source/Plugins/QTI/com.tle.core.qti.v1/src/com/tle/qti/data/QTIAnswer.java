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
import java.util.List;

/**
 * A answer option to a question. Contains the answer (display), a score (can be
 * positive or negative), and any specific feedback for this answer
 * 
 * @author will
 */
public class QTIAnswer implements Serializable
{
	private static final long serialVersionUID = 1L;

	private QTIMaterial display;
	private List<QTIMaterial> feedback = new ArrayList<QTIMaterial>();
	private double score;
	private String action;

	public double getScore()
	{
		return score;
	}

	public void setScore(double score)
	{
		this.score = score;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public QTIMaterial getDisplay()
	{
		return display;
	}

	public void setDisplay(QTIMaterial display)
	{
		this.display = display;
	}

	public void setFeedback(List<QTIMaterial> feedback)
	{
		this.feedback = feedback;
	}

	public List<QTIMaterial> getFeedback()
	{
		return feedback;
	}

	public void addFeedback(QTIMaterial feedbackString)
	{
		feedback.add(feedbackString);
	}

}