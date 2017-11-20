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

package com.tle.core.qti.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import com.tle.common.Check;
import com.tle.common.qti.entity.QtiAbstractResult;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.common.qti.entity.QtiItemResult;
import com.tle.common.qti.entity.QtiItemVariable;
import com.tle.common.qti.entity.QtiItemVariable.VariableType;
import com.tle.common.qti.entity.enums.QtiBaseType;
import com.tle.common.qti.entity.enums.QtiCardinality;
import com.tle.common.qti.entity.enums.QtiSessionStatus;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.qti.dao.QtiAssessmentResultDao;
import com.tle.core.qti.service.QtiAssessmentResultService;
import com.tle.core.qti.service.QtiService;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.xml.XmlDocument;

import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.result.ResponseVariable;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.state.marshalling.TestSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(QtiAssessmentResultService.class)
@Singleton
public class QtiAssessmentResultServiceImpl implements QtiAssessmentResultService
{
	private static final URI URI_SOURCE = URI.create("uri://equella/" + ApplicationVersion.get().getMmr());

	@Inject
	private QtiAssessmentResultDao dao;

	@Inject
	private QtiService qtiService;

	@Transactional
	@Override
	public AssessmentResult persistTestSessionState(QtiAssessmentTest test, TestSessionController testSessionController,
		QtiAssessmentResult qtiAssessmentResult)
	{
		final AssessmentResult assessmentResult = computeAssessmentResult(testSessionController);

		convertItemVariables(qtiAssessmentResult, qtiAssessmentResult.getItemVariables(),
			assessmentResult.getTestResult().getItemVariables());

		final List<QtiItemResult> qtiItemResults = qtiAssessmentResult.getItemResults();
		for( ItemResult itemResult : assessmentResult.getItemResults() )
		{
			final String itemRefId = itemResult.getIdentifier();
			QtiItemResult qtiItemResult = findQtiItemResult(qtiItemResults, itemRefId);
			final boolean newResult = (qtiItemResult == null);

			final QtiAssessmentItemRef qtiAssessmentItemRef = lookupItemRef(test, itemRefId);
			qtiItemResult = convertItemResult(qtiAssessmentResult, qtiAssessmentItemRef, itemResult, qtiItemResult);
			if( newResult )
			{
				qtiItemResults.add(qtiItemResult);
			}
		}

		qtiAssessmentResult.setDatestamp(new Date());
		qtiAssessmentResult.setSessionStatus(calculateOverallSessionStatus(qtiItemResults));

		// serialise the session state
		final Document doc = TestSessionStateXmlMarshaller.marshal(testSessionController.getTestSessionState());
		qtiAssessmentResult.setTestSessionState(new XmlDocument(doc).toString());

		dao.save(qtiAssessmentResult);
		// dao.flush();
		// dao.clear();

		return assessmentResult;
	}

	private QtiAssessmentItemRef lookupItemRef(QtiAssessmentTest test, String itemRefId)
	{
		for( QtiAssessmentItemRef itemRef : test.getQuestionRefs() )
		{
			if( itemRef.getIdentifier().equals(itemRefId) )
			{
				return itemRef;
			}
		}
		throw new RuntimeException("Cannot find itemRef with identifier " + itemRefId);
	}

	@Override
	public TestSessionController loadTestSessionState(ResolvedAssessmentTest resolvedAssessmentTest,
		QtiAssessmentResult qtiAssessmentResult)
	{
		final String testSessionStateXml = (qtiAssessmentResult == null ? null
			: qtiAssessmentResult.getTestSessionState());
		if( Check.isEmpty(testSessionStateXml) )
		{
			return qtiService.getNewTestSessionController(resolvedAssessmentTest);
		}
		else
		{
			final TestSessionState testSessionState = TestSessionStateXmlMarshaller.unmarshal(testSessionStateXml);
			return qtiService.getTestSessionController(resolvedAssessmentTest, testSessionState);
		}
	}

