package com.tle.core.pss.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.Constants;
import com.google.common.primitives.Ints;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pearson.pdn.icodeon.exception.IcodeonException;
import com.pearson.pdn.icodeon.model.DeleteCartridgeRequestParameters;
import com.pearson.pdn.icodeon.model.IcodeonConstants;
import com.pearson.pdn.icodeon.model.IcodeonConsumer;
import com.pearson.pdn.icodeon.model.LaunchPlayerRequestParameters;
import com.pearson.pdn.icodeon.model.PostCartridgeRequestParameters;
import com.pearson.pdn.icodeon.model.Request;
import com.pearson.pdn.icodeon.model.RequestHeader;
import com.pearson.pdn.icodeon.model.RequestParameters;
import com.pearson.pdn.icodeon.service.DeleteCartridgeService;
import com.pearson.pdn.icodeon.service.LaunchPlayerService;
import com.pearson.pdn.icodeon.service.PostCartridgeService;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.system.PearsonScormServicesSettings;
import com.tle.common.Check;
import com.tle.common.externaltools.SourcedIdPackage;
import com.tle.common.externaltools.SourcedIdPackage.SrcdIdPkgSerializer;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LocaleUtils;
import com.tle.core.guice.Bind;
import com.tle.core.pss.entity.PssCallbackLog;
import com.tle.core.pss.notification.PearsonScormServicesOperationFactory;
import com.tle.core.pss.util.PSSConstants;
import com.tle.core.security.RunAsUser;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.lti.LtiData.OAuthData;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind(PearsonScormServicesService.class)
@Singleton
public class PearsonScormServicesServiceImpl implements PearsonScormServicesService
{
	private static Logger LOGGER = Logger.getLogger(PearsonScormServicesServiceImpl.class);

	@Inject
	private ItemService itemService;
	@Inject
	private PearsonScormServicesCallbackService pssCallbackService;
	@Inject
	private ConfigurationService configurationService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	protected RunAsUser runAsUser;
	@Inject
	private UrlService urlService;
	@Inject
	private PearsonScormServicesOperationFactory pssOpFactory;
	@Inject
	private WorkflowFactory workflowFactory;

	@PlugKey("service.settings.error")
	private static String NO_PSS_SETTING;

	private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

	@Override
	public boolean isEnabled()
	{
		PearsonScormServicesSettings settings = getPearsonScormServicesSettings();
		return settings.isEnable();
	}

	@Override
	@Transactional
	public void addScormPackage(Item item, CustomAttachment scormAttachment)
	{
		PostCartridgeRequestParameters pcrp = new PostCartridgeRequestParameters();
		pcrp.setResourceUrl(attachmentResourceService.getPackageZipFileUrl(item, scormAttachment).toString());
		pcrp.setResourceId(prependAccountNamespace(item.getUuid()));
		pcrp.setResourceVersion(item.getVersion());
		pcrp.setResourceFileName(scormAttachment.getUrl());
		pcrp.setResourceTitle(scormAttachment.getDescription());

		HttpResponse response = null;

		try
		{
			// Post cartridge
			Request req = createRequest(pcrp, IcodeonConstants.CONTENT_TYPE_JSON, getPearsonScormServicesSettings()
				.getBaseUrl());
			PostCartridgeService service = new PostCartridgeService(req, createConsumer());
			response = service.execute();

			int responseHttpCode = response.getStatusLine().getStatusCode();
			if( 409 == responseHttpCode )
			{
				LOGGER.debug("Conflict detected! Deleting Package");
				deleteScormPackage(item, false);
			}
			else if( 202 == responseHttpCode )
			{
				PssCallbackLog logEntry = getLogEntry(item);
				logEntry.setMessage("Adding for: " + item.getItemId());
				createLogEntry(logEntry, getTrackingNumber(response));
				pssCallbackService.addCallbackLogEntry(logEntry);
				LOGGER.debug("Saving to PSS Callback log: " + logEntry.getTrackingNumber());
			}
			else
			{
				// Error from PSS unknown reason
				PssCallbackLog logEntry = getLogEntry(item);
				logEntry.setMessage("Unknown error occured when uploading to Pearson SCORM Services: "
					+ response.getStatusLine());
				createLogEntry(logEntry, -2);
				pssCallbackService.addCallbackLogEntry(logEntry);
				LOGGER.debug("Unknown error uploading to PSS! Sending notification");
				itemService.operation(item.getItemId(), pssOpFactory.notifyContributor(),
					workflowFactory.reIndexIfRequired());
			}

			HttpEntity entity = response.getEntity();
			if( entity != null )
			{
				try
				{
					InputStream instream = entity.getContent();
					// Closing the input stream will trigger connection release
					instream.close();
				}
				catch( IllegalStateException e )
				{
					LOGGER.debug(e.getMessage());
				}
				catch( IOException e )
				{
					LOGGER.debug(e.getMessage());
				}
			}
		}
		catch( IcodeonException ie )
		{
			throw new RuntimeException(ie);
		}
	}

