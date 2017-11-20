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

package com.tle.web.wizard;

import com.tle.beans.item.attachments.Attachment;

import java.util.ArrayList;
import java.util.Collection;

public class PackageInfo
{
    boolean isValid;
    protected String error;

    protected String title;
    protected String scormVersion;

    protected Collection<Attachment> createdAttachments;

    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(boolean isValid)
    {
        this.isValid = isValid;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getScormVersion()
    {
        return scormVersion;
    }

    public void setScormVersion(String scormVersion)
    {
        this.scormVersion = scormVersion;
    }

    public Collection<Attachment> getCreatedAttachments()
    {
        if( createdAttachments == null )
        {
            createdAttachments = new ArrayList<Attachment>();
        }
        return createdAttachments;
    }

    public void setCreatedAttachments(Collection<Attachment> createdAttachments)
    {
        this.createdAttachments = createdAttachments;
    }
}