	/**
	 * TODO: somewhere around here would be the enforcment of max attempts
	 * 
	 * @param test
	 * @param resourceLinkId
	 * @param userId
	 * @param toolConsumerInstanceGuid
	 * @return
	 */
	@Override
	public QtiAssessmentResult ensureAssessmentResult(QtiAssessmentTest qtiAssessmentTest, String resourceLinkId,
		String userId, String toolConsumerInstanceGuid)
	{
		QtiAssessmentResult qtiAssessmentResult = getAssessmentResult(qtiAssessmentTest, resourceLinkId, userId,
			toolConsumerInstanceGuid);
		if( qtiAssessmentResult != null )
		{
			return qtiAssessmentResult;
		}

		qtiAssessmentResult = new QtiAssessmentResult();
		qtiAssessmentResult.setTest(qtiAssessmentTest);
		qtiAssessmentResult.setResourceLinkId(resourceLinkId);
		qtiAssessmentResult.setUserId(userId);
		qtiAssessmentResult.setLmsInstanceId(toolConsumerInstanceGuid);
		return qtiAssessmentResult;
	}

	@Transactional
	@Override
	public QtiAssessmentResult getAssessmentResult(QtiAssessmentTest qtiAssessmentTest, String resourceLinkId,
		String userId, String toolConsumerInstanceGuid)
	{
		return dao.getCurrentByResourceLink(qtiAssessmentTest, resourceLinkId, userId, toolConsumerInstanceGuid);
	}

	private QtiSessionStatus calculateOverallSessionStatus(List<QtiItemResult> qtiItemResults)
	{
		QtiSessionStatus minStatus = QtiSessionStatus.FINAL;
		for( QtiItemResult qtiItemResult : qtiItemResults )
		{
			switch( qtiItemResult.getSessionStatus() )
			{
				case INITIAL:
					minStatus = QtiSessionStatus.INITIAL;
					break;
				case PENDING_SUBMISSION:
					if( minStatus != QtiSessionStatus.INITIAL )
					{
						minStatus = QtiSessionStatus.PENDING_SUBMISSION;
					}
					break;
				case PENDING_RESPONSE_PROCESSING:
					if( minStatus != QtiSessionStatus.INITIAL && minStatus != QtiSessionStatus.PENDING_SUBMISSION )
					{
						minStatus = QtiSessionStatus.PENDING_RESPONSE_PROCESSING;
					}
					break;
				default:
					minStatus = QtiSessionStatus.FINAL;
					break;
			}
		}
		return minStatus;
	}

	@Override
	public AssessmentResult computeAssessmentResult(TestSessionController testSessionController)
	{
		final AssessmentResult assessmentResult = testSessionController.computeAssessmentResult(new Date(),
			CurrentUser.getSessionID(), URI_SOURCE);
		return assessmentResult;
	}

	private QtiItemResult findQtiItemResult(List<QtiItemResult> qtiItemResults, String identifier)
	{
		for( QtiItemResult qtiItemResult : qtiItemResults )
		{
			if( qtiItemResult.getItemRef().getIdentifier().equals(identifier) )
			{
				return qtiItemResult;
			}
		}
		return null;
	}

	private QtiItemResult convertItemResult(QtiAssessmentResult qtiAssessmentResult,
		QtiAssessmentItemRef qtiAssessmentItemRef, ItemResult itemResult, QtiItemResult destResult)
	{
		final QtiItemResult qtiItemResult;
		if( destResult == null )
		{
			qtiItemResult = new QtiItemResult();
			qtiItemResult.setAssessmentResult(qtiAssessmentResult);
			qtiItemResult.setItemRef(qtiAssessmentItemRef);
		}
		else
		{
			qtiItemResult = destResult;
		}

		final List<QtiItemVariable> qtiItemVariables = qtiItemResult.getItemVariables();
		convertItemVariables(qtiItemResult, qtiItemVariables, itemResult.getItemVariables());

		// other stuff
		qtiItemResult.setSequenceIndex(itemResult.getSequenceIndex());
		qtiItemResult.setSessionStatus(convertSessionStatus(itemResult.getSessionStatus()));
		qtiItemResult.setDatestamp(itemResult.getDateStamp());
		return qtiItemResult;
	}

	private void convertItemVariables(QtiAbstractResult qtiResult, List<QtiItemVariable> qtiItemVariables,
		List<ItemVariable> itemVariables)
	{
		for( ItemVariable itemVariable : itemVariables )
		{
			QtiItemVariable qtiItemVariable = findQtiItemVariable(qtiItemVariables,
				itemVariable.getIdentifier().toString());
			final boolean newVariable = (qtiItemVariable == null);

			qtiItemVariable = convertItemVariable(qtiResult, itemVariable, qtiItemVariable);
			if( newVariable )
			{
				qtiItemVariables.add(qtiItemVariable);
			}
		}
	}