	private PssCallbackLog getLogEntry(Item item)
	{
		PssCallbackLog logEntry = pssCallbackService.getCallbackLogEntry(item);
		if( logEntry == null )
		{
			logEntry = new PssCallbackLog();
			logEntry.setItem(item);
		}
		return logEntry;
	}

	private Request createRequest(RequestParameters params, String contentType, String baseUrl) throws IcodeonException
	{
		Request req = new Request();
		RequestHeader header = new RequestHeader();
		header.setContentType(contentType);
		req.setBaseUrl(baseUrl);
		req.setRequestParameters(params);
		req.setRequestHeader(header);
		return req;
	}

	private IcodeonConsumer createConsumer()
	{
		return createConsumer(getPearsonScormServicesSettings());
	}

	private IcodeonConsumer createConsumer(PearsonScormServicesSettings settings)
	{
		return new IcodeonConsumer(settings.getConsumerKey(), settings.getConsumerSecret());
	}

	@Override
	public void deleteScormPackage(Item item, boolean log)
	{
		DeleteCartridgeRequestParameters dcrp = new DeleteCartridgeRequestParameters();
		dcrp.setResourceId(prependAccountNamespace(item.getUuid()));
		dcrp.setResourceVersion(item.getVersion());

		HttpResponse response = null;
		try
		{
			Request req = createRequest(dcrp, IcodeonConstants.CONTENT_TYPE_JSON, getPearsonScormServicesSettings()
				.getBaseUrl());
			DeleteCartridgeService dcs = new DeleteCartridgeService(req, createConsumer());
			response = dcs.execute();

			if( log )
			{
				// Log entry
				PssCallbackLog logEntry = createLogEntry(getLogEntry(item), getTrackingNumber(response));
				logEntry.setMessage("Deleting for: " + item.getItemId());
				pssCallbackService.addCallbackLogEntry(logEntry);
				LOGGER.debug("Saving to PSS Callback log: " + logEntry.getTrackingNumber());
			}
		}
		catch( IcodeonException ie )
		{
			throw new RuntimeException(ie);
		}
	}

	private PssCallbackLog createLogEntry(PssCallbackLog logEntry, int trackingNo)
	{
		logEntry.setTrackingNumber(trackingNo);
		logEntry.setLastAttempt(new Date());
		logEntry.setAttemptNumber(0);
		logEntry.setInstitution(CurrentInstitution.get());
		return logEntry;
	}

