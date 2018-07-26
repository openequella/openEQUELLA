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
