package com.tle.qti.service;

import com.dytech.devlib.PropBagEx;
import com.tle.qti.data.QTIItem;
import com.tle.qti.data.QTIQuiz;

public interface QTIService
{
	QTIQuiz parseQuizXml(PropBagEx quizXml, String resource);

	PropBagEx createQuizXml(QTIQuiz quiz);

	PropBagEx createItemXml(QTIItem item);
}