	/**
	 * Using the PSS LaunchPlayerRequestParameters class, wew need to avoid
	 * setting any parameter twice, because that class will throw an error.
	 * lti_message-type & lti_version are set by the
	 * LaunchPlayerRequestParameters constructor. Mandatory parameters are set
	 * by individual set methods, and custom parameters are added without the
	 * "custom_" prefix, which is attached by the LaunchPlayerRequestParameter
	 * addCustom method.
	 */
	@Override
	public Map<String, String> pseudoLaunchScormPackage(HttpServletRequest srvltReq, IItem<?> item,
		IAttachment attachment, String pssReturnUrl, ResourceViewerConfig config)
	{
		try
		{
			String resourceId = prependAccountNamespace(item.getUuid());
			LaunchPlayerRequestParameters lprp = new LaunchPlayerRequestParameters();
			lprp.setResourceId(resourceId);
			lprp.setResourceVersion(item.getVersion());

			Request req = createRequest(lprp, IcodeonConstants.CONTENT_TYPE_FORM_URL_ENCODED,
				getPearsonScormServicesSettings().getBaseUrl());
			UserState userState = CurrentUser.getUserState();

			// If this is a LTI request, it's from an external source such as an
			// LMS. Otherwise it's an internal EQUELLA launch, and the
			// lis_result_sourcedid is of no consequence...
			if( userState instanceof LtiUserState )
			{
				LtiUserState ltiUserState = (LtiUserState) userState;

				String scormVersion = attachment.getData("SCORM_VERSION") != null ? attachment.getData("SCORM_VERSION")
					.toString() : null;
				if( Check.isEmpty(scormVersion) )
				{
					throw new RuntimeException("No SCORM_VERSION exists");
				}
				Map<String, String> launchParams = accumulateRequestParameters(srvltReq, ltiUserState, scormVersion);

				// the request userId, which requires a dedicated set call on
				// the LaunchPlayerRequestParameters.
				String lmsUserId = srvltReq.getParameter(ExternalToolConstants.USER_ID);
				lprp.setUserId(lmsUserId);

				// Our parameters in the request may contain some
				// custom_<mumble> parameters, in which case we strip off the
				// custom_ prefix, and allow the requestParameters dedicated
				// setCustom method to set is (in effect, replaces the custom_
				// prefix)
				for( String paramKey : launchParams.keySet() )
				{
					String paramVal = launchParams.get(paramKey);
					if( paramVal.startsWith(ExternalToolConstants.CUSTOM_PARAMS_POST_PREFIX) )
					{
						// Resource id & version are dealt with separately (and
						// cannot be set twice)
						if( IcodeonConstants.CUSTOM_RESOURCE_ID.equals(paramVal)
							|| IcodeonConstants.CUSTOM_RESOURCE_VERSION.equals(paramVal) )
						{
							continue;
						}
						lprp.setCustomParameter(paramKey,
							paramVal.substring(ExternalToolConstants.CUSTOM_PARAMS_POST_PREFIX.length()));
					}
					else if( ExternalToolConstants.LTI_VERSION.equals(paramKey)
						|| ExternalToolConstants.USER_ID.equals(paramKey)
						|| ExternalToolConstants.LTI_MESSAGE_TYPE.equals(paramKey) || paramKey.startsWith("oauth_") )
					{
						// by-passing lti_message_type & lti_version (see
						// above).
						// oauth values added here are discarded, as they're
						// obselete and are recalculated before sending
						// the request
						continue;
					}
					else if( !Check.isEmpty(paramVal) )
					{
						lprp.setOptionalParameter(paramKey, paramVal);
					}
				}
			}
			else
			{
				// We use the logged-in EQUELLA user's id, and other current
				// miscellanea
				lprp.setUserId(CurrentUser.getUserID());
				lprp.setOptionalParameter(ExternalToolConstants.LAUNCH_PRESENTATION_LOCALE,
					LocaleUtils.toHtmlLang(CurrentLocale.getLocale()));
				lprp.setOptionalParameter("roles", ExternalToolConstants.INSTRUCTOR_ROLE_URN);

				lprp.setResourceLinkId(attachment.getUuid());
				if( !Check.isEmpty(pssReturnUrl) )
				{
					lprp.setOptionalParameter(ExternalToolConstants.LAUNCH_PRESENTATION_RETURN_URL, pssReturnUrl);
				}

				String attName = attachment.getDescription();
				lprp.setOptionalParameter(ExternalToolConstants.RESOURCE_LINK_TITLE, !Check.isEmpty(attName) ? attName
					: attachment.getUrl());
			}

			if( config != null && config.isOpenInNewWindow() )
			{
				String h = config.getHeight();
				String w = config.getWidth();
				if( (!Check.isEmpty(h) && Ints.tryParse(h) != null) && (!Check.isEmpty(w) && Ints.tryParse(w) != null) )
				{
					lprp.setOptionalParameter(ExternalToolConstants.LAUNCH_PRESENTATION_HEIGHT, h);
					lprp.setOptionalParameter(ExternalToolConstants.LAUNCH_PRESENTATION_WIDTH, w);
				}
			}

			lprp.setCustomParameter(PSSConstants.CARTRIDGE_ID, resourceId);
			lprp.setCustomParameter(PSSConstants.VERSION, Integer.toString(1));
			// from the PSS API: Content authors will typically use a log level
			// of "scorm" and a log target of "window" to check the SCORM API
			// invocations and return values.

			// Filter level for client side logging.
			// "error" (logs any client side errors, including trapped errors
			// from SCORM content)
			// "debug" (logs errors and debug statements)
			// "scorm" (logs errors, debug statements and SCORM API invocations)
			lprp.setCustomParameter(PSSConstants.LOG_LEVEL, "scorm");

			// The output destination for client side logging.
			// "window" (show SCORM API logging in a new browser window), or
			// "console" (show logging in the browser console window)
			lprp.setCustomParameter(PSSConstants.LOG_TARGET, "console");
			lprp.setCustomParameter(PSSConstants.SESSION_TIMEOUT, "90");
			lprp.setCustomParameter(PSSConstants.CONTENT_HOST, CurrentInstitution.get().getUrl());

			// Custom UI features
			if( config != null )
			{
				Map<String, Object> attr = config.getAttr();
				String uiFeatures = (String) attr.get(PSSConstants.VIEWER_UI_FEATURES);
				lprp.setCustomParameter(PSSConstants.UI_FEATURES, Check.isEmpty(uiFeatures)
					? PSSConstants.DEFAULT_UI_FEATURES : uiFeatures);
			}

			// and semi - finally ...
			IcodeonConsumer consumer = createConsumer();
			LaunchPlayerService lergy = new LaunchPlayerService(req, consumer);

			// fake launch - all this does is compose and oauth sign a bunch of
			// parameters
			lergy.execute(true);

			HashMap<String, String> pseudoLaunchComposition = lergy.getLtiLaunchParams();

			return pseudoLaunchComposition;
		}
		catch( Exception exception )
		{
			throw new RuntimeException(exception);
		}
	}

