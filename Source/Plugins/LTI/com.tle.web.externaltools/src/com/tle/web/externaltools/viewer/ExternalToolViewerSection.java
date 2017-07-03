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

package com.tle.web.externaltools.viewer;

import static com.tle.common.externaltools.constants.ExternalToolConstants.BASIC_LTI_LAUNCH_REQUEST;
import static com.tle.common.externaltools.constants.ExternalToolConstants.EQUELLA_PRODUCT_CODE;
import static com.tle.common.externaltools.constants.ExternalToolConstants.INSTRUCTOR_ROLE_URN;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LAUNCHER_ROLES;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LEARNER_ROLE_URN;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LIS_PERSON_NAME_FAMILY;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LIS_PERSON_NAME_GIVEN;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LTI_MESSAGE_TYPE;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LTI_MESSAGE_TYPE_VALUES;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LTI_VERSION;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LTI_VERSION_1_VALUE;
import static com.tle.common.externaltools.constants.ExternalToolConstants.LTI_VERSION_VALUES;
import static com.tle.common.externaltools.constants.ExternalToolConstants.RESOURCE_LINK_ID;
import static com.tle.common.externaltools.constants.ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE;
import static com.tle.common.externaltools.constants.ExternalToolConstants.TOOL_CONSUMER_INFO_VERSION;
import static com.tle.common.externaltools.constants.ExternalToolConstants.TOOL_CONSUMER_INSTANCE_CONSUMER_CONTACT_EMAIL;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.name.Named;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.externaltools.SourcedIdPackage;
import com.tle.common.externaltools.SourcedIdPackage.SrcdIdPkgSerializer;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.http.Request.Method;
import com.tle.web.lti.LtiData.OAuthData;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.SimpleFormAction;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;

@SuppressWarnings("nls")
@Bind
public class ExternalToolViewerSection extends AbstractViewerSection<ExternalToolViewerSection.ToolModel>
{
	private static final Logger LOGGER = Logger.getLogger(ExternalToolViewerSection.class);

	@Inject
	@Named("external.tool.contact.email")
	private String externalToolContactEmail = "";

	@Inject
	private ExternalToolsService toolService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ViewItemUrlFactory itemUrls;

	@TreeLookup
	private RootItemFileSection rootItemFileSection;

	@PlugKey("viewer.error.noprovider")
	private static Label NO_PROVIDER_ERROR;