	private QtiItemVariable findQtiItemVariable(List<QtiItemVariable> qtiItemVariables, String identifier)
	{
		for( QtiItemVariable qtiItemVariable : qtiItemVariables )
		{
			if( qtiItemVariable.getIdentifier().equals(identifier) )
			{
				return qtiItemVariable;
			}
		}
		return null;
	}

	private QtiItemVariable convertItemVariable(QtiAbstractResult qtiResult, ItemVariable itemVariable,
		QtiItemVariable destVariable)
	{
		final QtiItemVariable variable;
		if( destVariable == null )
		{
			variable = new QtiItemVariable();
			variable.setResult(qtiResult);
		}
		else
		{
			variable = destVariable;
		}

		final List<FieldValue> fieldValues;
		if( itemVariable instanceof OutcomeVariable )
		{
			variable.setVariableType(VariableType.OUTCOME);
			final OutcomeVariable ov = (OutcomeVariable) itemVariable;
			fieldValues = ov.getFieldValues();
		}
		else if( itemVariable instanceof ResponseVariable )
		{
			variable.setVariableType(VariableType.RESPONSE);
			final ResponseVariable rv = (ResponseVariable) itemVariable;
			fieldValues = rv.getCandidateResponse().getFieldValues();
		}
		else
		{
			// (TemplateVariables not currently supported)
			fieldValues = Collections.emptyList();
		}

		final List<String> values = variable.getValues();
		values.clear();
		for( FieldValue fieldValue : fieldValues )
		{
			final SingleValue singleValue = fieldValue.getSingleValue();
			values.add(singleValue.toQtiString());
		}

		variable.setBaseType(convertBaseType(itemVariable.getBaseType()));
		variable.setIdentifier(itemVariable.getIdentifier().toString());
		variable.setCardinality(convertCardinality(itemVariable.getCardinality()));
		return variable;
	}

	private QtiSessionStatus convertSessionStatus(SessionStatus sessionStatus)
	{
		switch( sessionStatus )
		{
			case FINAL:
				return QtiSessionStatus.FINAL;
			case INITIAL:
				return QtiSessionStatus.INITIAL;
			case PENDING_RESPONSE_PROCESSING:
				return QtiSessionStatus.PENDING_RESPONSE_PROCESSING;
			case PENDING_SUBMISSION:
				return QtiSessionStatus.PENDING_SUBMISSION;
			default:
				throw new RuntimeException("Unhandled SessionStatus " + sessionStatus.toQtiString());
		}
	}

	private QtiBaseType convertBaseType(BaseType baseType)
	{
		// blank baseType means "RECORD" cardinality, which we aren't
		// supporting.
		if( baseType == null )
		{
			return null;
		}

		// qti works doesn't seem to handle INT_OR_IDENTIFIER ?
		switch( baseType )
		{
			case BOOLEAN:
				return QtiBaseType.BOOLEAN;
			case DIRECTED_PAIR:
				return QtiBaseType.DIRECTED_PAIR;
			case DURATION:
				return QtiBaseType.DURATION;
			case FILE:
				return QtiBaseType.FILE;
			case FLOAT:
				return QtiBaseType.FLOAT;
			case IDENTIFIER:
				return QtiBaseType.IDENTIFIER;
			case INTEGER:
				return QtiBaseType.INTEGER;
			case PAIR:
				return QtiBaseType.PAIR;
			case POINT:
				return QtiBaseType.POINT;
			case STRING:
				return QtiBaseType.STRING;
			case URI:
				return QtiBaseType.URI;

			default:
				throw new RuntimeException("Unhandled BaseType " + baseType.toQtiString());
		}
	}

	private QtiCardinality convertCardinality(Cardinality cardinality)
	{
		switch( cardinality )
		{
			case MULTIPLE:
				return QtiCardinality.MULTIPLE;
			case ORDERED:
				return QtiCardinality.ORDERED;
			case RECORD:
				return QtiCardinality.RECORD;
			case SINGLE:
				return QtiCardinality.SINGLE;
			default:
				throw new RuntimeException("Unhandled Cardinality " + cardinality.toQtiString());
		}
	}

	@Override
	public int countAttemptsByResourceLink(QtiAssessmentTest qtiAssessmentTest, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid)
	{
		return dao.countAttemptsByResourceLink(qtiAssessmentTest, resourceLinkId, userId, toolConsumerInstanceGuid);
	}
}
