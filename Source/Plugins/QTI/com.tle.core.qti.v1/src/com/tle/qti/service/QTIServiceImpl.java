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

package com.tle.qti.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.qti.data.QTIAnswer;
import com.tle.qti.data.QTIItem;
import com.tle.qti.data.QTIMaterial;
import com.tle.qti.data.QTIMaterialElement.QTIMaterialMediaType;
import com.tle.qti.data.QTIMaterialMedia;
import com.tle.qti.data.QTIMaterialText;
import com.tle.qti.data.QTIQuiz;
import com.tle.qti.data.QTIResponse;
import com.tle.qti.data.QTIResponseElement;

@Bind(QTIService.class)
@Singleton
public class QTIServiceImpl implements QTIService
{
	@Inject
	private UrlService urlService;

	@SuppressWarnings("nls")
	@Override
	public QTIQuiz parseQuizXml(PropBagEx quizXml, String resource)
	{
		QTIQuiz quiz = new QTIQuiz();
		quiz.setTitle(quizXml.getNode("assessment/@title"));
		quiz.setId(quizXml.getNode("assessment/@ident"));

		Iterator<PropBagEx> quizIterator = quizXml.iterateAllNodesWithName("item");
		while( quizIterator.hasNext() )
		{
			PropBagEx itemXml = quizIterator.next();
			PropBagEx presXml = itemXml.aquireSubtree("presentation");
			PropBagEx resProcXml = itemXml.aquireSubtree("resprocessing");

			String questId = itemXml.getNode("@ident");
			String title = itemXml.getNode("@title");
			String type = itemXml.getNode("itemmetadata/qmd_itemtype");

			QTIMaterial questionText = new QTIMaterial();

			PropBagIterator matIterator = presXml.iterator("material");
			while( matIterator.hasNext() )
			{
				PropBagEx materialXml = matIterator.next();
				questionText.addAll(parseDisplayElementsXml(resource, materialXml).getElements());
			}
			QTIItem qtiItem = new QTIItem(questId, title, questionText, type);

			qtiItem.getResponses().putAll(parseResponseXml(resource, presXml));

			qtiItem.getGeneralFeedback().addAll(parseGeneralFeedbackXml(resource, itemXml));
			qtiItem.getResponseFeedback().putAll(parseResponseFeedbackXml(resource, itemXml));

			qtiItem.getAnswers()
				.addAll(parseResponseConditionsXml(resProcXml, qtiItem.getResponseFeedback(), qtiItem.getResponses()));

			quiz.putQuestion(questId, qtiItem);
		}

		return quiz;
	}

	@SuppressWarnings("nls")
	private List<QTIAnswer> parseResponseConditionsXml(PropBagEx resProcXml, Map<String, QTIMaterial> responseFeedback,
		Map<String, QTIResponse> responses)
	{
		List<QTIAnswer> feedbacks = new ArrayList<QTIAnswer>();
		PropBagIterator respCondIterator = resProcXml.iterator("respcondition");
		while( respCondIterator.hasNext() )
		{
			PropBagEx respCondXml = respCondIterator.next();

			PropBagIterator varIterator = respCondXml.iterator("conditionvar/varequal");
			while( varIterator.hasNext() )
			{
				PropBagEx varXml = varIterator.next();

				String respId = varXml.getNode("@respident");
				if( !Check.isEmpty(respId) )
				{
					// Respondus_Correct is for "matching" type
					if( resProcXml.nodeCount("outcomes/decvar") == 1
						|| respCondXml.getNode("setvar/@varname").equals("Respondus_Correct") )
					{
						QTIResponse qtiResponse = responses.get(respId);

						String score = respCondXml.getNode("setvar");
						String action = respCondXml.getNode("setvar/@action");
						String displayText = varXml.getNode();
						QTIMaterial display;

						if( qtiResponse.isLookupId() )
						{
							display = new QTIMaterial(new QTIMaterialText(displayText));
						}
						else
						{
							QTIResponseElement ele = qtiResponse.getElements().get(displayText.trim());
							display = ele.getDisplay();
						}

						QTIAnswer qtiFeedback = new QTIAnswer();
						qtiFeedback.setAction(action);
						qtiFeedback.setScore(Double.parseDouble(score));
						qtiFeedback.setDisplay(display);

						PropBagIterator dispFeedIterator = respCondXml.iterator("displayfeedback");
						while( dispFeedIterator.hasNext() )
						{
							PropBagEx dispFeedXml = dispFeedIterator.next();
							String feedId = dispFeedXml.getNode("@linkrefid");
							if( responseFeedback.containsKey(feedId) )
							{
								qtiFeedback.addFeedback(responseFeedback.get(feedId));
							}
						}

						feedbacks.add(qtiFeedback);
					}
				}
			}
		}
		return feedbacks;
	}

	@SuppressWarnings("nls")
	private Map<String, QTIMaterial> parseResponseFeedbackXml(String resource, PropBagEx itemXml)
	{
		Map<String, QTIMaterial> responseFeedback = new LinkedHashMap<String, QTIMaterial>();
		PropBagIterator feedIterator = itemXml.iterator("itemfeedback");
		while( feedIterator.hasNext() )
		{
			PropBagEx feedbackXml = feedIterator.next();
			String feedId = feedbackXml.getNode("@ident");
			String feedView = feedbackXml.getNode("@view");
			QTIMaterial feedDisplay = parseDisplayElementsXml(resource, feedbackXml.aquireSubtree("material"));

			if( feedView.equalsIgnoreCase("Candidate") )
			{
				responseFeedback.put(feedId, feedDisplay);
			}
		}
		return responseFeedback;
	}

