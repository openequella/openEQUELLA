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

package com.tle.core.externaltools.serializer;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class ExternalToolAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return ExternalToolConstants.CUSTOM_ATTACHMENT_TYPE;
	}

	public void editExternalToolProviderUuid(String externalToolProviderUuid)
	{
		editCustomData(ExternalToolConstants.EXTERNAL_TOOL_PROVIDER_UUID, externalToolProviderUuid);
	}

	public void editLaunchUrl(String launchUrl)
	{
		editCustomData(ExternalToolConstants.LAUNCH_URL, launchUrl);
	}

	public void editCustomParameters(String customParameters)
	{
		editCustomData(ExternalToolConstants.CUSTOM_PARAMS, customParameters);
	}

	public void editConsumerKey(String consumerKey)
	{
		editCustomData(ExternalToolConstants.CONSUMER_KEY, consumerKey);
	}

	public void editConsumerSecret(String consumerSecret)
	{
		editCustomData(ExternalToolConstants.SHARED_SECRET, consumerSecret);
	}

	public void editIconUrl(String iconUrl)
	{
		editCustomData(ExternalToolConstants.ICON_URL, iconUrl);
	}

	public void editShareUserNameDetails(boolean shareUserNameDetails)
	{
		editCustomData(ExternalToolConstants.SHARE_NAME, shareUserNameDetails);
	}

	public void editShareUserEmailDetails(boolean shareUserEmailDetails)
	{
		editCustomData(ExternalToolConstants.SHARE_EMAIL, shareUserEmailDetails);
	}
}
