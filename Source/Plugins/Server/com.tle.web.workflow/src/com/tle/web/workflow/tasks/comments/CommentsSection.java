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

package com.tle.web.workflow.tasks.comments;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.Check;
import com.tle.core.services.entity.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.template.Decorations;
import com.tle.web.workflow.tasks.CurrentTaskSection;

@NonNullByDefault
@SuppressWarnings("nls")
public class CommentsSection extends AbstractPrototypeSection<CommentsSection.Model>
	implements
		HtmlRenderer,
		ReadyToRespondListener
{
	public enum CommentType
	{
		REJECT, COMMENT, SHOW, ACCEPT
	}

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@PlugKey("reject.original")
	private static Label LABEL_ORIGINAL;

	@PlugKey("comments.title.comments")
	private static Label LABEL_COMMENTS_TITLE;
	@PlugKey("comments.title.approving")
	private static Label LABEL_APPROVING_TITLE;
	@PlugKey("comments.title.rejecting")
	private static Label LABEL_REJECTING_TITLE;

	@PlugKey("comments.rejectmsg")
	private static Label LABEL_REJECTMSG;
	@PlugKey("comments.commentmsg")
	private static Label LABEL_COMMENTMSG;
	@PlugKey("comments.acceptmsg")
	private static Label LABEL_ACCEPTMSG;
	@PlugKey("comments.entermsg")
	private static Label LABEL_ENTERMSG;

	@Component
	private TextField commentField;
	@Component
	@PlugKey("comments.submit")
	private Button submitButton;
	@Component
	@PlugKey("comments.cancel")
	private Button cancelButton;
	@Component
	@PlugKey("comments.close")
	private Button closeButton;
	@Component
	private SingleSelectionList<WorkflowStep> rejectSteps;

	@Inject
	private BundleCache bundleCache;

	@TreeLookup
	private CurrentTaskSection currentTaskSection;

	@Inject
	private ViewCommentsSection viewCommentsSection;
	@Inject
	private WorkflowService workflowService;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(viewCommentsSection, id);
		submitButton.setClickHandler(events.getNamedHandler("submit"));
		SubmitValuesHandler cancelHandler = events.getNamedHandler("cancel");
		cancelButton.setClickHandler(cancelHandler);
		closeButton.setClickHandler(cancelHandler);
		rejectSteps.setListModel(new StepListModel());
		rejectSteps.setDisplayed(false);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Label title = null;

		Model model = getModel(context);
		model.setMandatory(true);
		model.setPostAllowed(true);
		viewCommentsSection.setMessages(context,
			workflowService.getMessages(currentTaskSection.getCurrentState(context).getItemTaskId()));
		switch( model.getCommentType() )
		{
			case REJECT:
				title = LABEL_REJECTING_TITLE;
				rejectSteps.setDisplayed(context, !rejectSteps.getListModel().getOptions(context).isEmpty());
				model.setPostCommentHeading(LABEL_REJECTMSG);
				break;
			case COMMENT:
				title = LABEL_COMMENTS_TITLE;
				model.setPostCommentHeading(LABEL_COMMENTMSG);
				break;
			case ACCEPT:
				title = LABEL_APPROVING_TITLE;
				model.setPostCommentHeading(LABEL_ACCEPTMSG);
				model.setMandatory(false);
				break;
			case SHOW:
				title = LABEL_COMMENTS_TITLE;
				model.setPostAllowed(false);
				break;
		}
		Decorations.getDecorations(context).setTitle(title);
		model.setPageTitle(title);

		return viewFactory.createResult("comments.ftl", this);
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		SectionUtils.clearModel(info, this);
	}

	@EventHandlerMethod
	public void submit(SectionInfo info)
	{
		Model model = getModel(info);
		String commentMsg = commentField.getValue(info);
		CommentType commentType = model.getCommentType();
		if( (commentType == CommentType.REJECT || commentType == CommentType.COMMENT) && Check.isEmpty(commentMsg) )
		{
			model.setErrorMessage(LABEL_ENTERMSG);
			info.preventGET();
			return;
		}
		currentTaskSection.doComment(info, commentType, rejectSteps.getSelectedValueAsString(info), commentMsg);
		commentField.setValue(info, null);
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		if( !redirect && getModel(info).getCommentType() != null )
		{
			currentTaskSection.setModal(info, this);
		}
	}

	public Button getSubmitButton()
	{
		return submitButton;
	}

	public TextField getCommentField()
	{
		return commentField;
	}

	public class StepListModel extends DynamicHtmlListModel<WorkflowStep>
	{
		@Override
		protected Iterable<WorkflowStep> populateModel(SectionInfo info)
		{
			WorkflowStep step = currentTaskSection.getCurrentStep(info);
			List<WorkflowStep> steps = new ArrayList<WorkflowStep>(step.getRejectPoints());
			if( !steps.isEmpty() )
			{
				steps.add(null);
			}
			return steps;
		}

		@Override
		protected Option<WorkflowStep> convertToOption(SectionInfo info, @Nullable WorkflowStep obj)
		{
			if( obj == null )
			{
				return new LabelOption<WorkflowStep>(LABEL_ORIGINAL, "", null);
			}
			return new NameValueOption<WorkflowStep>(new BundleNameValue(obj.getDisplayName(), obj.getUuid(),
				bundleCache), obj);
		}
	}

	public SingleSelectionList<WorkflowStep> getRejectSteps()
	{
		return rejectSteps;
	}

	public void doComment(SectionInfo info, CommentType type)
	{
		getModel(info).setCommentType(type);
	}

	public boolean isCommenting(SectionInfo info)
	{
		return getModel(info).getCommentType() != null;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@NonNullByDefault(false)
	public static class Model
	{
		@Bookmarked
		private CommentType commentType;
		private boolean postAllowed;
		private boolean mandatory;
		private Label errorMessage;
		private Label postCommentHeading;
		private Label commentHeading;
		private Label pageTitle;

		public CommentType getCommentType()
		{
			return commentType;
		}

		public void setCommentType(CommentType commentType)
		{
			this.commentType = commentType;
		}

		public boolean isPostAllowed()
		{
			return postAllowed;
		}

		public void setPostAllowed(boolean postAllowed)
		{
			this.postAllowed = postAllowed;
		}

		public Label getPostCommentHeading()
		{
			return postCommentHeading;
		}

		public void setPostCommentHeading(Label postCommentHeading)
		{
			this.postCommentHeading = postCommentHeading;
		}

		public Label getCommentHeading()
		{
			return commentHeading;
		}

		public void setCommentHeading(Label commentHeading)
		{
			this.commentHeading = commentHeading;
		}

		public Label getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(Label errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		public boolean isMandatory()
		{
			return mandatory;
		}

		public void setMandatory(boolean mandatory)
		{
			this.mandatory = mandatory;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}
	}

	public Button getCloseButton()
	{
		return closeButton;
	}

	public ViewCommentsSection getViewCommentsSection()
	{
		return viewCommentsSection;
	}
}