	@PlugKey("viewer.error.lti.nonconformance")
	private static String LTI_NONCONFORMANCE_MSG_KEY;

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext context, ViewItemResource resource) throws IOException
	{
		Decorations.getDecorations(context).clearAllDecorations();

		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);

		IAttachment attachment = viewableResource.getAttachment();
		Decorations.getDecorations(context).setTitle(new TextLabel(attachment.getDescription()));
		String launchUrl = (String) attachment.getData(ExternalToolConstants.LAUNCH_URL);
		String ltiProviderUUID = (String) attachment.getData(ExternalToolConstants.EXTERNAL_TOOL_PROVIDER_UUID);
		ExternalTool toolProvider = null;
		// look up selected provider
		if( ExternalToolConstants.AUTOMATIC_UUID.equals(ltiProviderUUID) )
		{
			// sanity check: should always be a valid launchUrl if
			// AUTOMATIC_UUID
			if( !Check.isEmpty(launchUrl) )
			{
				toolProvider = toolService.findMatchingBaseURL(launchUrl);
			}
		}
		else
		{
			// Attachment had specified its own launchUrl
			toolProvider = toolService.getByUuid(ltiProviderUUID);
		}

		// use base url for no launch-url/selected provider combo
		if( launchUrl == null )
		{
			// neither provider nor launch url -> error
			if( toolProvider == null )
			{
				getModel(context).setError(NO_PROVIDER_ERROR);
				return viewFactory.createResult("lti-error.ftl", this);
			}
			launchUrl = toolProvider.getBaseURL();
			if( launchUrl == null )
			{
				// improbable, but sonar is complaining
				throw new Error("Attempted launch with no URL");
			}
		}

		// sanity check on whatever we've got for a LaunchURL
		URL urlLaunchURL = new URL(launchUrl);

		Map<String, Object> attachmentData = attachment.getDataAttributesReadOnly();
		String consumerKey = null;
		String sharedSecret = null;
		if( attachmentData.containsKey(ExternalToolConstants.CONSUMER_KEY) )
		{
			consumerKey = (String) attachmentData.get(ExternalToolConstants.CONSUMER_KEY);
		}
		else if( toolProvider != null )
		{
			consumerKey = toolProvider.getConsumerKey();
		}
		if( attachmentData.containsKey(ExternalToolConstants.SHARED_SECRET) )
		{
			sharedSecret = (String) attachmentData.get(ExternalToolConstants.SHARED_SECRET);
		}
		else if( toolProvider != null )
		{
			sharedSecret = toolProvider.getSharedSecret();
		}

		// These booleans are the EQUELLA config T/Fs for sending personal user
		// details, and prevail over any lms POST data.
		boolean shareEmail = deriveBoolean(attachmentData.get(ExternalToolConstants.SHARE_EMAIL),
			toolProvider != null ? toolProvider.getShareEmail() : null);
		boolean shareName = deriveBoolean(attachmentData.get(ExternalToolConstants.SHARE_NAME),
			toolProvider != null ? toolProvider.getShareName() : null);

		// This map is used to create the hidden inputs in the form
		Map<String, String> formParams = new TreeMap<String, String>();

		addDynamicLtiParameters(context, formParams, attachment, toolProvider, shareEmail, shareName,
			(Item) resource.getViewableItem().getItem());

		// if this is a POST request, we assume this to be a relayed request
		// from an LMS, with EQUELLA acting as the front for an actual Tool
		// Provider.
		if( context.getRequest().getMethod().equals(Method.POST.toString()) )
		{
			equellaAsProvider(formParams, context.getRequest(), shareEmail, shareName);
		}
		else
		{
			// EQUELLA is consumer - impose our own defaults
			addDefaultLtiParameters(formParams);
			setRole(formParams);
		}

		// We add add/overwrite our resource_link_id AFTER having tested if
		// EQUELLA is in Provider mode and we've verified that the consumer
		// sent their own (which we'd ignore anyway).
		formParams.put(ExternalToolConstants.RESOURCE_LINK_ID, attachment.getUuid());

		// Now we've got our final form contents, calculate and add the OAuth
		// signature elements
		calculateAndAddOauth(formParams, consumerKey, sharedSecret, urlLaunchURL);

		FormTag formTag = context.getForm();
		formTag.setName("ltiLaunchForm");
		formTag.setElementId(new SimpleElementId("ltiLaunchForm"));
		formTag.setAction(new SimpleFormAction(launchUrl));
		formTag.setEncoding("application/x-www-form-urlencoded");
		formTag.setMethod("POST");
		for( Entry<String, String> param : formParams.entrySet() )
		{
			String val = param.getValue();
			if( !Check.isEmpty(val) )
			{
				formTag.addHidden(new HiddenInput(param.getKey(), val));
			}
		}
		// a neater way of calling "submit" than to have a dedicated one-line
		// javascript file
		formTag.addReadyStatements(
			Js.statement(Js.methodCall(Jq.$('#' + formTag.getElementId(context)), Js.function("submit"))));
		return null;

	}

	/**
	 * In the case where an external consumer (such as an LMS) is using EQUELLA
	 * as a front for a actual tool provider.
	 */
	private void equellaAsProvider(Map<String, String> formParams, HttpServletRequest servletRequest,
		boolean shareEmail, boolean shareName)
	{
		// ... overwrite or create parameters with values from the LMS:
		overwriteOrAddWithRequestParameters(formParams, servletRequest, shareEmail, shareName);

		String messageType = servletRequest.getParameter(LTI_MESSAGE_TYPE);
		if( !Check.isEmpty(messageType) )
		{
			formParams.put(LTI_MESSAGE_TYPE, messageType);
		}

		String ltiVersion = servletRequest.getParameter(LTI_VERSION);
		if( !Check.isEmpty(ltiVersion) )
		{
			formParams.put(LTI_VERSION, ltiVersion);
		}

		verifyLTIConformance(servletRequest);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_LOW)
	public void ensurePOST(SectionInfo info)
	{
		final ViewItemResource viewItemResource = rootItemFileSection.getViewItemResource(info);
		if( viewItemResource != null && viewItemResource.getViewer() == this )
		{
			info.preventGET();
		}
	}

	@SuppressWarnings("unchecked")
	private void addDynamicLtiParameters(RenderContext context, Map<String, String> formParams, IAttachment attachment,
		ExternalTool provider, boolean shareEmail, boolean shareName, Item item)
	{
		UserBean userDetails = CurrentUser.getDetails();

		String returnUrl = itemUrls.createItemUrl(context, item.getItemId()).getHref();
		formParams.put(ExternalToolConstants.LAUNCH_PRESENTATION_RETURN_URL, returnUrl);

		formParams.put(ExternalToolConstants.USER_ID, CurrentUser.getUserID());

		formParams.put(ExternalToolConstants.LAUNCH_PRESENTATION_LOCALE, CurrentLocale.getLocale().toString());

		// share name/email permissions from attachment prevail if they exist
		if( shareEmail && !Check.isEmpty(userDetails.getEmailAddress()) )
		{
			formParams.put(LIS_PERSON_CONTACT_EMAIL_PRIMARY, userDetails.getEmailAddress());
		}
		if( shareName )
		{
			String firstName = userDetails.getFirstName();
			boolean hasGiven = !Check.isEmpty(firstName);
			String familyName = userDetails.getLastName();
			boolean hasFamily = !Check.isEmpty(familyName);

			if( hasGiven )
			{
				formParams.put(LIS_PERSON_NAME_GIVEN, firstName);
			}
			if( hasFamily )
			{
				formParams.put(LIS_PERSON_NAME_FAMILY, familyName);
			}

			String compoundName = null;
			if( hasGiven )
			{
				compoundName = firstName;
				if( hasFamily )
				{
					compoundName += " " + familyName;
				}
			}
			else if( hasFamily )
			{
				compoundName = familyName;
			}

			if( !Check.isEmpty(compoundName) )
			{
				formParams.put(ExternalToolConstants.LIS_PERSON_NAME_FULL, compoundName);
			}
		}

		// custom params. Add both - don't overwrite
		List<NameValue> customParams = new ArrayList<NameValue>();
		Map<String, Object> attachmentData = attachment.getDataAttributesReadOnly();

		if( attachmentData.containsKey(ExternalToolConstants.CUSTOM_PARAMS) )
		{
			Object paramContent = attachmentData.get(ExternalToolConstants.CUSTOM_PARAMS);
			if( paramContent instanceof Collection )
			{
				customParams.addAll(((Collection<? extends NameValue>) paramContent));
			}
			else if( paramContent instanceof String )
			{
				String[] nv = ((String) paramContent).split("=");
				if( nv.length == 2 )
				{
					customParams.add(new NameValue(nv[0].trim(), nv[1].trim()));
				}
			}
		}

		if( provider != null && !Check.isEmpty(provider.getCustomParams()) )
		{
			customParams.addAll(provider.getCustomParams());
		}

		for( NameValue customParam : customParams )
		{
			if( !customParam.getFirst().startsWith(ExternalToolConstants.CUSTOM_PARAMS_POST_PREFIX) )
			{
				formParams.put(ExternalToolConstants.CUSTOM_PARAMS_POST_PREFIX + customParam.getFirst(),
					customParam.getSecond());
			}
			else
			{
				formParams.put(customParam.getFirst(), customParam.getSecond());
			}
		}
		// Here to make some tools that require it work
		formParams.put(ExternalToolConstants.CONTEXT_ID, item.getItemId().toString());
		formParams.put(ExternalToolConstants.CONTEXT_LABEL, item.getUuid());
		formParams.put(ExternalToolConstants.CONTEXT_TITLE, CurrentLocale.get(item.getName(), item.getUuid()));

		formParams.put(ExternalToolConstants.RESOURCE_LINK_TITLE, attachment.getDescription());
	}

	private void addDefaultLtiParameters(Map<String, String> formParams)
	{
		LOGGER.debug("Using the optional-config external.tool.contact.email of ["+externalToolContactEmail+"]");
		formParams.put(LTI_MESSAGE_TYPE, BASIC_LTI_LAUNCH_REQUEST);
		formParams.put(LTI_VERSION, LTI_VERSION_1_VALUE);
		formParams.put(TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE, EQUELLA_PRODUCT_CODE);
		formParams.put(TOOL_CONSUMER_INSTANCE_CONSUMER_CONTACT_EMAIL, externalToolContactEmail);
		formParams.put(TOOL_CONSUMER_INFO_VERSION, ApplicationVersion.get().getDisplay());
		formParams.put(ExternalToolConstants.CONTEXT_TYPE, ExternalToolConstants.CONTEXT_TYPE_EQUELLA_ITEM);
		// EQUELLA (no longer) implements the LTI viewer as an iframe, so best
		// left unset ...?
		// formParams.put(ExternalToolUtils.LAUNCH_PRESENTATION_DOCUMENT_TARGET,
		// ExternalToolUtils.LAUNCH_TARGET_IFRAME);
	}

	/**
	 * For the purposes of imsglobal certification, we needed to be able to send
	 * with Learner role (or none). During testing this was done with a one-off
	 * by-hand 'variation' by changing this Boolean variable in a debugging
	 * session to null or TRUE.
	 */
	private void setRole(Map<String, String> formParams)
	{
		Boolean sendAsLearner = Boolean.FALSE;
		// a breakpoint here and manual change value, if that's what we need to
		// do ...
		if( sendAsLearner != null )
		{
			if( sendAsLearner )
			{
				formParams.put(LAUNCHER_ROLES, LEARNER_ROLE_URN);
			}
			else
			{
				formParams.put(LAUNCHER_ROLES, INSTRUCTOR_ROLE_URN);
			}
		}
	}

	/**
	 * overwrite or create these parameters with values from lms:
	 * resource_link_id, tool_consumer_info_*, lis_outcome_service_url,
	 * lis_result_sourcedid, launch_presentation_return_url, context_*
	 */
	private void overwriteOrAddWithRequestParameters(Map<String, String> formParams, HttpServletRequest request,
		boolean shareEmail, boolean shareName)
	{
		String oldValPeek = null;

		// The following keys refer to parameters which if present, are attached
		// to (or overwrite) the request without any ado.
		// LIS_OUTCOME_SERVICE_URL & LIS_RESULT_SOURCEDID are dealt with
		// separately
		//@formatter:off
		final String[] rqParams = {
			ExternalToolConstants.LAUNCHER_ROLES, ExternalToolConstants.ROLE_SCOPE_MENTOR,
			ExternalToolConstants.RESOURCE_LINK_ID, ExternalToolConstants.RESOURCE_LINK_TITLE, ExternalToolConstants.RESOURCE_LINK_DESCRIPTION,
			ExternalToolConstants.USER_ID,
			ExternalToolConstants.LAUNCH_PRESENTATION_RETURN_URL, ExternalToolConstants.LAUNCH_PRESENTATION_DOCUMENT_TARGET, ExternalToolConstants.LAUNCH_PRESENTATION_LOCALE,
			ExternalToolConstants.CONTEXT_ID, ExternalToolConstants.CONTEXT_TYPE, ExternalToolConstants.CONTEXT_TITLE, ExternalToolConstants.CONTEXT_LABEL
			};
		//@formatter:on
		for( String paramKey : rqParams )
		{
			if( !Check.isEmpty(request.getParameter(paramKey)) )
			{
				// we aren't using the return value here, just allowing for a
				// debug session peek
				oldValPeek = formParams.put(paramKey, request.getParameter(paramKey));
			}
		}

		// If there was no LAUNCHER_ROLE string from the consumer, dictate
		// LEARNER
		if( Check.isEmpty(formParams.get(LAUNCHER_ROLES)) )
		{
			formParams.put(LAUNCHER_ROLES, LEARNER_ROLE_URN);
		}

		// make up a combined product name, so we may end up with something like
		// "Moodle - EQUELLA"
		if( !Check.isEmpty(request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE)) )
		{
			String productCode = request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);
			oldValPeek = formParams.get(ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);
			if( !Check.isEmpty(oldValPeek) )
			{
				productCode += " - " + oldValPeek;
			}
			formParams.put(ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE, productCode);
		}

		// Something similar for product version
		if( !Check.isEmpty(request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INFO_VERSION)) )
		{
			String productVersion = request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INFO_VERSION);
			oldValPeek = formParams.get(ExternalToolConstants.TOOL_CONSUMER_INFO_VERSION);
			if( !Check.isEmpty(oldValPeek) )
			{
				productVersion += " - " + oldValPeek;
			}
			formParams.put(ExternalToolConstants.TOOL_CONSUMER_INFO_VERSION, productVersion);
		}

		// only if EQUELLA says so will we pass on the user email ...
		if( shareEmail )
		{
			if( !Check.isEmpty(request.getParameter(ExternalToolConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY)) )
			{
				String personEmail = request.getParameter(ExternalToolConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);
				formParams.put(ExternalToolConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, personEmail);
			}
		}

		// ...likewise for the user's name
		if( shareName )
		{
			if( !Check.isEmpty(request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_FULL)) )
			{
				String personFull = request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_FULL);
				formParams.put(ExternalToolConstants.LIS_PERSON_NAME_FULL, personFull);
			}
			if( !Check.isEmpty(request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_FAMILY)) )
			{
				String personFamily = request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_FAMILY);
				formParams.put(ExternalToolConstants.LIS_PERSON_NAME_FAMILY, personFamily);
			}
			if( !Check.isEmpty(request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_GIVEN)) )
			{
				String personGiven = request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_GIVEN);
				formParams.put(ExternalToolConstants.LIS_PERSON_NAME_GIVEN, personGiven);
			}
		}

		// LMS may have a few of it extension values it likes to send, (eg
		// ext_lms, ext_submit)so we'll onsend them ...
		for( Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements(); )
		{
			String paramKey = paramNames.nextElement();
			if( paramKey.startsWith(ExternalToolConstants.EXT_PREFIX) )
			{
				formParams.put(paramKey, request.getParameter(paramKey));
			}
		}

		addResultOutcomeParameters(formParams, request);
	}

	/**
	 * To enable the external tool to send gradebook information back to the LMS
	 * (via EQUELLA), we substitute the LMS's outcome_service_url for EUQLLA's
	 * own, meanwhile preserving the original LMS outcome_service_url as an
	 * appended item of information when EQUELLA onsends to LMS's
	 * lis_result_sourcedid.<br>
	 * We extend the original lis_result_sourcedid with such information as
	 * EQUELLA can use to identify where to onsend the gradebook results back
	 * to.
	 * 
	 * @param formParams
	 * @param request
	 */
	protected void addResultOutcomeParameters(Map<String, String> formParams, HttpServletRequest request)
	{
		String lmsOutcomeServiceUrl = request.getParameter(ExternalToolConstants.LIS_OUTCOME_SERVICE_URL);
		// If there's an outcome service url, we want to replace it with our own
		if( !Check.isEmpty(lmsOutcomeServiceUrl) )
		{
			String instiUrlExtended = institutionService
				.institutionalise(ExternalToolConstants.OUTCOME_SERVICE_URL_PATH);

			formParams.put(ExternalToolConstants.LIS_OUTCOME_SERVICE_URL, instiUrlExtended);
		}

		String oAuthConsumerKeyForLms = null;
		UserState userState = CurrentUser.getUserState();

		if( userState instanceof LtiUserState )
		{
			LtiUserState ltiUserState = (LtiUserState) userState;
			try
			{
				OAuthData oauthData = ltiUserState.getData().getOAuthData();
				oAuthConsumerKeyForLms = oauthData.getConsumerKey();
			}
			catch( Exception cce )
			{
				throw new RuntimeException("Could not retrieve Lti UserState info", cce);
			}
		}

		String lmsResultSourcedid = request.getParameter(ExternalToolConstants.LIS_RESULT_SOURCEDID);

		// Being wary of Icodeon's 512 character limit ...!
		SourcedIdPackage sourcedIdPackage = new SourcedIdPackage(lmsResultSourcedid, lmsOutcomeServiceUrl,
			oAuthConsumerKeyForLms);

		Gson gson = new GsonBuilder().registerTypeAdapter(SourcedIdPackage.class, new SrcdIdPkgSerializer()).create();

		String lmsSourcedIdToSend = gson.toJson(sourcedIdPackage);
		formParams.put(ExternalToolConstants.LIS_RESULT_SOURCEDID, lmsSourcedIdToSend);
	}

	/**
	 * If EQUELLA is provider, any resource_link_id sent by consumer is
	 * irrelevant, but conformance to imsglobal certifications requires that we
	 * throw an error if resource_link_id is absent. Consumer could send any
	 * random stuff and we'd be happy.
	 * 
	 * @param servletRequest
	 */
	private void verifyLTIConformance(HttpServletRequest servletRequest)
	{
		String messageType = servletRequest.getParameter(LTI_MESSAGE_TYPE);
		boolean emptyOrInvalidMessageType = Check.isEmpty(messageType);
		String ltiVersion = servletRequest.getParameter(LTI_VERSION);
		boolean emptyOrInvalidLtiVersion = Check.isEmpty(ltiVersion);
		String rsrcLinkId = servletRequest.getParameter(RESOURCE_LINK_ID);
		boolean emptyRsrcLinkId = Check.isEmpty(rsrcLinkId);

		if( !emptyOrInvalidLtiVersion )
		{
			boolean isAcceptable = false;
			for( String acceptable : LTI_VERSION_VALUES.split(",") )
			{
				if( acceptable.equals(ltiVersion) )
				{
					isAcceptable = true;
					break;
				}
			}
			emptyOrInvalidLtiVersion = !isAcceptable;
		}

		if( !emptyOrInvalidMessageType )
		{
			boolean isAcceptable = false;
			for( String acceptable : LTI_MESSAGE_TYPE_VALUES.split(",") )
			{
				if( acceptable.equals(messageType) )
				{
					isAcceptable = true;
					break;
				}
			}
			emptyOrInvalidMessageType = !isAcceptable;
		}

		if( emptyOrInvalidMessageType || emptyOrInvalidLtiVersion || emptyRsrcLinkId )
		{
			String nonconformanceBlurb = CurrentLocale.get(LTI_NONCONFORMANCE_MSG_KEY);

			if( emptyOrInvalidLtiVersion )
			{
				nonconformanceBlurb += "\n" + LTI_VERSION;
				if( !Check.isEmpty(ltiVersion) )
				{
					nonconformanceBlurb += " (" + ltiVersion + "), ";
				}
			}
			if( emptyOrInvalidMessageType )
			{
				nonconformanceBlurb += "\n" + LTI_MESSAGE_TYPE;
				if( !Check.isEmpty(messageType) )
				{
					nonconformanceBlurb += " (" + messageType + "), ";
				}
			}
			if( emptyRsrcLinkId )
			{
				nonconformanceBlurb += "\n" + RESOURCE_LINK_ID;
			}

			throw new RuntimeException(nonconformanceBlurb);
		}
	}

	/**
	 * The error that Moodle showed us how to do: where URL query parameters
	 * exist, add them to (a copy of) the form for the purposes of calculating
	 * the OAuth signature, but ensure that the original URL including its query
	 * string, and params (without the added query-string pairs) are what's sent
	 * to the provider.
	 * 
	 * @param formParams
	 * @param sharedSecret
	 * @param launchUrl
	 */
	private void calculateAndAddOauth(Map<String, String> formParams, String consumerKey, String sharedSecret,
		URL urlLaunchURL)
	{
		// don't bother signing if there's no secret
		if( !Check.isEmpty(sharedSecret) )
		{
			Map<String, String[]> urlQuery = SectionUtils.parseParamString(urlLaunchURL.getQuery());

			// Allow for multiple values for same key, eg ?foo=bar&foo=golly
			Map<String, String[]> formParamsConverted = convertToStringArrayMap(formParams);

			Map<String, String[]> queryPaddedFormParams = formParamsConverted;

			if( !Check.isEmpty(urlQuery) )
			{
				queryPaddedFormParams = new TreeMap<String, String[]>(formParamsConverted);
				for( Entry<String, String[]> entry : urlQuery.entrySet() )
				{
					queryPaddedFormParams.put(entry.getKey(), entry.getValue());
				}
			}

			// DO as OAuth does: strip off the query-string if it's present
			String urlStr = urlLaunchURL.toExternalForm();
			int q = urlStr.indexOf('?');
			if( q >= 0 )
			{
				urlStr = urlStr.substring(0, q);
			}

			// This method returns both the original parameters and the oauth
			// specific parameters. We could harmlessly overwrite the original
			// formParams, but the easiest way to avoid adding the queryString
			// parameters to the final output formParams collection - which we
			// specifically DON'T want here - is to only add the oauth_
			// parameters
			List<Entry<String, String>> oauthExtendedParams = toolService.getOauthSignatureParams(consumerKey,
				sharedSecret, urlStr, queryPaddedFormParams);
			for( Entry<String, String> entry : oauthExtendedParams )
			{
				if( entry.getKey().startsWith("oauth_") )
				{
					formParams.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	private static boolean deriveBoolean(Object attachmentSetting, Boolean providerSetting)
	{
		boolean share = false;
		if( attachmentSetting != null )
		{
			share = (Boolean) attachmentSetting;
		}
		else if( providerSetting != null )
		{
			share = providerSetting;
		}
		return share;
	}

	/**
	 * Copy a Map<String, String> to a Map<String, String[]>
	 * 
	 * @param formParams
	 * @return Map of identical contents except single string values are an
	 *         array of a single string
	 */
	private static Map<String, String[]> convertToStringArrayMap(Map<String, String> formParams)
	{
		Map<String, String[]> formParamsConverted = new TreeMap<String, String[]>();

		for( Entry<String, String> entry : formParams.entrySet() )
		{
			formParamsConverted.put(entry.getKey(), new String[]{entry.getValue()});
		}
		return formParamsConverted;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ToolModel();
	}

	public static class ToolModel
	{
		Label error;

		public Label getError()
		{
			return error;
		}

		public void setError(Label error)
		{
			this.error = error;
		}
	}
}
