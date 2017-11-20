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

package com.tle.web.portal.section.admin;

import javax.inject.Inject;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.portal.service.PortletService;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.itemlist.item.AbstractListEntry;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author aholland
 */
@Bind
public class PortletListItem extends AbstractListEntry
{
	static
	{
		PluginResourceHandler.init(PortletListItem.class);
	}

	@PlugKey("admin.list.operation.delete")
	private static Label labelDelete;
	@PlugKey("admin.list.value.no")
	private static Label labelNo;
	@PlugKey("admin.list.value.yes")
	private static Label labelYes;
	@PlugKey("admin.list.operation.edit")
	private static Label labelEdit;
	@PlugKey("admin.list.label.owner")
	private static Label labelOwner;
	@PlugKey("admin.list.label.institutionwide")
	private static Label labelInst;
	@PlugKey("admin.list.label.type")
	private static Label labelType;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private PortletService portletService;

	private Portlet portlet;
	private JSHandler editHandler;
	private JSHandler deleteHandler;
	private HtmlLinkState ownerLink;

	@Override
	public Label getDescription()
	{
		return new BundleLabel(portlet.getDescription(), bundleCache);
	}

	@Override
	public HtmlLinkState getTitle()
	{
		HtmlLinkState state = new HtmlLinkState();
		state.setLabel(new BundleLabel(portlet.getName(), portlet.getUuid(), bundleCache));
		state.setClickHandler(editHandler);
		return state;
	}

	@Override
	protected void setupMetadata(RenderContext context)
	{
		super.setupMetadata(context);
		addMetadata(new StdMetadataEntry(labelInst, new LabelRenderer(portlet.isInstitutional() ? labelYes : labelNo)));

		addMetadata(new StdMetadataEntry(labelOwner, new LinkRenderer(ownerLink)));

		addMetadata(new StdMetadataEntry(labelType, new LabelRenderer(new KeyLabel(portletService
			.mapAllAvailableTypes().get(portlet.getType()).getNameKey()))));

		HtmlLinkState edit = new HtmlLinkState(labelEdit, editHandler);
		addRatingAction(new ButtonRenderer(edit).showAs(ButtonType.EDIT));

		// TODO: There should probably be an OperationMetadataEntry
		HtmlLinkState delete = new HtmlLinkState(labelDelete, deleteHandler);
		addRatingAction(new ButtonRenderer(delete).showAs(ButtonType.DELETE));
	}

	public void setPortlet(Portlet portlet)
	{
		this.portlet = portlet;
	}

	public void setEditHandler(JSHandler editHandler)
	{
		this.editHandler = editHandler;
	}

	public void setDeleteHandler(JSHandler deleteHandler)
	{
		this.deleteHandler = deleteHandler;
	}

	public void setOwnerLabel(HtmlLinkState ownerLink)
	{
		this.ownerLink = ownerLink;
	}
}
