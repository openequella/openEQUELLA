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

package com.tle.web.qti.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.qti.QtiConstants;
import com.tle.core.qti.beans.QtiTestDetails;
import com.tle.core.qti.service.QtiAssessmentResultService;
import com.tle.core.qti.service.QtiAssessmentTestService;
import com.tle.core.qti.service.QtiService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.LtiData.LisData;
import com.tle.web.lti.service.LtiService;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.qti.viewer.QtiViewerConstants;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;

import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.node.result.TestResult;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * Ties together LTI service and QtiService
 * 
 * @author Aaron
 */
@NonNullByDefault
@Bind(QtiWebService.class)
@Singleton
public class QtiWebServiceImpl implements QtiWebService
{
	private static final String SCORE_VARIABLE = "SCORE";

	private static final Cache<String, ResolvedAssessmentTest> assessmentTestCache = CacheBuilder.newBuilder()
		.concurrencyLevel(4).expireAfterWrite(3, TimeUnit.HOURS).build();

	@Inject
	private QtiService qtiService;
	@Inject
	private QtiAssessmentResultService qtiResultService;
	@Inject
	private QtiAssessmentTestService qtiTestService;
	@Inject
	private LtiService ltiService;
	@Inject
	private UserSessionService sessionService;
	@Inject
	private ViewItemUrlFactory itemUrls;

	@Override
	public void startTest(SectionInfo info, ViewItemResource resource)
	{
		final TestSessionState testSessionState = getTestSessionState(info, resource);
		final TestSessionController testSessionController = getTestSessionController(info, resource, testSessionState);
		final Date timestamp = new Date();
		testSessionController.initialize(timestamp);
		testSessionController.enterTest(timestamp);

		final TestPlan testPlan = testSessionState.getTestPlan();

		final List<TestPlanNode> testPartNodes = testPlan.getTestPartNodes();
		if( testPartNodes.size() > 1 )
		{
			throw new Error("Only one test part currently supported");
		}
		testSessionState.setCurrentTestPartKey(null);
		testSessionState.setCurrentItemKey(null);
		final TestPlanNode testPart = testSessionController.enterNextAvailableTestPart(timestamp);
		final List<TestPlanNode> questions = testPart.searchDescendants(TestNodeType.ASSESSMENT_ITEM_REF);

		// If mode is linear, enterNextTestPart has already selected the first
		// question
		// Otherwise we select the first question below:
		if( testSessionController.getCurrentTestPart().getNavigationMode() == NavigationMode.NONLINEAR )
		{
			// select the first available question
			if( questions.size() > 0 )
			{
				final TestPlanNode question = questions.get(0);
				testSessionController.selectItemNonlinear(timestamp, question.getKey());
			}
		}

		updateSessionState(info, resource, testSessionController, testSessionState);
	}

	@Override
	public void cancelTest(SectionInfo info, ViewItemResource resource)
	{
		LtiData ltiData = getLtiData();
		if( ltiData != null )
		{
			info.forwardToUrl(ltiData.getReturnUrl());
		}
		else
		{
			info.forwardToUrl(itemUrls.createItemUrl(info, resource.getViewableItem().getItemId()).getHref());
		}
	}

	/**
	 * If you call this with persist=false then you MUST call updateSessionState
	 * some time afterwards
	 * 
	 * @param info
	 * @param session
	 * @param testSessionController
	 */
	@Override
	public void readFormValues(SectionInfo info, ViewItemResource resource, boolean persist)
	{
		final TestSessionState testSessionState = getTestSessionState(info, resource);
		final TestSessionController testSessionController = getTestSessionController(info, resource, testSessionState);

		final ItemSessionState currentItemSessionState = testSessionState.getCurrentItemSessionState();
		if( currentItemSessionState != null && !currentItemSessionState.isEnded() )
		{
			// submit values into the session state
			if( testSessionState.getCurrentItemKey() == null
				|| !testSessionController.maySubmitResponsesToCurrentItem() )
			{
				throw new RuntimeException("Cannot submit responses in the current state");
			}

			final Map<Identifier, StringResponseData> stringResponseMap = extractStringResponseData(info.getRequest());
			final Map<Identifier, ResponseData> responseDataMap = new HashMap<Identifier, ResponseData>();
			for( final Entry<Identifier, StringResponseData> stringResponseEntry : stringResponseMap.entrySet() )
			{
				final Identifier identifier = stringResponseEntry.getKey();
				final StringResponseData stringResponseData = stringResponseEntry.getValue();
				responseDataMap.put(identifier, stringResponseData);
			}
			testSessionController.handleResponsesToCurrentItem(new Date(), responseDataMap);
		}
		if( persist )
		{
			updateSessionState(info, resource, testSessionState);
		}
	}