	@SuppressWarnings("nls")
	private List<QTIMaterial> parseGeneralFeedbackXml(String resource, PropBagEx itemXml)
	{
		List<QTIMaterial> generalFeedback = new ArrayList<QTIMaterial>();
		PropBagIterator feedIterator = itemXml.iterator("itemfeedback");
		while( feedIterator.hasNext() )
		{
			PropBagEx feedbackXml = feedIterator.next();
			String feedView = feedbackXml.getNode("@view");
			QTIMaterial feedDisplay = parseDisplayElementsXml(resource, feedbackXml.aquireSubtree("material"));
			if( feedView.equalsIgnoreCase("all") )
			{

				generalFeedback.add(feedDisplay);
			}
		}
		return generalFeedback;
	}

	@SuppressWarnings("nls")
	private Map<String, QTIResponse> parseResponseXml(String resource, PropBagEx presXml)
	{
		Map<String, QTIResponse> responses = new LinkedHashMap<String, QTIResponse>();

		PropBagIterator respIterator = presXml.iterator();
		while( respIterator.hasNext() )
		{
			PropBagEx respXml = respIterator.next();
			if( respXml.getNodeName().startsWith("response_") )
			{
				String respId = respXml.getNode("@ident");
				String respCard = respXml.getNode("@rcardinality");
				boolean lookupId = !respXml.getNodeName().endsWith("_lid");
				PropBagIterator choiceIterator = respXml.iterator();
				while( choiceIterator.hasNext() )
				{
					PropBagEx choiceXml = choiceIterator.next();
					String nodeName = choiceXml.getNodeName();
					if( nodeName.startsWith("render_") )
					{
						String renderChoice = nodeName.substring(7, nodeName.length());

						PropBagEx materialXml;
						if( respXml.nodeExists("material") )
						{
							materialXml = respXml.aquireSubtree("material");
						}
						else
						{
							materialXml = respXml.aquireSubtree("render_" + renderChoice + "/material");
						}

						QTIMaterial display = parseDisplayElementsXml(resource, materialXml);

						QTIResponse qtiResponse = new QTIResponse(respId, renderChoice, display, respCard, lookupId);

						PropBagIterator rendIterator = respXml.iterator("render_" + renderChoice + "/response_label");
						while( rendIterator.hasNext() )
						{
							PropBagEx rendXml = rendIterator.next();

							QTIMaterial rendStr = parseDisplayElementsXml(resource, rendXml.aquireSubtree("material"));

							String nodeValue = rendXml.getNode();
							if( !Check.isEmpty(nodeValue) )
							{
								rendStr.add(new QTIMaterialText(nodeValue));
							}

							String respChoiceId = rendXml.getNode("@ident");

							QTIResponseElement ele = new QTIResponseElement();

							ele.setId(respChoiceId);
							ele.setType(renderChoice);
							ele.setDisplay(rendStr);
							qtiResponse.putElement(respChoiceId, ele);
						}

						responses.put(respId, qtiResponse);
					}
				}
			}
		}
		return responses;
	}

	@SuppressWarnings("nls")
	private QTIMaterial parseDisplayElementsXml(String resource, PropBagEx materialXml)
	{
		QTIMaterial matElements = new QTIMaterial();
		PropBagIterator allNodes = materialXml.iterator();
		while( allNodes.hasNext() )
		{
			PropBagEx materialElement = allNodes.next();
			String materialType = materialElement.getNodeName();

			if( materialType.equals("mattext") )
			{
				matElements.add(new QTIMaterialText(materialElement.getNode()));
			}
			else if( materialType.equals("matemtext") )
			{
				matElements.add(new QTIMaterialText(materialElement.getNode(), true));
			}
			else if( materialElement.nodeExists("@uri") )
			{
				String url = materialElement.getNode("@uri");
				if( urlService.isRelativeUrl(url) )
				{
					url = resource + "/" + url;
				}
				if( materialType.equals("mataudio") )
				{
					matElements.add(new QTIMaterialMedia(url, QTIMaterialMediaType.EMBED));
				}
				else if( materialType.equals("matimage") )
				{
					matElements.add(new QTIMaterialMedia(url, QTIMaterialMediaType.IMAGE));
				}
			}
		}

		return matElements;
	}

	@SuppressWarnings("nls")
	@Override
	public PropBagEx createQuizXml(QTIQuiz quiz)
	{
		PropBagEx quizXml = new PropBagEx("<questestinterop/>");
		quizXml.setNode("assessment/@title", quiz.getTitle());
		quizXml.setNode("assessment/@ident", quiz.getId());
		quizXml.setNode("assessment/section/@title", "Main");

		Collection<QTIItem> questions = quiz.getQuestions().values();

		for( QTIItem qtiItem : questions )
		{
			quizXml.append("assessment/section", createItemXml(qtiItem));
		}
		return quizXml;
	}

	@SuppressWarnings("nls")
	@Override
	public PropBagEx createItemXml(QTIItem item)
	{
		PropBagEx itemXml = new PropBagEx("<item/>");
		itemXml.setNode("@title", item.getTitle());
		itemXml.setNode("@ident", item.getId());

		// TODO qti phase 2/3

		return itemXml;
	}

}
