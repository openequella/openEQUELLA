package com.tle.web.activation;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ActivationResultsExtension implements IntegrationSessionExtension
{
	private static final String KEY_SELECTREQUEST = "ActivateRequest";

	@Inject
	private PluginTracker<ActivationResultsModifier> pluginTracker;

	@Override
	public void setupSession(SectionInfo info, SelectionSession session, SingleSignonForm form)
	{
		// Nah
	}

	@Override
	public void processResultForMultiple(SectionInfo info, SelectionSession session, ObjectNode link, IItem<?> item,
		SelectedResource resource)
	{
		processSingle(info, link, "", item, resource);
	}

	// FIXME: Note: due to Jackson ObjectNode not being a Map subclass, need to
	// copy
	// and paste this here. Long term solution is pass in an ObjectNode into
	// processSingle
	public void processSingle(SectionInfo info, ObjectNode link, String prefix, IItem<?> item, SelectedResource resource)
	{
		ActivateRequest request = resource.getAttribute(KEY_SELECTREQUEST);
		if( request != null )
		{
			link.put(prefix + "description", request.getDescription());
			link.put(prefix + "activationUuid", request.getUuid());

			for( ActivationResultsModifier mod : pluginTracker.getBeanList() )
			{
				mod.processSingle(info, link, prefix, (Item) item, resource);
			}
		}
	}

	@Override
	public void processResultForSingle(SectionInfo info, SelectionSession session, Map<String, String> params,
		String prefix, IItem<?> item, SelectedResource resource)
	{
		ActivateRequest request = resource.getAttribute(KEY_SELECTREQUEST);
		if( request != null )
		{
			params.put(prefix + "description", request.getDescription());
			params.put(prefix + "activationUuid", request.getUuid());
		}
	}

	public void addRequest(SelectedResource resource, ActivateRequest activateRequest)
	{
		resource.setAttribute(KEY_SELECTREQUEST, activateRequest);
	}
}
