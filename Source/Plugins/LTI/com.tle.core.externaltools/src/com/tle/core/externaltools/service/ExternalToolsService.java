package com.tle.core.externaltools.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tle.beans.item.attachments.Attachment;
import com.tle.common.NameValue;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.remoting.RemoteExternalToolsService;
import com.tle.core.service.session.ExternalToolEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

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

