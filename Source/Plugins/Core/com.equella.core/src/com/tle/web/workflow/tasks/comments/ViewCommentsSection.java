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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.workflow.servlet.WorkflowMessageServlet;

@Bind
public class ViewCommentsSection extends AbstractPrototypeSection<ViewCommentsSection.Model> implements HtmlRenderer
{
	@PlugKey("comments.existing")
	private static String KEY_COMMENTS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService instituionService;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		userLinkSection = userLinkService.register(tree, id);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws IOException
	{
		Model model = getModel(context);
		Collection<WorkflowMessage> messages = model.getMessages();
		int numComments = messages.size();
		model.setCommentHeading(new PluralKeyLabel(KEY_COMMENTS, numComments));
		model.setComments(ModCommentRender.render(context, viewFactory, userLinkSection, fileSystemService, messages));

		return viewFactory.createResult("viewcomments.ftl", this);
	}

	public void setMessages(SectionInfo info, Collection<WorkflowMessage> messages)
	{
		getModel(info).setMessages(messages);
	}

	public static class Model
	{
		private Label commentHeading;
		private Collection<WorkflowMessage> messages;
		private SectionRenderable comments;

		public Label getCommentHeading()
		{
			return commentHeading;
		}

		public void setCommentHeading(Label commentHeading)
		{
			this.commentHeading = commentHeading;
		}

		public Collection<WorkflowMessage> getMessages()
		{
			return messages;
		}

		public void setMessages(Collection<WorkflowMessage> messages)
		{
			this.messages = messages;
		}

		public SectionRenderable getComments()
		{
			return comments;
		}

		public void setComments(SectionRenderable comments)
		{
			this.comments = comments;
		}
	}
}