	/**
	 * @param info
	 * @param resource
	 * @return
	 */
	@Override
	public TestSessionState getTestSessionState(SectionInfo info, ViewItemResource resource)
	{
		TestSessionState qtiSessionState = info.getAttributeForClass(TestSessionState.class);
		if( qtiSessionState == null )
		{
			final TestSessionController dbState = getDbState(info, resource);
			if( dbState != null )
			{
				setTestSessionController(info, dbState);
				qtiSessionState = dbState.getTestSessionState();
			}
			else
			{
				final String sessionKey = getSessionKey(resource);
				qtiSessionState = sessionService.getAttribute(sessionKey);
				if( qtiSessionState == null )
				{
					final TestSessionController newTestSessionController = qtiService
						.getNewTestSessionController(getResolvedTest(info, resource));
					setTestSessionController(info, newTestSessionController);

					qtiSessionState = newTestSessionController.getTestSessionState();
				}
			}
			info.setAttribute(TestSessionState.class, qtiSessionState);
		}
		return qtiSessionState;
	}

	@Override
	public TestSessionController getTestSessionController(SectionInfo info, ViewItemResource resource)
	{
		return getTestSessionController(info, resource, getTestSessionState(info, resource));
	}

	@Override
	public void submitTest(SectionInfo info, ViewItemResource resource)
	{
		// persist the submitted values
		readFormValues(info, resource, true);
		endTest(info, resource);
	}

	@Transactional
	void endTest(SectionInfo info, ViewItemResource resource)
	{
		final TestSessionState testSessionState = getTestSessionState(info, resource);
		final TestSessionController testSessionController = getTestSessionController(info, resource);

		if( testSessionController.mayEndCurrentTestPart() )
		{
			// performs outcome processing for us
			testSessionController.endCurrentTestPart(new Date());
		}
		else
		{
			throw new RuntimeException("Cannot end test part for whatever reason");
		}

		final AssessmentResult result = qtiResultService.computeAssessmentResult(testSessionController);

		final TestResult testResult = result.getTestResult();
		OutcomeVariable score = null;

		final List<ItemVariable> itemVariables = testResult.getItemVariables();
		for( ItemVariable var : itemVariables )
		{
			if( var instanceof OutcomeVariable )
			{
				if( var.getIdentifier().toString().equals(SCORE_VARIABLE) )
				{
					score = (OutcomeVariable) var;
				}
			}
		}

		if( score != null )
		{
			final Value computedValue = score.getComputedValue();
			if( computedValue != null && !computedValue.isNull() )
			{
				sendGrades(computedValue.toQtiString());
			}
		}

		// persist the results
		updateSessionState(info, resource, testSessionController, testSessionState);
	}

	@Override
	public void selectQuestion(SectionInfo info, ViewItemResource resource, String key, int direction)
	{
		readFormValues(info, resource, false);

		final TestSessionState testSessionState = getTestSessionState(info, resource);
		final TestSessionController testSessionController = getTestSessionController(info, resource);

		// move to prev or next question
		final TestPlanNode newNode;
		if( key != null )
		{
			final TestPlanNodeKey nodeKey = TestPlanNodeKey.fromString(key);
			final TestPlan testPlan = testSessionState.getTestPlan();
			newNode = testPlan.getNode(nodeKey);
		}
		else if( direction == 0 )
		{
			newNode = null;
		}
		else
		{
			newNode = findRelativeQuestion(testSessionState, direction);
		}
		setActiveAssessmentItem(testSessionController, testSessionState, newNode);
		updateSessionState(info, resource, testSessionController, testSessionState);
	}