	/**
	 * The Icodeon library doesn't allow a parameter to be overwritten once set,
	 * so we load the request parameters into a local structure prior to
	 * setting. LIS_OUTCOME_SERVICE_URL & LIS_RESULT_SOURCEDID are dealt with as
	 * special cases. Otherwise pass on all parameters.
	 */
	@SuppressWarnings("nls")
	private Map<String, String> accumulateRequestParameters(HttpServletRequest request, LtiUserState ltiUserState,
		String scormVersion)
	{
		Map<String, String> launchParams = new HashMap<String, String>();
		String imposedEQUELLAOutcomeServiceUrl = urlService
			.institutionalise(ExternalToolConstants.OUTCOME_SERVICE_URL_PATH);

		// If there's an outcome service url, we want to replace it with our own
		launchParams.put(ExternalToolConstants.LIS_OUTCOME_SERVICE_URL, imposedEQUELLAOutcomeServiceUrl);

		// saving the LMS's outcome service URL, and embedding it
		String lmsOutcomeServiceUrl = request.getParameter(ExternalToolConstants.LIS_OUTCOME_SERVICE_URL);

		for( Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements(); )
		{
			String paramKey = paramNames.nextElement();
			if( ExternalToolConstants.LIS_OUTCOME_SERVICE_URL.equals(paramKey) )
			{
				// dealt with above. We embed the lmsOutcomeServiceUrl in our
				// elaborated LIS_RESULT_SOURCEDID
				continue;
			}
			else if( ExternalToolConstants.LIS_RESULT_SOURCEDID.equals(paramKey) )
			{
				String oAuthConsumerKeyForLms = null;
				try
				{
					OAuthData oauthData = ltiUserState.getData().getOAuthData();
					oAuthConsumerKeyForLms = oauthData.getConsumerKey();
				}
				catch( Exception cce )
				{
					throw new RuntimeException("Could not retrieve Lti UserState info", cce);
				}

				String lmsResultSourcedid = request.getParameter(ExternalToolConstants.LIS_RESULT_SOURCEDID);

				// being wary of Icodeon's 512 character limit ...!
				SourcedIdPackage sourcedIdPackage = new SourcedIdPackage(lmsResultSourcedid, lmsOutcomeServiceUrl,
					oAuthConsumerKeyForLms);

				Gson gson = new GsonBuilder().registerTypeAdapter(SourcedIdPackage.class, new SrcdIdPkgSerializer())
					.create();

				String lmsSourcedIdToSend = gson.toJson(sourcedIdPackage);
				// Icodeon's cured its Json indigestion, we trust ...
				launchParams.put(ExternalToolConstants.LIS_RESULT_SOURCEDID, lmsSourcedIdToSend);
			}
			else
			{
				// just add it. Note that parameters keys of "custom_<mumble>"
				// will be further tuned after this method returns
				String paramValue = request.getParameter(paramKey);
				if( !Check.isEmpty(paramValue) )
				{
					// Could hardly matter, but to be sure
					paramValue = paramValue.replaceAll("\n", " ");
					launchParams.put(paramKey, paramValue);
				}
			}
		}

		return launchParams;
	}

