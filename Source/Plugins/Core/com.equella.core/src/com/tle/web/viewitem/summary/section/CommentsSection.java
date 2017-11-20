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

package com.tle.web.viewitem.summary.section;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Comment;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.services.user.UserService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ItemSectionInfo;

@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class CommentsSection extends AbstractParentViewItemSection<CommentsSection.CommentsModel>
	implements
		DisplaySectionConfiguration
{
	private static enum NameDisplayType
	{
		BOTH, FIRST, LAST;
	}

	private static final IncludeFile INCLUDE = new IncludeFile(
		ResourcesService.getResourceHelper(CommentsSection.class).url("scripts/viewitem/comments.js"));
	private static final JSCallable COMMENT_EFFECT = new ExternallyDefinedFunction("commentEffect", INCLUDE);
	private static final JSCallable SETUP_CHANGE_EVENTS = new ExternallyDefinedFunction("setupChangeEvents", INCLUDE);

	@PlugKey("comments.delete")
	private static Label DELETE_LABEL;
	@PlugKey("comments.deleteemptyconfirm")
	private static Label DELETE_CONFIRM_EMPTY_LABEL;
	@PlugKey("comments.deleteconfirm")
	private static String DELETE_CONFIRM_KEY;
	@PlugKey("comments.anonymous")
	private static Label LABEL_ANONYMOUS;

	@Inject
	private UserService userService;
	@Inject
	private ItemCommentService itemCommentService;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@Inject
	private BundleCache bundleCache;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(stateful = false)
	private SingleSelectionList<Integer> addRating;
	@Component(name = "c")
	private TextField textArea;
	@Component(name = "a")
	@PlugKey("comments.anonymouscheck")
	private Checkbox anonymous;
	@Component(name = "add")
	@PlugKey("summary.comments.submit")
	private Button addComment;

	private JSCallable deleteFunc;
	private boolean allowAnonymous;
	private boolean displayUsername;
	private NameDisplayType whichName;
	private boolean displayIdentity;
	private SummarySectionsConfig sectionConfig;

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( canView(context) )
		{
			final CommentsModel model = getModel(context);

			if( model.isCanView() )
			{
				final List<Comment> comments = itemCommentService.getComments(getItemInfo(context).getItem(), null,
					null, -1);
				if( comments != null )
				{
					final boolean canDelete = model.isCanDelete();
					final List<CommentRow> rows = new ArrayList<CommentRow>(comments.size());
					for( Comment comment : comments )
					{
						rows.add(getCommentRow(comment, canDelete));
					}
					model.setComments(rows);
				}
			}

			// Of autologin (whoever the auto-logged in user is) we deem
			// synonymous to anonymity
			UserState userState = CurrentUser.getUserState();
			if( userState.wasAutoLoggedIn() )
			{
				anonymous.setChecked(context, true);
				anonymous.setDisabled(context, true);
			}

			if( sectionConfig != null )
			{
				Label title = new BundleLabel(sectionConfig.getBundleTitle(), bundleCache);
				model.setSectionTitle(title);
			}

			if( model.isAjax() )
			{
				return viewFactory.createResult("viewitem/comments.ftl", this);
			}
			return viewFactory.createNamedResult("section_comments", "viewitem/comments.ftl", this);
		}
		return null;

	}

	@EventHandlerMethod
	public void addClick(SectionInfo info)
	{
		ItemSectionInfo iinfo = getItemInfo(info);

		String commentText = textArea.getValue(info).trim();
		Integer rating = addRating.getSelectedValue(info);
		addRating.setSelectedValue(info, 0);

		// you need to specify either comment text or a rating (or both)
		if( !Check.isEmpty(commentText) || rating != null )
		{
			itemCommentService.addComment(iinfo.getItemId(), commentText, rating == null ? 0 : rating.intValue(),
				anonymous.isChecked(info));
			textArea.setValue(info, Constants.BLANK);
		}
		getModel(info).setAjax(true);
	}

	@EventHandlerMethod
	public void deleteComment(SectionInfo info, String commentUuid)
	{
		itemCommentService.deleteComment(getItemInfo(info).getItemId(), commentUuid);
		getModel(info).setAjax(true);
	}

	@Override
	public Class<CommentsModel> getModelClass()
	{
		return CommentsModel.class;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		final Set<String> privileges = getItemInfo(info).getPrivileges();
		final CommentsModel model = getModel(info);

		model.setCanAdd(privileges.contains("COMMENT_CREATE_ITEM") && !isForPreview(info));
		model.setCanView(privileges.contains("COMMENT_VIEW_ITEM"));
		model.setCanDelete(privileges.contains("COMMENT_DELETE_ITEM") && !isForPreview(info));

		return model.canAdd || model.canView;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		addRating.setListModel(new SimpleHtmlListModel<Integer>(0, 1, 2, 3, 4, 5));

		deleteFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteComment"), COMMENT_EFFECT,
			"comments-list");

		addComment.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("addClick"),
			COMMENT_EFFECT, "comments-add", "comments-list"));

		textArea.addReadyStatements(Js.call_s(SETUP_CHANGE_EVENTS, Jq.$(textArea), Jq.$(addRating), Jq.$(addComment)));
	}

	private CommentRow getCommentRow(Comment comment, boolean canDelete)
	{
		final CommentRow row = new CommentRow(comment, dateRendererFactory);
		final String owner = comment.getOwner();
		if( !comment.isAnonymous() && !Check.isEmpty(owner) && displayIdentity )
		{
			row.setDisplayUsername(displayUsername);
			row.setNameDisplay(whichName);
			row.setUser(userService.getInformationForUser(owner));
		}

		if( canDelete )
		{
			final String c = comment.getComment();
			final Label confirmText = Check.isEmpty(c) ? DELETE_CONFIRM_EMPTY_LABEL
				: new KeyLabel(DELETE_CONFIRM_KEY, c);

			IconLabel label = new IconLabel(Icon.DELETE, null);
			final HtmlLinkState hls = new HtmlLinkState(
				new OverrideHandler(deleteFunc, comment.getUuid()).addValidator(new Confirm(confirmText)));
			label.setWhiteIcon(false);
			hls.setTitle(DELETE_LABEL);
			hls.setLabel(label);

			row.setDeleteButton(hls);
		}

		return row;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "comments";
	}

	public static class CommentsModel
	{
		private boolean canView;
		private boolean canAdd;
		private boolean canDelete;
		private List<CommentRow> comments;
		private boolean ajax;
		private Label sectionTitle;

		public boolean isCanDelete()
		{
			return canDelete;
		}

		public boolean isCanView()
		{
			return canView;
		}

		public void setCanView(boolean canView)
		{
			this.canView = canView;
		}

		public void setCanDelete(boolean canDelete)
		{
			this.canDelete = canDelete;
		}

		public List<CommentRow> getComments()
		{
			return comments;
		}

		public void setComments(List<CommentRow> comments)
		{
			this.comments = comments;
		}

		public boolean isHasComments()
		{
			return !Check.isEmpty(comments);
		}

		public boolean isCanAdd()
		{
			return canAdd;
		}

		public void setCanAdd(boolean canAdd)
		{
			this.canAdd = canAdd;
		}

		public boolean isAjax()
		{
			return ajax;
		}

		public void setAjax(boolean ajax)
		{
			this.ajax = ajax;
		}

		public Label getSectionTitle()
		{
			return sectionTitle;
		}

		public void setSectionTitle(Label sectionTitle)
		{
			this.sectionTitle = sectionTitle;
		}
	}

	@NonNullByDefault(false)
	public static class CommentRow
	{
		private final Comment comment;
		private UserBean user;
		private HtmlComponentState deleteButton;
		private NameDisplayType nameDisplay;
		private boolean displayUsername;
		private DateRendererFactory dateRendererFactory;

		public void setDisplayUsername(boolean displayUsername)
		{
			this.displayUsername = displayUsername;
		}

		public void setNameDisplay(NameDisplayType nameDisplay)
		{
			this.nameDisplay = nameDisplay;
		}

		public CommentRow(Comment comment, DateRendererFactory dateRendererFactory)
		{
			this.comment = comment;
			this.dateRendererFactory = dateRendererFactory;
		}

		public HtmlComponentState getDeleteButton()
		{
			return deleteButton;
		}

		public void setDeleteButton(HtmlComponentState deleteButton)
		{
			this.deleteButton = deleteButton;
		}

		public String getComment()
		{
			return comment.getComment();
		}

		public String getOwner()
		{
			return comment.getOwner();
		}

		public boolean hasRating()
		{
			return comment.getRating() > 0;
		}

		public int getRating()
		{
			return comment.getRating();
		}

		public Date getDateCreated()
		{
			return comment.getDateCreated();
		}

		public SectionRenderable getDateCreatedRenderable()
		{
			return dateRendererFactory.createDateRenderer(comment.getDateCreated());
		}

		public long getId()
		{
			return comment.getId();
		}

		public String getUsername()
		{
			if( user == null )
			{
				return LABEL_ANONYMOUS.getText();
			}
			else
			{
				String displayString;
				switch( nameDisplay )
				{
					case BOTH:
						displayString = user.getFirstName() + " " + user.getLastName();
						break;
					case FIRST:
						displayString = user.getFirstName();
						break;
					case LAST:
						displayString = user.getLastName();
						break;
					default:
						displayString = user.getFirstName() + " " + user.getLastName();
						break;
				}
				if( displayUsername )
				{
					displayString += " [" + user.getUsername() + "]";
				}
				return displayString;
			}
		}

		public void setUser(UserBean user)
		{
			this.user = user;
		}
	}

	public SingleSelectionList<Integer> getAddRating()
	{
		return addRating;
	}

	public Button getAddComment()
	{
		return addComment;
	}

	public TextField getTextArea()
	{
		return textArea;
	}

	public Checkbox getAnonymous()
	{
		return anonymous;
	}

	public boolean isAllowAnonymous()
	{
		return allowAnonymous;
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		String configuration = config.getConfiguration();
		if( !Check.isEmpty(configuration) )
		{
			PropBagEx xml = new PropBagEx(configuration);

			displayIdentity = Boolean.parseBoolean(xml.getNode("DISPLAY_IDENTITY_KEY"));
			if( displayIdentity )
			{
				allowAnonymous = Boolean.parseBoolean(xml.getNode("ANONYMOUSLY_COMMENTS_KEY"));
				displayUsername = !(Boolean.parseBoolean(xml.getNode("SUPPRESS_USERNAME_KEY")));
				whichName = NameDisplayType.valueOf(xml.getNode("DISPLAY_NAME_KEY"));
			}
		}
		else
		{
			displayIdentity = true;
			whichName = NameDisplayType.BOTH;
			displayUsername = true;
			allowAnonymous = true;
		}

		sectionConfig = config;
	}
}
