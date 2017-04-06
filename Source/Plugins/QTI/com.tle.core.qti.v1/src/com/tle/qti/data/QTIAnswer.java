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