/*
 * Copyright 2019 Apereo
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
