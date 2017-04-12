package com.tle.core.pss.servlet;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pearson.pdn.icodeon.model.IcodeonConstants;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.system.PearsonScormServicesSettings;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.pss.entity.PssCallbackLog;
import com.tle.core.pss.notification.PearsonScormServicesOperationFactory;
import com.tle.core.pss.service.PearsonScormServicesCallbackService;
import com.tle.core.pss.service.PearsonScormServicesService;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.scorm.ScormUtils;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PearsonScormServicesCallbackServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(PearsonScormServicesCallbackServlet.class);

	private static final String PARAM_PAYLOAD = "PAYLOAD";
	private static final String PARAM_PAYLOAD_TYPE = "PAYLOAD-CONTENT-TYPE";
	private static final String ACTION_POST = "POST";
	private static final String ACTION_DELETE = "DELETE";

	@Inject
	private PearsonScormServicesCallbackService pssCallbackService;
	@Inject
	private ItemService itemService;
	@Inject
	private PearsonScormServicesService pssService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private PearsonScormServicesOperationFactory pssOpFactory;
	@Inject
	private WorkflowFactory workflowFactory;

	private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		logDebugRequest(req);

		if( req.getParameter(PARAM_PAYLOAD_TYPE).contains(IcodeonConstants.CONTENT_TYPE_JSON) )
		{
			Payload payload = gson.fromJson(req.getParameter(PARAM_PAYLOAD), Payload.class);
			int tn = payload.getTrackingNumber();

			ItemId itemId = getItemIdFromPayload(payload);
			PssCallbackLog logEntry = pssCallbackService.getCallbackLogEntryWithFallback(tn, itemId);

			switch( payload.getActionType() )
			{
				case ACTION_POST:
					if( payload.isStatus() )
					{
						// Successfully added
						removeLogEntry(tn);
					}
					else
					{
						LOGGER.debug("Editing PSS Callback log entry: " + tn);
						if( logEntry != null )
						{
							logEntry.setTrackingNumber(tn);
							logEntry.setMessage(payload.getMessage());
							logEntry.setStatus(Integer.toString(resp.getStatus()));
							logEntry.setLastAttempt(new Date());
							logEntry.setAttemptNumber(logEntry.getAttemptNumber() + 1);
							pssCallbackService.editCallbackLogEntry(logEntry);
							Item item = itemService.getUnsecureIfExists(itemId);
							if( item != null )
							{
								sendNotification(item);
							}
						}
					}
					break;
				case ACTION_DELETE:
					if( logEntry != null )
					{
						/*
						 * Delete performed by EQUELLA and we don't care about
						 * failures
						 */
						removeLogEntry(tn);
					}
					else
					{
						/*
						 * Delete performed from PSS Control panel or Item
						 * deleted from EQUELLA
						 */
						Item item = itemService.getUnsecureIfExists(itemId);

						if( item != null )
						{
							// Get attachment if it exists
							CustomAttachment scormAtt = new UnmodifiableAttachments(item)
								.getFirstCustomOfType(ScormUtils.ATTACHMENT_TYPE);
							if( scormAtt != null )
							{
								ItemStatus itemStatus = item.getStatus();
								if( !ItemStatus.DELETED.equals(itemStatus) )
								{
									if( pssService.isEnabled() )
									{
										runAs.executeAsSystem(item.getInstitution(), new Runnable()
										{
											@Override
											public void run()
											{
												pssService.addScormPackage(item, scormAtt);
											}
										});
									}
								}
							}
							else
							{
								// Probably never had a SCORM attachment
								removeLogEntry(tn);
							}
						}
						else
						{
							removeLogEntry(tn);
						}
						break;
					}
			}
		}
	}

	private ItemId getItemIdFromPayload(Payload payload)
	{
		return new ItemId(stripNamespace(payload.getResourceId()), Integer.parseInt(payload.getResourceVersion()));
	}

	private void sendNotification(Item item)
	{
		ItemId itemId = item.getItemId();
		itemService.operation(itemId, pssOpFactory.notifyContributor(), workflowFactory.reIndexIfRequired());
	}

	private void removeLogEntry(int trackingNo)
	{
		LOGGER.debug("Removing PSS Callback log entry: " + trackingNo);
		pssCallbackService.deleteCallbackEntry(trackingNo);
	}

	public static class Payload
	{
		private int trackingNumber;
		private boolean status;
		private String message;
		private String actionType;
		private String resourceId;
		private String resourceVersion;

		public int getTrackingNumber()
		{
			return trackingNumber;
		}

		public void setTrackingNumber(int trackingNumber)
		{
			this.trackingNumber = trackingNumber;
		}

		public boolean isStatus()
		{
			return status;
		}

		public void setStatus(boolean status)
		{
			this.status = status;
		}

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}

		public String getActionType()
		{
			return actionType;
		}

		public void setActionType(String actionType)
		{
			this.actionType = actionType;
		}

		public String getResourceId()
		{
			return resourceId;
		}

		public void setResourceId(String resourceId)
		{
			this.resourceId = resourceId;
		}

		public String getResourceVersion()
		{
			return resourceVersion;
		}

		public void setResourceVersion(String resourceVersion)
		{
			this.resourceVersion = resourceVersion;
		}
	}

	private String stripNamespace(String resourceId)
	{
		PearsonScormServicesSettings pssSettings = pssService.getPearsonScormServicesSettings();
		String nsp = pssSettings.getAccountNamespace();
		return resourceId.startsWith(nsp) ? resourceId.substring(nsp.length() + 1) : resourceId;
	}

	private void logDebugRequest(HttpServletRequest req)
	{
		if( LOGGER.isDebugEnabled() )
		{
			for( String key : req.getParameterMap().keySet() )
			{
				LOGGER.debug(key + ": " + req.getParameter(key));
			}
		}
	}
}
