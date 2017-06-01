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
