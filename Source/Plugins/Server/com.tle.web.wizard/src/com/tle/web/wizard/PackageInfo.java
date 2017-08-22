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