	@Nullable
	@Override
	public LtiData getLtiData()
	{
		UserState userState = CurrentUser.getUserState();
		if( userState instanceof LtiUserState )
		{
			return ((LtiUserState) userState).getData();
		}
		return null;
	}

	private void setActiveAssessmentItem(TestSessionController testSessionController, TestSessionState testSessionState,
		@Nullable TestPlanNode testPlanNode)
	{
		final TestPlan testPlan = testSessionState.getTestPlan();
		final TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		if( currentItemKey != null )
		{
			final ItemSessionController itemSessionController = (ItemSessionController) testSessionController
				.getItemProcessingContext(testPlan.getNode(currentItemKey));
			if( !itemSessionController.getItemSessionState().isEnded() )
			{
				itemSessionController.suspendItemSession(new Date());
			}
		}

		testSessionState.setCurrentItemKey(testPlanNode == null ? null : testPlanNode.getKey());
		if( testPlanNode != null )
		{
			final ItemSessionController itemSessionController = (ItemSessionController) testSessionController
				.getItemProcessingContext(testPlanNode);
			// Either enter or unsuspend
			final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
			if( !itemSessionState.isEnded() )
			{
				if( itemSessionState.isSuspended() )
				{
					itemSessionController.unsuspendItemSession(new Date());
				}
				else
				{
					itemSessionController.enterItem(new Date());
				}
			}
		}
	}

	private TestPlanNode findRelativeQuestion(TestSessionState testSessionState, int direction)
	{
		final TestPlanNode currentTestPartNode = testSessionState.getTestPlan()
			.getNode(testSessionState.getCurrentTestPartKey());

		/* Find next item */
		final TestPlan testPlan = testSessionState.getTestPlan();
		final List<TestPlanNode> itemsInTestPart = currentTestPartNode
			.searchDescendants(TestNodeType.ASSESSMENT_ITEM_REF);
		final TestPlanNodeKey itemKey = testSessionState.getCurrentItemKey();
		final TestPlanNode nextItemRefNode;
		if( itemKey == null )
		{
			/* Haven't entered any items yet, so select first if available */
			nextItemRefNode = !itemsInTestPart.isEmpty() ? itemsInTestPart.get(0) : null;
		}
		else
		{
			final TestPlanNode currentItem = testPlan.getNode(itemKey);
			final int currentItemIndex = itemsInTestPart.indexOf(currentItem);
			// wraps around
			if( direction < 0 )
			{
				nextItemRefNode = currentItemIndex - 1 >= 0 ? itemsInTestPart.get(currentItemIndex - 1)
					: itemsInTestPart.get(itemsInTestPart.size() - 1);
			}
			else
			{
				nextItemRefNode = currentItemIndex + 1 < itemsInTestPart.size()
					? itemsInTestPart.get(currentItemIndex + 1) : itemsInTestPart.get(0);
			}
		}
		return nextItemRefNode;
	}

	/**
	 * Based on (copied and pasted from)
	 * uk.ac.ed.ph.qtiworks.web.controller.candidate.CandidateItemController
	 * .extractStringResponseData
	 */
	private Map<Identifier, StringResponseData> extractStringResponseData(final HttpServletRequest request)
	{
		final Map<Identifier, StringResponseData> responseMap = new HashMap<Identifier, StringResponseData>();
		final Set<String> parameterNames = request.getParameterMap().keySet();
		for( final String name : parameterNames )
		{
			if( name.startsWith(QtiViewerConstants.CONTROL_PREFIX) )
			{
				final String responseIdentifierString = name.substring(QtiViewerConstants.CONTROL_PREFIX.length());
				final Identifier responseIdentifier;
				try
				{
					responseIdentifier = Identifier.parseString(responseIdentifierString);
				}
				catch( final QtiParseException e )
				{
					throw new RuntimeException("Bad response identifier encoded in parameter  " + name, e);
				}
				final String[] responseValues = request
					.getParameterValues(QtiViewerConstants.CONTROL_PREFIX + responseIdentifierString);

				if( responseValues != null )
				{
					final StringResponseData stringResponseData = new StringResponseData(responseValues);
					responseMap.put(responseIdentifier, stringResponseData);
				}
			}
		}
		return responseMap;
	}

