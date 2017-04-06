package com.tle.core.item.serializer;

import javax.inject.Singleton;

import com.tle.beans.item.Comment;
import com.tle.core.guice.Bind;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.beans.CommentBean;

@Bind
@Singleton
public class ItemCommentSerializer
{
	public CommentBean serialize(Comment comment)
	{
		CommentBean commentBean = new CommentBean();
		commentBean.setUuid(comment.getUuid());
		commentBean.setComment(comment.getComment());
		commentBean.setPostedDate(comment.getDateCreated());
		if( !comment.isAnonymous() )
		{
			UserBean user = new UserBean();
			user.setId(comment.getOwner());
			commentBean.setPostedBy(user);
		}
		else
		{
			commentBean.setAnonymous(true);
		}
		commentBean.setRating(comment.getRating());
		return commentBean;
	}
}
