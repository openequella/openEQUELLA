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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.exception.QtiAttributeException;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimit;
import uk.ac.ed.ph.jqtiplus.notification.Notification;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationListener;
import uk.ac.ed.ph.jqtiplus.notification.NotificationType;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestPlanner;
import uk.ac.ed.ph.jqtiplus.running.TestProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.value.Value;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.PathUtils;
import com.tle.core.guice.Bind;
import com.tle.core.qti.beans.QtiTestDetails;
import com.tle.core.qti.service.QtiService;
import com.tle.core.services.FileSystemService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiService.class)
@Singleton
public class QtiServiceImpl implements QtiService
{
	private static final Logger LOGGER = Logger.getLogger(QtiService.class);
	private static final JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();

	@Inject
	private FileSystemService fileSystem;

	@Override
	public ResolvedAssessmentTest loadV2Test(FileHandle handle, String basePath, String relativeFilePath)
	{
		final QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager);
		final AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader,
			new FileSystemResourceLocator(fileSystem, handle, basePath));
		final ResolvedAssessmentTest loadAndResolveAssessmentTest = assessmentObjectXmlLoader
			.loadAndResolveAssessmentTest(URI.create(PathUtils.filePath(basePath, relativeFilePath)));

		return loadAndResolveAssessmentTest;
	}

	@Override
	public QtiTestDetails getTestDetails(ResolvedAssessmentTest test)
	{
		final QtiTestDetails details = new QtiTestDetails();
		final AssessmentTest q = test.getTestLookup().extractIfSuccessful();
		if( q == null )
		{
			throw new RuntimeException("Cannot extract assessmentTest from test XML");
		}
		final String title = q.getTitle();
		if( title != null )
		{
			details.setTitle(title);
		}
		details.setToolName(q.getToolName());
		details.setToolVersion(q.getToolVersion());

		// Time limit
		final TimeLimit timeLimit = q.getTimeLimit();
		if( timeLimit != null )
		{
			final Long minimumMillis = timeLimit.getMinimumMillis();
			if( minimumMillis != null )
			{
				details.setMinTime(minimumMillis);
			}

			final Long maximumMillis = timeLimit.getMaximumMillis();
			if( maximumMillis != null )
			{
				details.setMaxTime(maximumMillis);
			}

			try
			{
				// JQTI doesn't seem to know about allowLateSubmission
				final Attribute<?> allowLate = timeLimit.getAttributes().get("allowLateSubmission");
				if( allowLate != null )
				{
					Object allowLateValue = allowLate.getComputedValue();
					details.setAllowLateSubmission(Boolean.valueOf(allowLateValue.toString()));
				}
			}
			catch( QtiAttributeException nsa )
			{
				// who cares?
			}
		}

		final List<TestPart> testParts = q.getTestParts();
		if( testParts != null )
		{
			details.setPartCount(testParts.size());

			// In general there seems to be only ever one
			// testPart element (indeed we and qtiworks only support one test
			// part)
			if( testParts.size() > 0 )
			{
				final TestPart testPart = testParts.get(0);
				details.setNavigationMode(testPart.getNavigationMode());

				final ItemSessionControl itemSessionControl = testPart.getItemSessionControl();
				if( itemSessionControl != null )
				{
					final Boolean allowSkipping = itemSessionControl.getAllowSkipping();
					if( allowSkipping != null )
					{
						details.setAllowSkipping(allowSkipping);
					}
				}
				details.setSectionCount(testPart.getAssessmentSections().size());
			}
		}

		final TestSessionState testSessionState = createNewTestSessionState(test);
		final TestPlan testPlan = testSessionState.getTestPlan();
		final List<TestPlanNode> testPartNodes = testPlan.getTestPartNodes();
		if( testPartNodes != null && testPartNodes.size() > 0 )
		{
			final TestPlanNode testPlanNode = testPartNodes.get(0);
			final List<TestPlanNode> questions = testPlanNode.searchDescendants(TestNodeType.ASSESSMENT_ITEM_REF);
			if( questions != null )
			{
				details.setQuestionCount(questions.size());
			}
		}
		return details;
	}

	private TestSessionState createNewTestSessionState(ResolvedAssessmentTest resolvedAssessmentTest)
	{
		final TestProcessingMap testProcessingMap = new TestProcessingInitializer(resolvedAssessmentTest, true)
			.initialize();
		final TestPlanner testPlanner = new TestPlanner(testProcessingMap);
		testPlanner.addNotificationListener(new LoggingNotificationListener());
		final TestPlan testPlan = testPlanner.generateTestPlan();

		return new TestSessionState(testPlan);
	}

	@Override
	public boolean isResponded(AssessmentItem assessmentItem, ItemSessionState itemState)
	{
		final List<ResponseDeclaration> responseDeclarations = assessmentItem.getResponseDeclarations();
		for( ResponseDeclaration responseDec : responseDeclarations )
		{
			final Value uncommittedResponseValue = itemState.getUncommittedResponseValue(responseDec.getIdentifier());
			if( uncommittedResponseValue != null && !uncommittedResponseValue.isNull() )
			{
				return true;
			}
			final Value responseValue = itemState.getResponseValue(responseDec.getIdentifier());
			if( responseValue != null && !responseValue.isNull() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public TestSessionController getNewTestSessionController(ResolvedAssessmentTest test)
	{
		return getTestSessionController(test, createNewTestSessionState(test));
	}

	@Override
	public TestSessionController getTestSessionController(ResolvedAssessmentTest test, TestSessionState testSessionState)
	{
		final TestProcessingMap testProcessingMap = new TestProcessingInitializer(test, true).initialize();
		final TestSessionControllerSettings testSessionControllerSettings = new TestSessionControllerSettings();
		final TestSessionController testSessionController = new TestSessionController(jqtiExtensionManager,
			testSessionControllerSettings, testProcessingMap, testSessionState);
		testSessionController.addNotificationListener(new LoggingNotificationListener());
		return testSessionController;
	}

	private class LoggingNotificationListener implements NotificationListener
	{
		@Override
		public void onNotification(Notification notification)
		{
			final NotificationType notificationType = notification.getNotificationType();
			final NotificationLevel level = notification.getNotificationLevel();
			final String message = notificationType.toString() + " " + notification.getMessage();
			switch( level )
			{
				case ERROR:
					LOGGER.error(message);
					break;
				case INFO:
					LOGGER.info(message);
					break;
				case WARNING:
					LOGGER.warn(message);
					break;
				default:
					LOGGER.info(message);
					break;
			}
		}
	}
}
