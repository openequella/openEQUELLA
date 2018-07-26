package com.tle.web.template;

import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.EventAuthoriser;

import java.util.Map;

public class XSRFAuthoriser implements EventAuthoriser {

    @Override
    public void checkAuthorisation(SectionInfo info)
    {
        if( !CurrentUser.getSessionID().equals(info.getRequest().getParameter(RenderTemplate.XSRF_PARAM)) )
        {
            throw new AccessDeniedException(
                "XSRF Prevention: session parameter must be included for this request"); //$NON-NLS-1$
        }
    }

    @Override
    public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
    {
        bookmarkState.put(RenderTemplate.XSRF_PARAM, new String[]{CurrentUser.getSessionID()});
    }
}
