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

package com.tle.core.externaltools.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.NameValue;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.LangUtils;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.externaltools.dao.ExternalToolsDao;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.service.session.ExternalToolEditingBean;
import com.tle.core.service.session.ExternalToolEditingSession;
import com.tle.web.mimetypes.service.WebMimeTypeService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

import net.oauth.OAuth;
import net.oauth.OAuth.Parameter;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;

@Bind(ExternalToolsService.class)
@Singleton
@SecureEntity(ExternalToolsService.ENTITY_TYPE)
@SuppressWarnings("nls")
public class ExternalToolsServiceImpl
	extends
		AbstractEntityServiceImpl<ExternalToolEditingBean, ExternalTool, ExternalToolsService>
	implements
		ExternalToolsService
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(ExternalToolsServiceImpl.class);

	private static final String KEY_VALIDATION_NOBASEURL = "validation.baseurl.empty";
	private static final String KEY_VALIDATION_DUPLICATEBASE = "validation.baseurl.duplicate";
	private static final String KEY_VALIDATION_SUBSTRINGBASE = "validation.baseurl.substring";
	private static final String KEY_VALIDATION_SUPERSTRINGBASE = "validation.baseurl.superstring";

	@Inject
	private ExternalToolsDao toolsDao;
	@Inject
	private WebMimeTypeService webMimeService;

	@Inject
	public ExternalToolsServiceImpl(ExternalToolsDao entityDao)
	{
		super(Node.EXTERNAL_TOOL, entityDao);
		this.toolsDao = entityDao;
	}

	@Override
	protected void doValidation(EntityEditingSession<ExternalToolEditingBean, ExternalTool> session,
		ExternalTool entity, List<ValidationError> errors)
	{
		// do nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<ExternalToolEditingBean, ExternalTool>> SESSION createSession(
		String sessionId, EntityPack<ExternalTool> pack, ExternalToolEditingBean bean)
	{
		return (SESSION) new ExternalToolEditingSession(sessionId, pack, bean);
	}

	@Override
	protected ExternalToolEditingBean createEditingBean()
	{
		return new ExternalToolEditingBean();
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected void populateEditingBean(ExternalToolEditingBean bean, ExternalTool entity)
	{
		super.populateEditingBean(bean, entity);
		if( bean.getId() != 0 )
		{
			bean.setBaseURL(entity.getBaseURL());
			bean.setConsumerKey(entity.getConsumerKey());
			bean.setSharedSecret(entity.getSharedSecret());
			bean.setCustomParams(entity.getCustomParams());
			bean.setShareName(entity.getShareName().booleanValue());
			bean.setShareEmail(entity.getShareEmail().booleanValue());
		}
	}

	@Override
	protected void doAfterImport(TemporaryFileHandle importFolder, ExternalToolEditingBean bean, ExternalTool entity,
		ConverterParams params)
	{
		super.doAfterImport(importFolder, bean, entity, params);
		if( bean != null )
		{
			final ExternalTool dbExternalTool = bean.getId() != 0 ? toolsDao.findById(bean.getId()) : entity;
			copyFromBean(dbExternalTool, bean);
			toolsDao.saveOrUpdate(dbExternalTool);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(ExternalTool entity, boolean checkReferences)
	{
		toolsDao.delete(entity);
		EntityFile file = new EntityFile(entity);
		fileSystemService.removeFile(file, null);
		auditLogService.logEntityDeleted(entity.getId());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public EntityPack<ExternalTool> startEdit(ExternalTool entity)
	{
		return startEditInternal(entity);
	}

	@Override
	protected void populateEntity(ExternalToolEditingBean bean, ExternalTool tool)
	{
		super.populateEntity(bean, tool);
		copyFromBean(tool, bean);
	}

	@Override
	protected void doValidationBean(ExternalToolEditingBean bean, List<ValidationError> errors)
	{
		if( Check.isEmpty(bean.getBaseURL()) )
		{
			errors.add(new ValidationError("errors.baseurl", resources.getString(KEY_VALIDATION_NOBASEURL)));
		}
		else
		{
			List<ExternalTool> allTools = toolsDao.enumerateAll();
			for( ExternalTool exTool : allTools )
			{
				String exToolBaseStr = exTool.getBaseURL().trim();
				String beanStrBaseStr = bean.getBaseURL().trim();
				if( exTool.getId() != bean.getId() )
				{
					if( exToolBaseStr.equalsIgnoreCase(beanStrBaseStr) )
					{
						errors.add(new ValidationError("errors.baseurl",
							resources.getString(KEY_VALIDATION_DUPLICATEBASE, LangUtils.getString(exTool.getName()))));
					}
					else if( exToolBaseStr.startsWith(beanStrBaseStr) )
					{
						errors.add(new ValidationError("errors.baseurl", resources
							.getString(KEY_VALIDATION_SUPERSTRINGBASE, LangUtils.getString(exTool.getName()))));
					}
					else if( beanStrBaseStr.startsWith(exToolBaseStr) )
					{
						errors.add(new ValidationError("errors.baseurl",
							resources.getString(KEY_VALIDATION_SUBSTRINGBASE, LangUtils.getString(exTool.getName()))));
					}
				}
			}
		}
	}

	private void copyFromBean(ExternalTool entity, ExternalToolEditingBean bean)
	{
		entity.setBaseURL(bean.getBaseURL());
		entity.setConsumerKey(bean.getConsumerKey());
		entity.setSharedSecret(bean.getSharedSecret());
		entity.setCustomParams(bean.getCustomParams());
		entity.setShareName(bean.isShareName());
		entity.setShareEmail(bean.isShareEmail());
	}

	@Override
	public List<Entry<String, String>> getOauthSignatureParams(String consumerKey, String secret, String urlStr,
		Map<String, String[]> formParams)
	{
		String nonce = UUID.randomUUID().toString();
		String timestamp = Long.toString(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		// OAuth likes the Map.Entry interface, so copy into a Collection of a
		// local implementation thereof. Note that this is a flat list.
		List<Parameter> postParams = null;

		if( !Check.isEmpty(formParams) )
		{
			postParams = new ArrayList<Parameter>(formParams.size());
			for( Entry<String, String[]> entry : formParams.entrySet() )
			{
				String key = entry.getKey();
				String[] formParamEntry = entry.getValue();
				// cater for multiple values for the same key
				if( formParamEntry.length > 0 )
				{
					for( int i = 0; i < formParamEntry.length; ++i )
					{
						Parameter erp = new Parameter(entry.getKey(), formParamEntry[i]);
						postParams.add(erp);
					}
				}
				else
				{
					// key with no value
					postParams.add(new Parameter(key, null));
				}
			}
		}

		OAuthMessage message = new OAuthMessage(OAuthMessage.POST, urlStr, postParams);
		// Parameters needed for a signature
		message.addParameter(OAuth.OAUTH_CONSUMER_KEY, consumerKey);
		message.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
		message.addParameter(OAuth.OAUTH_NONCE, nonce);
		message.addParameter(OAuth.OAUTH_TIMESTAMP, timestamp);
		message.addParameter(OAuth.OAUTH_VERSION, OAuth.VERSION_1_0);
		message.addParameter(OAuth.OAUTH_CALLBACK, "about:blank");

		// Sign the request
		OAuthConsumer consumer = new OAuthConsumer("about:blank", consumerKey, secret, null);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		try
		{
			message.sign(accessor);
			// send oauth parameters back including signature
			return message.getParameters();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

	}

	/**
	 * Ignoring the protocol, check known providers for matching host. If only
	 * one host matches, return that provider. If multiple providers have a
	 * matching host with the launch url, attempt a match on path. (the Moodle
	 * implementation for example only attempts a server-comparison and returns
	 * first match. Moodle does not examine URL paths where servers match).
	 */
	@Override
	public ExternalTool findMatchingBaseURL(String launchURL)
	{
		try
		{
			URL urlLaunchURL = new URL(launchURL);
			String launchHost = urlLaunchURL.getHost();
			List<ExternalTool> providerHosts = new ArrayList<ExternalTool>();

			List<ExternalTool> providers = enumerateEnabled();
			for( ExternalTool p : providers )
			{
				URL urlBaseURL = new URL(p.getBaseURL());
				if( launchHost.equalsIgnoreCase(urlBaseURL.getHost()) )
				{
					providerHosts.add(p);
				}
			}

			if( !Check.isEmpty(providerHosts) )
			{
				if( providerHosts.size() > 1 )
				{
					String launchFilePath = urlLaunchURL.getFile();
					for( ExternalTool hostedP : providerHosts )
					{
						String baseFilePath = new URL(hostedP.getBaseURL()).getFile();
						if( Objects.equal(Strings.nullToEmpty(launchFilePath), Strings.nullToEmpty(baseFilePath)) )
						{
							return hostedP;
						}
					}
				}
				// if not only return the first
				return providerHosts.get(0);
			}
		}
		catch( MalformedURLException mue )
		{
			// somebody else's problem here: return null
		}
		return null;
	}

	/**
	 * To quote
	 * http://www.imsglobal.org/lti/v2p0pd/ltiIMGv2p0pd.html#_Toc340229529<br>
	 * &lt;quote&gt;<br>
	 * Note: In earlier versions of LTI, custom parameter names had to be
	 * converted to lower case, and all punctuation or special characters in the
	 * name had to be replaced with underscores (before adding the “custom_”
	 * prefix). This naming rule has been removed in LTI 2.0. To preserve
	 * backward compatibility with launches from LTI 1 Tool Providers, it is
	 * recommended that when Tool Consumers are sending a
	 * basic-lti-launch-request message, they check to see if the LTI 1.x rule
	 * would result in a different POST parameter name. In this case, the Tool
	 * Consumer should, as a best practice, render the parameter twice – once
	 * using the LTI 1 convention and a second time using the LTI 2.0
	 * convention.; for example, both as “custom_chapter ” and as
	 * “custom_Chapter ”.<br>
	 * &lt;/quote&gt;<br>
	 * Accordingly, we will follow that 'best practice' here.<br>
	 * Noting that the LTI 1 rule applies to parameter names (& not values), and
	 * also that the "custom_" prefix is prepended by the
	 * ExternalToolViewerSection when composing the POST request
	 */
	@Override
	public List<NameValue> parseCustomParamsString(String paramString)
	{
		List<NameValue> pList = new ArrayList<NameValue>();
		if( !Check.isEmpty(paramString) )
		{
			// convert commas to newline
			paramString = paramString.replace(',', '\n');
			// now split on newline
			String[] params = paramString.split("\n");
			for( String param : params )
			{
				// ignore if no '=', they mangled it
				if( param.indexOf("=") != -1 )
				{
					// just ignore if more than one '='
					String[] keyValue = param.split("=");
					if( keyValue.length == 2 )
					{
						keyValue[0] = keyValue[0].trim();
						String unmodifiedParamName = keyValue[0];
						keyValue[0] = keyValue[0].toLowerCase();
						// remove any internal spaces
						keyValue[0] = keyValue[0].replaceAll("\\s", "");
						// replace non-alphanumeric char with underscores,
						// except '='
						keyValue[0] = keyValue[0].replaceAll("[^A-Za-z0-9=]", "_");

						keyValue[1] = keyValue[1].trim();

						addToPListIfUnique(pList, new NameValue(keyValue[0], keyValue[1]));
						// If we munged the parameter name to conform to LTI 1,
						// repeat it in unmodified form
						if( !unmodifiedParamName.equals(keyValue[0]) )
						{
							addToPListIfUnique(pList, new NameValue(unmodifiedParamName, keyValue[1]));
						}
					}
				}
			}
		}
		return pList;
	}

	/**
	 * Because we may have munged and unmunged parameter names, and intend to
	 * included both, we may create duplicates if the user has over-zealously
	 * explicitly provided their parameters in both munged and unmunged form, or
	 * if we are re-saving a pList for which we have already provided a
	 * munge/unmunged pair.<br>
	 * Note also that the NameValue class has an equals(..) method which
	 * compares value (second) only, which we ignore here by having an explicit
	 * iterator instead of relying on Container.contains(...)
	 * 
	 * @param pList
	 * @param nameValue
	 * @return
	 */
	private NameValue addToPListIfUnique(List<NameValue> pList, NameValue nameValue)
	{
		boolean matchExists = false;
		for( NameValue nv : pList )
		{
			if( nv.getFirst().equals(nameValue.getFirst()) && nv.getSecond().equals(nameValue.getSecond()) )
			{
				matchExists = true;
				break;
			}
		}
		if( matchExists )
		{
			return null;
		}

		pList.add(nameValue);
		return nameValue;
	}

	@Override
	public String customParamListToString(List<NameValue> customParams)
	{
		StringBuilder value = new StringBuilder("");
		if( customParams.size() > 0 )
		{
			Iterator<NameValue> iterator = customParams.iterator();
			while( iterator.hasNext() )
			{
				NameValue param = iterator.next();
				value.append(param.getFirst());
				value.append("=");
				value.append(param.getSecond());

				if( iterator.hasNext() )
				{
					value.append(", ");
				}
			}
		}
		return value.toString();
	}

	@Override
	public String findApplicableIconUrl(Attachment attachment)
	{
		String iconUrl = (String) attachment.getData(ExternalToolConstants.ICON_URL);
		if( Check.isEmpty(iconUrl) )
		{
			String externalToolUuid = (String) attachment.getData(ExternalToolConstants.EXTERNAL_TOOL_PROVIDER_UUID);
			if( !Check.isEmpty(externalToolUuid) )
			{
				ExternalTool lti;
				if( externalToolUuid.equals(ExternalToolConstants.AUTOMATIC_UUID) )
				{
					lti = this.findMatchingBaseURL((String) attachment.getData(ExternalToolConstants.LAUNCH_URL));
				}
				else
				{
					lti = this.getByUuid(externalToolUuid);
				}
				iconUrl = lti != null ? lti.getAttribute(ExternalToolConstants.ICON_URL) : null;

			}
			if( Check.isEmpty(iconUrl) )
			{
				MimeEntry mimeEntry = webMimeService.getEntryForMimeType(ExternalToolConstants.MIME_TYPE);
				URL mimeUrl = webMimeService.getIconForEntry(mimeEntry);
				iconUrl = mimeUrl != null ? mimeUrl.toString() : null;
			}
		}
		return iconUrl;
	}

}
