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

package com.tle.web.qti.viewer;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.qti.QtiConstants;
import com.tle.core.qti.beans.QtiTestDetails;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.lti.LtiData;
import com.tle.web.qti.service.QtiWebService;
import com.tle.web.qti.viewer.QtiPlayViewerSection.QTIPlayViewerModel;
import com.tle.web.qti.viewer.questions.QuestionRenderers;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.NavBar;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.FullScreen;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.ResourceViewerAware;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPartSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.Value;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class QtiPlayViewerSection extends AbstractViewerSection<QTIPlayViewerModel>
	implements
		ResourceViewerAware,
		HtmlRenderer
{
	@PlugKey("viewer.label.questions")
	private static Label LABEL_QUESTIONS;
	@PlugKey("viewer.results.title")
	private static Label RESULTS_LABEL;
	@PlugKey("viewer.heading.finalscore")
	private static String KEY_FINALSCORE;
	@PlugKey("viewer.link.return.namedcourse")
	private static String KEY_RETURN_TO_NAMED_COURSE;
	@PlugKey("viewer.link.return.course")
	private static Label LABEL_RETURN_TO_COURSE;
	@PlugKey("viewer.confirm.submit")
	private static Label LABEL_CONFIRM_SUBMIT;
	@PlugKey("viewer.error.protectedresource")
	private static String KEY_ERROR_PROTECTED_RESOURCE;
	@PlugKey("viewer.error.outsidepackage")
	private static String KEY_ERROR_OUTSIDE_PACKAGE;

	@Inject
	private ViewItemUrlFactory itemUrls;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private QuestionRenderers questionRenderers;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private QtiWebService qtiWebService;

	@TreeLookup
	private RootItemFileSection rootFileSection;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	@Component
	private NavBar navBar;
	@Component
	private Link title;
	@Component
	private Link returnLink;

	@Component
	@PlugKey("viewer.button.start")
	private Button startButton;
	@Component
	@PlugKey("viewer.button.cancel")
	private Button cancelButton;
	@Component
	@PlugKey("viewer.button.previous")
	private Button previousButton;
	@Component
	@PlugKey("viewer.button.next")
	private Button nextButton;
	@Component
	@PlugKey("viewer.button.submit")
	private Button submitButton;
	@Component
	@PlugKey("viewer.button.viewresult")
	private Button viewResultButton;

	private UpdateDomFunction updateLeftAndRightFunction;
	private UpdateDomFunction questionSelectFunction;

	/**
	 * This is required for the AJAX updates, otherwise nothing is rendered
	 */
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws IOException
	{
		return view(context, rootFileSection.getViewItemResource(context));
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource) throws IOException
	{
		Decorations.getDecorations(info).clearAllDecorations();
		Decorations.getDecorations(info).setFullscreen(FullScreen.YES_WITH_TOOLBAR);

		final ViewableItem<Item> viewableItem = resource.getViewableItem();
		final Item item = viewableItem.getItem();

		title.setLabel(info, new BundleLabel(item.getName(), item.getUuid(), bundleCache));
		if( viewableItem.isItemForReal() )
		{
			title.setBookmark(info, itemUrls.createItemUrl(info, item.getItemId()));
		}

		setupCloseLinks(info);

		final QTIPlayViewerModel model = getModel(info);

		final ResolvedAssessmentTest quiz = qtiWebService.getResolvedTest(info, resource);

		final TestSessionState testSessionState = qtiWebService.getTestSessionState(info, resource);
		final TestPlan testPlan = testSessionState.getTestPlan();
		final TestPlanNodeKey currentTestPartKey = testSessionState.getCurrentTestPartKey();
		if( currentTestPartKey != null )
		{
			final TestPlanNode testPart = testPlan.getNode(currentTestPartKey);
			final TestPartSessionState testPartSessionState = testSessionState.getTestPartSessionStates()
				.get(testPart.getKey());

			if( testPartSessionState.isEntered() || testPartSessionState.isEnded() )
			{
				final TestPlanNodeKey key = testSessionState.getCurrentItemKey();
				final boolean ended = testPartSessionState.isEnded();

				model.setSections(populateSectionModels(info, resource, quiz, testPart, testSessionState, ended));

				if( key == null && ended )
				{
					model.setQuestionTitle(RESULTS_LABEL);
					model.setQuestionRenderable(renderEndSummary(info, resource, quiz, testSessionState));
				}

				submitButton.setDisplayed(info, !ended);
				viewResultButton.setDisplayed(info, ended);

				return viewFactory.createResult("viewer/qtiplayviewer.ftl", this);
			}
		}

		final QtiTestDetails details = qtiWebService.getTestDetails(quiz);
		model.setQuestionCount(details.getQuestionCount());
		model.setSectionCount(details.getSectionCount());
		return viewFactory.createResult("viewer/qtitestsummary.ftl", this);
	}

	private void setupCloseLinks(RenderContext info)
	{
		returnLink.setDisplayed(info, false);
		cancelButton.setDisplayed(info, false);
		final LtiData ltiData = qtiWebService.getLtiData();
		if( ltiData != null )
		{
			// disable title link
			final String resourceLinkTitle = ltiData.getResourceLinkTitle();
			if( !Strings.isNullOrEmpty(resourceLinkTitle) )
			{
				title.setLabel(info, new TextLabel(resourceLinkTitle));
			}
			title.disable(info);
			title.getState(info).addClass("disabled");

			final String courseTitle = ltiData.getContextTitle();
			if( courseTitle != null )
			{
				returnLink.setLabel(info, new KeyLabel(KEY_RETURN_TO_NAMED_COURSE, new TextLabel(courseTitle)));
			}
			else
			{
				returnLink.setLabel(info, LABEL_RETURN_TO_COURSE);
			}

			final String returnUrl = ltiData.getReturnUrl();
			if( returnUrl != null )
			{
				returnLink.setBookmark(info, new SimpleBookmark(returnUrl));
				returnLink.setDisplayed(info, true);
				cancelButton.setClickHandler(info, events.getNamedHandler("onCancelTest"));
				cancelButton.setDisplayed(info, true);
			}
			else
			{
				final String launchPresentation = ltiData.getLaunchPresentationDocumentTarget();
				if( launchPresentation != null )
				{
					if( launchPresentation.equals("window") )
					{
						final JSHandler closeWindow = Js.handler(ScriptStatement.WINDOW_CLOSE);
						returnLink.setClickHandler(info, closeWindow);
						returnLink.setDisplayed(info, true);
						cancelButton.setClickHandler(info, closeWindow);
						cancelButton.setDisplayed(info, true);
					}
				}
			}
		}
	}

	@EventHandlerMethod
	public void onCancelTest(SectionInfo info)
	{
		qtiWebService.cancelTest(info, rootFileSection.getViewItemResource(info));
	}

	private List<TestSectionModel> populateSectionModels(RenderContext context, ViewItemResource resource,
		ResolvedAssessmentTest resolvedAssessmentTest, TestPlanNode testPartNode, TestSessionState testSessionState,
		boolean ended)
	{
		final QTIPlayViewerModel model = getModel(context);

		final TestSessionController testSessionController = qtiWebService.getTestSessionController(context, resource);

		final TestProcessingMap testProcessingMap = testSessionController.getTestProcessingMap();
		List<TestPlanNode> sectionsOrRoots = testPartNode.searchDescendants(TestNodeType.ASSESSMENT_SECTION);
		if( sectionsOrRoots.size() == 0 )
		{
			sectionsOrRoots = Lists.newArrayList(testPartNode);
		}

		final String testRootPath = PathUtils
			.getParentFolderFromFilepath(resolvedAssessmentTest.getTestLookup().getSystemId().toString());
		final Map<URI, ResolvedAssessmentItem> resolvedAssessmentItemBySystemIdMap = resolvedAssessmentTest
			.getResolvedAssessmentItemBySystemIdMap();
		final TestPlanNodeKey currentQuestionKey = testSessionState.getCurrentItemKey();

		final List<TestSectionModel> sectionModels = Lists.newArrayList();
		for( TestPlanNode sectionOrRootNode : sectionsOrRoots )
		{
			final TestSectionModel testSectionModel = new TestSectionModel();

			if( sectionOrRootNode.getTestNodeType() == TestNodeType.ASSESSMENT_SECTION )
			{
				final AssessmentSection section = (AssessmentSection) testProcessingMap
					.resolveAbstractPart(sectionOrRootNode);
				testSectionModel.setTitle(new TextLabel(section.getTitle()));
			}
			else if( sectionOrRootNode.getTestNodeType() == TestNodeType.TEST_PART )
			{
				testSectionModel.setTitle(LABEL_QUESTIONS);
			}

			final List<QuestionModel> questionModels = Lists.newArrayList();
			testSectionModel.setQuestions(questionModels);

			final List<TestPlanNode> questions = sectionOrRootNode.searchDescendants(TestNodeType.ASSESSMENT_ITEM_REF);

			int qIndex = 0;
			for( TestPlanNode questionNode : questions )
			{
				qIndex++;
				boolean allowReviewForQuestion = questionNode.getEffectiveItemSessionControl().isAllowReview();

				final AssessmentItemRef assessmentItemRef = (AssessmentItemRef) testProcessingMap
					.resolveAbstractPart(questionNode);

				final URI uri = assessmentItemRef.getHref();
				// this is a path relative to the test XML
				final String relPath = uri.toString();
				final String fullItemPath = PathUtils.filePath(testRootPath, relPath);
				final ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentItemBySystemIdMap
					.get(URI.create(fullItemPath));
				final AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().extractIfSuccessful();
				final TestPlanNodeKey questionKey = questionNode.getKey();

				final QuestionModel qm = new QuestionModel();
				String questionId = questionKey.toString();

				qm.setId(questionId);
				final JSHandler handler = new OverrideHandler(questionSelectFunction, questionId, 0);
				final HtmlLinkState qlink = new HtmlLinkState(new TextLabel(assessmentItem.getTitle()), handler);
				SimpleElementId qeid = new SimpleElementId("q" + qIndex);
				qeid.registerUse();
				qlink.setElementId(qeid);

				final boolean selected = questionKey.equals(currentQuestionKey);
				if( selected )
				{
					if( !ended || (ended && allowReviewForQuestion) )
					{
						model.setQuestionTitle(new TextLabel(assessmentItem.getTitle()));

						final QtiNodeRenderer questionRenderable = questionRenderers.chooseRenderer(assessmentItem,
							new PlayerContext(context, resource));
						model.setQuestionRenderable(questionRenderable);
					}
				}

				final ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(questionKey);
				final StringBuilder styleClass = new StringBuilder("question");

				if( ended && !allowReviewForQuestion )
				{
					qlink.setDisabled(true);
				}

				qm.setLink(qlink);

				if( selected )
				{
					styleClass.append(" active");
				}
				if( qtiWebService.isResponded(assessmentItem, itemSessionState) )
				{
					styleClass.append(" answered");
				}
				if( itemSessionState.isPresented() )
				{
					styleClass.append(" seen");
				}
				qm.setStyleClass(styleClass.toString());

				questionModels.add(qm);
			}

			sectionModels.add(testSectionModel);
		}
		return sectionModels;
	}

	private SectionRenderable renderEndSummary(RenderContext info, ViewItemResource resource,
		ResolvedAssessmentTest resolvedAssessmentTest, TestSessionState testSessionState)
	{
		getModel(info).setHideNavigationButtons(true);

		final EndSummaryModel model = new EndSummaryModel();
		final Value outcomeValue = testSessionState.getOutcomeValue(Identifier.parseString("SCORE"));
		if( outcomeValue != null && !outcomeValue.isNull() )
		{
			final double finalScore = Double.parseDouble(outcomeValue.toQtiString());
			model.setHeading(new KeyLabel(KEY_FINALSCORE, finalScore));
		}

		final PlayerContext viewerContext = new PlayerContext(info, resource);
		final List<TestFeedback> feedbacks = resolvedAssessmentTest.getTestLookup().extractIfSuccessful()
			.getTestFeedbacks();
		final List<SectionRenderable> feedbackRenderables = Lists.newArrayList();
		for( TestFeedback feedback : feedbacks )
		{
			feedbackRenderables.add(questionRenderers.chooseRenderer(feedback, viewerContext));
		}
		model.setFeedbacks(feedbackRenderables);

		return viewFactory.createResultWithModel("viewer/qtiresultsummary.ftl", model);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		navBar.setTitle(title);
		navBar.buildRight().divider().action(returnLink);

		updateLeftAndRightFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("onValueChange"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "test-questions-container", "question-header",
			"question-body-container");
		questionSelectFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("onQuestionSelect"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "test-questions-container", "question-header",
			"question-body-container");

		previousButton.setClickHandler(new OverrideHandler(questionSelectFunction, "", -1));
		nextButton.setClickHandler(new OverrideHandler(questionSelectFunction, "", 1));

		startButton.setClickHandler(events.getNamedHandler("onStartTest"));
		submitButton
			.setClickHandler(events.getNamedHandler("onSubmitTest").addValidator(new Confirm(LABEL_CONFIRM_SUBMIT)));
		viewResultButton.setClickHandler(events.getNamedHandler("onViewResult"));
	}

	@EventHandlerMethod
	public void onStartTest(SectionInfo info)
	{
		qtiWebService.startTest(info, rootFileSection.getViewItemResource(info));
	}

	/**
	 * Serve up the image (or whatever) relative to the current question (if
	 * any)
	 * 
	 * @param info
	 * @param href
	 */
	@EventHandlerMethod(preventXsrf = false)
	public void viewResource(SectionInfo info, String href)
	{
		final ViewItemResource resource = rootFileSection.getViewItemResource(info);
		final TestSessionState testSessionState = qtiWebService.getTestSessionState(info, resource);
		final TestPlan testPlan = testSessionState.getTestPlan();
		final CustomAttachment qti = getAttachment(resource);
		final String testXmlPath = (String) qti.getData(QtiConstants.KEY_XML_PATH);

		final String path;
		final TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
		if( currentItemKey != null )
		{
			final TestPlanNode questionNode = testPlan.getNode(currentItemKey);
			final URI itemSystemId = questionNode.getItemSystemId();
			final String questionXmlPath = itemSystemId.toString();
			// relative to question XML file
			path = PathUtils.filePath(PathUtils.getParentFolderFromFilepath(questionXmlPath), href);
		}
		else
		{
			// relative to test XML file
			path = PathUtils.filePath(PathUtils.getParentFolderFromFilepath(testXmlPath), href);
		}
		// normalize the path, i.e. remove any ".."
		String finalPath = PathUtils.normalizePath(path);
		if( finalPath.charAt(0) == '/' && finalPath.length() > 1 )
		{
			finalPath = finalPath.substring(1);
		}

		final FileHandle fileHandle = resource.getViewableItem().getFileHandle();
		if( !fileSystemService.fileExists(fileHandle, finalPath) )
		{
			throw new NotFoundException(href);
		}

		// TODO: this is sub-optimal. You can use Path.isParent(Path p2) or
		// similar, but Paths uses the default file system, which may not be
		// appropriate...
		if( !finalPath.toUpperCase()
			.startsWith(PathUtils.filePath(FileSystemService.SECURE_FOLDER, QtiConstants.QTI_FOLDER_NAME)) )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_OUTSIDE_PACKAGE));
		}

		if( matchesProtectedFile(testPlan, fileHandle, testXmlPath, finalPath) )
		{
			throw new AccessDeniedException(CurrentLocale.get(KEY_ERROR_PROTECTED_RESOURCE));
		}

		final FileContentStream stream = fileSystemService.getInsecureContentStream(fileHandle, finalPath,
			mimeService.getMimeTypeForFilename(finalPath));
		stream.setCacheControl("max-age=86400, s-maxage=0, must-revalidate");
		contentStreamWriter.outputStream(info.getRequest(), info.getResponse(), stream);
		info.setRendered();
	}

	private boolean matchesProtectedFile(TestPlan testPlan, FileHandle fileHandle, String testXmlPath, String path)
	{
		if( fileSystemService.isSameFile(fileHandle, testXmlPath, fileHandle, path) )
		{
			return true;
		}

		// need to test ALL questions
		final List<TestPlanNode> questionNodes = testPlan.searchNodes(TestNodeType.ASSESSMENT_ITEM_REF);
		for( TestPlanNode questionNode : questionNodes )
		{
			final URI itemSystemId = questionNode.getItemSystemId();
			final String questionXmlPath = itemSystemId.toString();
			if( fileSystemService.isSameFile(fileHandle, questionXmlPath, fileHandle, path) )
			{
				return true;
			}
		}

		return false;
	}

	@EventHandlerMethod
	public void onValueChange(SectionInfo info)
	{
		qtiWebService.readFormValues(info, rootFileSection.getViewItemResource(info), true);
	}

	@EventHandlerMethod
	public void onViewResult(SectionInfo info)
	{
		final ViewItemResource resource = rootFileSection.getViewItemResource(info);
		qtiWebService.selectQuestion(info, resource, null, 0);
	}

	@EventHandlerMethod
	public void onSubmitTest(SectionInfo info)
	{
		qtiWebService.submitTest(info, rootFileSection.getViewItemResource(info));
	}

	/**
	 * @param info
	 * @param key May be empty in the case of prev/next buttons
	 * @param direction Either -1 (prev), +1 (next) or 0 for specific selection
	 */
	@EventHandlerMethod
	public void onQuestionSelect(SectionInfo info, String key, int direction)
	{
		final ViewItemResource resource = rootFileSection.getViewItemResource(info);
		qtiWebService.selectQuestion(info, resource, Strings.emptyToNull(key), direction);
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public void beforeRender(SectionInfo info, ViewItemResource resource)
	{
		// Maybe later...
	}

	private CustomAttachment getAttachment(ViewItemResource resource)
	{
		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		return (CustomAttachment) viewableResource.getAttachment();
	}

	@Override
	public QTIPlayViewerModel instantiateModel(SectionInfo info)
	{
		return new QTIPlayViewerModel();
	}

	public NavBar getNavBar()
	{
		return navBar;
	}

	public Button getPreviousButton()
	{
		return previousButton;
	}

	public Button getNextButton()
	{
		return nextButton;
	}

	public Button getSubmitButton()
	{
		return submitButton;
	}

	public Button getViewResultButton()
	{
		return viewResultButton;
	}

	public Button getStartButton()
	{
		return startButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public class PlayerContext implements QtiViewerContext
	{
		private final RenderContext info;
		private final ViewItemResource resource;
		@Nullable
		private ItemSessionController itemSessionController;
		private boolean hasResolvedItemSessionController;
		private final List<Pair<String, Identifier>> errors = Lists.newArrayList();

		public PlayerContext(RenderContext info, ViewItemResource resource)
		{
			this.info = info;
			this.resource = resource;
		}

		@Override
		public RenderContext getRenderContext()
		{
			return info;
		}

		@Override
		public TestSessionState getSessionState()
		{
			return qtiWebService.getTestSessionState(info, resource);
		}

		@Override
		public Bookmark getViewResourceUrl(String url)
		{
			return new BookmarkAndModify(info, events.getNamedModifier("viewResource", url));
		}

		@Override
		public UpdateDomFunction getValueChangedFunction()
		{
			return updateLeftAndRightFunction;
		}

		@Override
		public SubmitValuesFunction getEndAttemptFunction()
		{
			return events.getSubmitValuesFunction("onSubmitTest");
		}

		@Nullable
		@Override
		public List<String> getValues(Identifier responseIdentifier)
		{
			final TestSessionState testSessionState = getSessionState();
			final ItemSessionState currentItemSessionState = testSessionState.getCurrentItemSessionState();
			final ResponseData responseValue = currentItemSessionState.getRawResponseData(responseIdentifier);
			if( responseValue != null )
			{
				if( responseValue instanceof StringResponseData )
				{
					final List<String> responseData = ((StringResponseData) responseValue).getResponseData();
					return responseData; // NOSONAR (local var for readability)
				}
				throw new Error("FileResponseData not supported");
			}
			return null;
		}

		@Override
		public TestSessionController getTestSessionController()
		{
			return qtiWebService.getTestSessionController(info, resource);
		}

		@Nullable
		@Override
		public ItemSessionController getItemSessionController()
		{
			if( !hasResolvedItemSessionController )
			{
				final TestSessionController testController = getTestSessionController();
				final TestSessionState testSessionState = testController.getTestSessionState();
				final TestPlan testPlan = testSessionState.getTestPlan();

				final TestPlanNodeKey currentItemKey = testSessionState.getCurrentItemKey();
				if( currentItemKey != null )
				{
					final TestPlanNode itemNode = testPlan.getNode(currentItemKey);
					itemSessionController = (ItemSessionController) testController.getItemProcessingContext(itemNode);
					hasResolvedItemSessionController = true;
				}
			}
			return itemSessionController;
		}

		@Override
		public Value evaluateVariable(@Nullable QtiNode caller, Identifier variableId)
		{
			final ItemSessionController isc = getItemSessionController();
			if( isc != null )
			{
				return isc.evaluateVariableValue(variableId);
			}
			final TestSessionController tsc = getTestSessionController();
			return tsc.evaluateVariableReference(caller, variableId);
		}

		@Override
		public void addError(String message, Identifier interactionId)
		{
			errors.add(new Pair<String, Identifier>(message, interactionId));
		}

		@Override
		public List<Pair<String, Identifier>> getErrors()
		{
			return errors;
		}
	}

	@NonNullByDefault(false)
	public static class QTIPlayViewerModel
	{
		private Label questionTitle;
		private SectionRenderable questionRenderable;
		private boolean hideNavigationButtons;

		private int sectionCount;
		private int questionCount;
		private double totalScore;
		private List<TestSectionModel> sections;

		public SectionRenderable getQuestionRenderable()
		{
			return questionRenderable;
		}

		public void setQuestionRenderable(SectionRenderable questionRenderable)
		{
			this.questionRenderable = questionRenderable;
		}

		public int getSectionCount()
		{
			return sectionCount;
		}

		public void setSectionCount(int sectionCount)
		{
			this.sectionCount = sectionCount;
		}

		public int getQuestionCount()
		{
			return questionCount;
		}

		public void setQuestionCount(int questionCount)
		{
			this.questionCount = questionCount;
		}

		public List<TestSectionModel> getSections()
		{
			return sections;
		}

		public void setSections(List<TestSectionModel> sections)
		{
			this.sections = sections;
		}

		public double getTotalScore()
		{
			return totalScore;
		}

		public void setTotalScore(double totalScore)
		{
			this.totalScore = totalScore;
		}

		public Label getQuestionTitle()
		{
			return questionTitle;
		}

		public void setQuestionTitle(Label questionTitle)
		{
			this.questionTitle = questionTitle;
		}

		public boolean isHideNavigationButtons()
		{
			return hideNavigationButtons;
		}

		public void setHideNavigationButtons(boolean hideNavigationButtons)
		{
			this.hideNavigationButtons = hideNavigationButtons;
		}
	}

	@NonNullByDefault(false)
	public static class TestSectionModel
	{
		private Label title;
		private List<QuestionModel> questions;

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public List<QuestionModel> getQuestions()
		{
			return questions;
		}

		public void setQuestions(List<QuestionModel> questions)
		{
			this.questions = questions;
		}
	}

	@NonNullByDefault(false)
	public static class QuestionModel
	{
		private String id;
		private HtmlLinkState link;
		private String styleClass;
		private Label iconTitle;
		private String iconClass;

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}

		public void setLink(HtmlLinkState link)
		{
			this.link = link;
		}

		public String getStyleClass()
		{
			return styleClass;
		}

		public void setStyleClass(String styleClass)
		{
			this.styleClass = styleClass;
		}

		public Label getIconTitle()
		{
			return iconTitle;
		}

		public void setIconTitle(Label iconTitle)
		{
			this.iconTitle = iconTitle;
		}

		public String getIconClass()
		{
			return iconClass;
		}

		public void setIconClass(String iconClass)
		{
			this.iconClass = iconClass;
		}
	}

	@NonNullByDefault(false)
	public static class EndSummaryModel
	{
		private Label heading;
		private List<SectionRenderable> feedbacks;

		public Label getHeading()
		{
			return heading;
		}

		public void setHeading(Label heading)
		{
			this.heading = heading;
		}

		public List<SectionRenderable> getFeedbacks()
		{
			return feedbacks;
		}

		public void setFeedbacks(List<SectionRenderable> feedbacks)
		{
			this.feedbacks = feedbacks;
		}
	}
}