	// Sometimes there is no tracking number with the response
	private int getTrackingNumber(HttpResponse response)
	{
		int tno = -1;
		try
		{
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, Constants.UTF8);
			Header ct = entity.getContentType();
			if( ct != null && ct.getValue().contains(IcodeonConstants.CONTENT_TYPE_JSON)
				&& responseString.contains("TrackingNumber") )
			{
				TrackingNo num = gson.fromJson(responseString, TrackingNo.class);
				tno = num.getTrackingNumber();
			}
		}
		catch( IOException | ParseException e )
		{
			// We can't blow up... just log
			LOGGER.error("Unable to get PSS tracking number from response");
		}
		return tno;
	}

	public String prependAccountNamespace(String rawUuid)
	{
		PearsonScormServicesSettings settings = getPearsonScormServicesSettings();
		String namespace = settings.getAccountNamespace();
		if( !Check.isEmpty(namespace) )
		{
			return namespace + "-" + rawUuid;
		}
		return rawUuid;
	}

	@Override
	public PearsonScormServicesSettings getPearsonScormServicesSettings()
	{
		return configurationService.getProperties(new PearsonScormServicesSettings());
	}

	@Override
	public String getLaunchURL()
	{
		String baseUrl = getPearsonScormServicesSettings().getBaseUrl();
		return baseUrl + PSSConstants.PSS_LAUNCH_URL_PATH;
	}

	public static class TrackingNo
	{
		/**
		 * The standard response on success from PSS
		 */
		private int trackingNumber;

		public int getTrackingNumber()
		{
			return trackingNumber;
		}

		public void setTrackingNumber(int trackingNumber)
		{
			this.trackingNumber = trackingNumber;
		}
	}

	@Override
	public boolean pingConnection(PearsonScormServicesSettings settings)
	{
		DeleteCartridgeRequestParameters dcrp = new DeleteCartridgeRequestParameters();
		dcrp.setResourceId("test");
		dcrp.setResourceVersion(1);

		HttpResponse response = null;
		try
		{
			Request req = createRequest(dcrp, IcodeonConstants.CONTENT_TYPE_JSON, settings.getBaseUrl());
			req.setBaseUrl(settings.getBaseUrl());
			DeleteCartridgeService dcs = new DeleteCartridgeService(req, createConsumer(settings));
			response = dcs.execute();

			return 202 == response.getStatusLine().getStatusCode();
		}
		catch( IcodeonException ie )
		{
			return false;
		}
	}
}
