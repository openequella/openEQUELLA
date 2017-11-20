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

package com.tle.core.externaltools.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tle.beans.item.attachments.Attachment;
import com.tle.common.NameValue;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.remoting.RemoteExternalToolsService;
import com.tle.core.service.session.ExternalToolEditingBean;

public interface ExternalToolsService
	extends
		AbstractEntityService<ExternalToolEditingBean, ExternalTool>,
		RemoteExternalToolsService
{
	@SuppressWarnings("nls")
	public static final String ENTITY_TYPE = "EXTERNAL_TOOL";

	/**
	 * Method for creating oauth signature parameters. Creates an oauth message
	 * and signs it which adds the signature to the message's parameters. Only
	 * the parameters are returned. Note: this method could be moved to not such
	 * a specialised service
	 * 
	 * @param consumerKey consumer key
	 * @param secret shared secret
	 * @param url launch URL
	 * @return all parameters needed to sign a POST message
	 */
	public List<Entry<String, String>> getOauthSignatureParams(String consumerKey, String secret, String url,
		Map<String, String[]> postParams);

	/**
	 * @param launchURL tool launch URL entered during contribution
	 * @return first matched enabled provider configuration or null if none
	 *         found
	 */
	public ExternalTool findMatchingBaseURL(String launchURL);

	List<NameValue> parseCustomParamsString(String paramString);

	String customParamListToString(List<NameValue> customParams);

	/**
	 * If an ICON_URL isn't found in the attachment, look for an ExternalTool
	 * uuid and if found use it's URL if it exists. Fall back on the mime
	 * entry's icon it that exists and after all that, return null.
	 * 
	 * @param attachment
	 * @return icon url, or null if not found at all
	 */
	String findApplicableIconUrl(Attachment attachment);
}
