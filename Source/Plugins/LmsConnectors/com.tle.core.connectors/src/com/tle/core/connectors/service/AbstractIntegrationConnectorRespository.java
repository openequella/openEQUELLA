package com.tle.core.connectors.service;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ViewableItemType;
import com.tle.web.integration.Integration.LmsLinkInfo;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.selection.SelectedResource;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;

/**
 * @author Aaron
 */
@NonNullByDefault
public abstract class AbstractIntegrationConnectorRespository implements ConnectorRepositoryImplementation
{
	@Inject
	private IntegrationService integService;
	@Inject
	private ViewableItemResolver viewableItemResolver;

	// NO! Shouldn't need this!
	@Inject
	private SectionsController sectionsController;

	protected LmsLinkInfo getLmsLink(IItem<?> item, SelectedResource resource)
	{
		return integService.getIntegrationServiceForId(getIntegrationId()).getLinkForResource(createViewItemInfo(),
			createViewableItem(item, resource.isLatest(), resource.getKey().getExtensionType()), resource,
			isRelativeUrls(), true);
	}

	protected ViewableItem<?> createViewableItem(IItem<?> item, boolean latest, String extensionType)
	{
		return viewableItemResolver.createIntegrationViewableItem(item, latest, getViewableItemType(), extensionType);
	}

	@SuppressWarnings("nls")
	protected SectionInfo createViewItemInfo()
	{
		// FIXME: shouldn't NEED an info. this is just DODGE-O-RAMA
		return sectionsController.createForward("/viewitem/viewitem.do");
	}

	@Override
	public boolean supportsEditDescription()
	{
		return true;
	}

	protected abstract ViewableItemType getViewableItemType();

	protected abstract String getIntegrationId();

	protected abstract boolean isRelativeUrls();
}
