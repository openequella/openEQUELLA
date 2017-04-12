package com.tle.web.workflow.tasks.comments;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
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
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;

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
	public SectionResult renderHtml(RenderEventContext context)
	{
		Model model = getModel(context);
		List<CommentRow> commentList = Lists.newArrayList();
		Collection<WorkflowMessage> comments = model.getMessages();
		int numComments = comments.size();
		model.setCommentHeading(new PluralKeyLabel(KEY_COMMENTS, numComments));
		for( WorkflowMessage comEvent : comments )
		{
			Date date = comEvent.getDate();
			String extraClass = "";
			String user = comEvent.getUser();
			char type = comEvent.getType();
			if( type == WorkflowMessage.TYPE_REJECT )
			{
				extraClass = "rejection";
			}
			else if( type == WorkflowMessage.TYPE_ACCEPT )
			{
				extraClass = "approval";
			}
			BundleLabel taskName = new BundleLabel(comEvent.getNode().getNode().getName(), bundleCache);
			HtmlLinkState userLink = userLinkSection.createLink(context, user);
			CommentRow modrow = new CommentRow(comEvent.getMessage(), userLink, taskName, date,
				JQueryTimeAgo.timeAgoTag(date), extraClass);
			commentList.add(modrow);
		}
		Collections.sort(commentList, new Comparator<CommentRow>()
		{
			@Override
			public int compare(CommentRow r1, CommentRow r2)
			{
				long t1 = r1.getDate().getTime();
				long t2 = r2.getDate().getTime();
				return t1 < t2 ? -1 : (t1 == t2 ? 0 : 1);
			}
		});
		model.setComments(commentList);
		if( commentList.isEmpty() )
		{
			return null;
		}
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
		private List<CommentRow> comments;

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

		public List<CommentRow> getComments()
		{
			return comments;
		}

		public void setComments(List<CommentRow> comments)
		{
			this.comments = comments;
		}
	}
}
