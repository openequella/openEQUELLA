package com.tle.web.api.item.equella.interfaces.beans;

public class DisplayOptions
{
    private final String attachmentType;
    private final boolean disableThumbnail;
    private final boolean standardOpen;
    private final boolean integrationOpen;

    public DisplayOptions(String attachmentType, boolean disableThumbnail, boolean standardOpen, boolean integrationOpen)
    {
        this.attachmentType = attachmentType;
        this.disableThumbnail = disableThumbnail;
        this.standardOpen = standardOpen;
        this.integrationOpen = integrationOpen;
    }

    public String getAttachmentType()
    {
        return attachmentType;
    }

    public boolean isDisableThumbnail()
    {
        return disableThumbnail;
    }

    public boolean isStandardOpen()
    {
        return standardOpen;
    }

    public boolean isIntegrationOpen()
    {
        return integrationOpen;
    }

}