	private String getToolConsumerInstanceGuid(LtiData ltiData)
	{
		final String toolConsumerInstanceGuid = ltiData.getToolConsumerInstanceGuid();
		if( toolConsumerInstanceGuid != null )
		{
			return toolConsumerInstanceGuid;
		}

		final String toolConsumerInfoProductFamilyCode = ltiData.getToolConsumerInfoProductFamilyCode();
		if( toolConsumerInfoProductFamilyCode != null )
		{
			return toolConsumerInfoProductFamilyCode;
		}
		return "unknown";
	}

	private QtiAssessmentTest getQtiAssessmentTest(SectionInfo info, String testUuid)
	{
		QtiAssessmentTest qtiAssessmentTest = info.getAttributeForClass(QtiAssessmentTest.class);
		if( qtiAssessmentTest == null )
		{
			qtiAssessmentTest = qtiTestService.getByUuid(testUuid);
			info.setAttribute(QtiAssessmentTest.class, qtiAssessmentTest);
		}
		return qtiAssessmentTest;
	}

	private TestSessionController getTestSessionController(SectionInfo info, ViewItemResource resource,
		TestSessionState testSessionState)
	{
		TestSessionController testSessionController = info.getAttributeForClass(TestSessionController.class);
		if( testSessionController == null )
		{
			testSessionController = qtiService.getTestSessionController(getResolvedTest(info, resource),
				testSessionState);
			info.setAttribute(TestSessionController.class, testSessionController);
		}
		return testSessionController;
	}

	private void setTestSessionController(SectionInfo info, TestSessionController testSessionController)
	{
		info.setAttribute(TestSessionController.class, testSessionController);
	}

	/**
	 * Returns null if there is no LTI data and we should use the session
	 * 
	 * @param info
	 * @param resource
	 * @return
	 */
	@Nullable
	private TestSessionController getDbState(SectionInfo info, ViewItemResource resource)
	{
		final LtiData ltiData = getLtiData();
		if( ltiData != null )
		{
			final LisData lisData = ltiData.getLisData();
			if( lisData != null )
			{
				final String outcomeServiceUrl = lisData.getOutcomeServiceUrl();
				final String sourcedId = lisData.getResultSourcedid();
				if( outcomeServiceUrl != null && sourcedId != null )
				{
					final CustomAttachment qtiAttachment = getAttachment(resource);
					final String testUuid = (String) qtiAttachment.getData(QtiConstants.KEY_TEST_UUID);
					final QtiAssessmentTest qtiAssessmentTest = qtiTestService.getByUuid(testUuid);
					final QtiAssessmentResult qtiAssessmentResult = qtiResultService.getAssessmentResult(
						qtiAssessmentTest, ltiData.getResourceLinkId(), ltiData.getUserId(),
						getToolConsumerInstanceGuid(ltiData));
					return qtiResultService.loadTestSessionState(getResolvedTest(info, resource), qtiAssessmentResult);
				}
			}
		}
		return null;
	}

	private void updateSessionState(SectionInfo info, ViewItemResource resource, TestSessionState session)
	{
		updateSessionState(info, resource, getTestSessionController(info, resource, session), session);
	}

	private void updateSessionState(SectionInfo info, ViewItemResource resource,
		TestSessionController testSessionController, TestSessionState session)
	{
		if( !updateDbState(info, resource, testSessionController) )
		{
			final String sessionKey = getSessionKey(resource);
			sessionService.setAttribute(sessionKey, session);
		}
	}

	private boolean updateDbState(SectionInfo info, ViewItemResource resource,
		TestSessionController testSessionController)
	{
		// save out the variables and set to sessionStatus = INITIAL but only if
		// we were launched from LTI and we have a return URL+sourcedId
		final LtiData ltiData = getLtiData();
		if( ltiData != null )
		{
			final LisData lisData = ltiData.getLisData();
			if( lisData != null )
			{
				final String outcomeServiceUrl = lisData.getOutcomeServiceUrl();
				final String sourcedId = lisData.getResultSourcedid();
				if( outcomeServiceUrl != null && sourcedId != null )
				{
					final CustomAttachment qtiAttachment = getAttachment(resource);
					final String testUuid = (String) qtiAttachment.getData(QtiConstants.KEY_TEST_UUID);
					final QtiAssessmentTest qtiAssessmentTest = getQtiAssessmentTest(info, testUuid);
					final QtiAssessmentResult qtiAssessmentResult = qtiResultService.ensureAssessmentResult(
						qtiAssessmentTest, ltiData.getResourceLinkId(), ltiData.getUserId(),
						getToolConsumerInstanceGuid(ltiData));
					qtiResultService.persistTestSessionState(qtiAssessmentTest, testSessionController,
						qtiAssessmentResult);
					return true;
				}
			}
		}
		return false;
	}

