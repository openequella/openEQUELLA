package com.tle.qti.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.qti.data.QTIItem;
import com.tle.qti.data.QTIQuiz;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;

public class QTIQuizRenderer
{
	private QTIQuiz quiz;

	public QTIQuizRenderer(QTIQuiz quiz)
	{
		this.quiz = quiz;
	}

	public SectionRenderable getTitle()
	{
		return new SimpleSectionResult(quiz.getTitle());
	}

	public List<QTIItemRenderer> getQuestions()
	{
		ArrayList<QTIItemRenderer> renderedQuestions = new ArrayList<QTIItemRenderer>();
		Map<String, QTIItem> questions = quiz.getQuestions();
		Collection<QTIItem> values = questions.values();
		for( QTIItem qtiItem : values )
		{
			renderedQuestions.add(new QTIItemRenderer(qtiItem));
		}

		return renderedQuestions;
	}
}
