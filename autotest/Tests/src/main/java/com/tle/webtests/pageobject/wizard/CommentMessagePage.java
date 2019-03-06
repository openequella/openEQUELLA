package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;

public class CommentMessagePage extends ModerationMessagePage<CommentMessagePage>
{
    public CommentMessagePage(PageContext context)
    {
        super(context);
    }

    @Override
    public String getPfx()
    {
        return "_taskscommentDialog";
    }
}