	private String getSessionKey(ViewItemResource resource)
	{
		return CurrentInstitution.get().getDatabaseId() + ":" + resource.getViewableItem().getItemId().toString();
	}

	@Override
	public ResolvedAssessmentTest getResolvedTest(final SectionInfo info, ViewItemResource resource)
	{
		final CustomAttachment qti = getAttachment(resource);
		final ViewableItem viewableItem = resource.getViewableItem();

		final String xmlLoc = (String) qti.getData(QtiConstants.KEY_XML_PATH);

		// if not a real item then don't use cache
		if( !viewableItem.isItemForReal() )
		{
			return loadAssessmentTest(info, viewableItem, xmlLoc, null);
		}

		final String testUuid = (String) qti.getData(QtiConstants.KEY_TEST_UUID);
		try
		{
			return assessmentTestCache.get(getCacheKey(testUuid), new Callable<ResolvedAssessmentTest>()
			{
				@Override
				public ResolvedAssessmentTest call() throws Exception
				{
					return loadAssessmentTest(info, viewableItem, xmlLoc, testUuid);
				}
			});
		}
		catch( ExecutionException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public QtiTestDetails getTestDetails(ResolvedAssessmentTest test)
	{
		return qtiService.getTestDetails(test);
	}

	@Override
	public boolean isResponded(AssessmentItem assessmentItem, ItemSessionState itemSessionState)
	{
		return qtiService.isResponded(assessmentItem, itemSessionState);
	}

	private String getCacheKey(String testUuid)
	{
		final Institution institution = CurrentInstitution.get();
		return institution.getUniqueId() + ":" + testUuid;
	}

	private ResolvedAssessmentTest loadAssessmentTest(SectionInfo info, ViewableItem viewableItem,
		@Nullable String xmlLoc, @Nullable String testUuid)
	{
		String xml = xmlLoc;
		if( xml == null && testUuid != null )
		{
			xml = getQtiAssessmentTest(info, testUuid).getXmlPath();
		}
		if( xml != null )
		{
			final FileHandle handle = viewableItem.getFileHandle();
			final String xmlRelPath = PathUtils.relativize(QtiConstants.QTI_FOLDER_PATH, xml);
			final ResolvedAssessmentTest test = qtiService.loadV2Test(handle, QtiConstants.QTI_FOLDER_PATH, xmlRelPath);

			final AssessmentTest t = test.getTestLookup().extractIfSuccessful();
			for( TestPart tp : t.getTestParts() )
			{
				// !! Only whole submission mode is currently supported:
				tp.setSubmissionMode(SubmissionMode.SIMULTANEOUS);
				// !! Only NONLINEAR navigation mode is supported
				tp.setNavigationMode(NavigationMode.NONLINEAR);
			}
			return test;
		}
		throw new Error("No test associated with attachment");
	}

	private void sendGrades(String grade)
	{
		final LtiData ltiData = getLtiData();
		if( ltiData != null )
		{
			// We need an outcome URL and a sourcedId, otherwise it won't work
			final LisData lisData = ltiData.getLisData();
			if( lisData != null )
			{
				final String outcomeServiceUrl = lisData.getOutcomeServiceUrl();
				final String resultSourcedid = lisData.getResultSourcedid();
				if( !Strings.isNullOrEmpty(resultSourcedid) && !Strings.isNullOrEmpty(outcomeServiceUrl) )
				{
					ltiService.sendGrade(ltiData, grade);
				}
			}
		}
	}

	private CustomAttachment getAttachment(ViewItemResource resource)
	{
		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		return (CustomAttachment) viewableResource.getAttachment();
	}
}
