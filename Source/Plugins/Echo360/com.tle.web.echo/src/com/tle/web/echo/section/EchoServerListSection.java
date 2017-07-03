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

package com.tle.web.echo.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.echo.EchoConstants;
import com.tle.core.echo.service.EchoService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.security.TLEAclManager;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class EchoServerListSection extends AbstractPrototypeSection<EchoServerListSection.ListEchoServersSectionModel>
	implements
		HtmlRenderer
{
	@PlugKey("serverlist.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("serverlist.emptylist")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("serverlist.link.edit")
	private static Label LABEL_LINK_EDIT;
	@PlugKey("serverlist.link.delete")
	private static Label LABEL_LINK_DELETE;
	@PlugKey("serverlist.column.echoserver")
	private static Label LABEL_ECHO_SERVER;

	@PlugKey("serverlist.confirm.delete")
	private static Confirm CONFIRM_DELETE;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private EchoService echoService;
	@Inject
	private TLEAclManager aclService;

	@Component
	private Link addServerLink;
	@Component(name = "c")
	private SelectionsTable serversTable;

	@TreeLookup
	private EchoServerEditorSection addServerSection;

	private JSCallable editFunction;
	private JSCallable deleteFunction;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		addServerLink.setLabel(LABEL_LINK_ADD);
		addServerLink.setClickHandler(events.getNamedHandler("newEchoServer"));

		deleteFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteEchoServer"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "echoservers");

		editFunction = events.getSubmitValuesFunction("editEchoServer");

		serversTable.setColumnHeadings(LABEL_ECHO_SERVER, null);
		serversTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE);
		serversTable.setSelectionsModel(new EchoServerModel());
		serversTable.setNothingSelectedText(LABEL_EMPTY_LIST);
		serversTable.setAddAction(addServerLink);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final boolean canAdd = !aclService.filterNonGrantedPrivileges(EchoConstants.PRIV_CREATE_ECHO).isEmpty();
		addServerLink.setDisplayed(context, canAdd);

		return viewFactory.createNamedResult(OneColumnLayout.BODY, "echoservers.ftl", this);
	}

	@EventHandlerMethod
	public void editEchoServer(SectionInfo info, String uuid)
	{
		addServerSection.startEdit(info, uuid);
	}

	@EventHandlerMethod
	public void newEchoServer(SectionInfo info)
	{
		addServerSection.createNew(info);
	}

	@EventHandlerMethod
	public void deleteEchoServer(SectionInfo info, String uuid)
	{
		echoService.delete(echoService.getByUuid(uuid), false);
	}

	@Override
	public Class<ListEchoServersSectionModel> getModelClass()
	{
		return ListEchoServersSectionModel.class;
	}

	private class EchoServerModel extends DynamicSelectionsTableModel<BaseEntityLabel>
	{
		@Override
		protected List<BaseEntityLabel> getSourceList(SectionInfo info)
		{
			return echoService.listEditable();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, BaseEntityLabel echoServer,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new BundleLabel(echoServer.getBundleId(), bundleCache)));
			final String uuid = echoServer.getUuid();

			if( echoService.canEdit(echoServer) )
			{
				actions.add(makeAction(LABEL_LINK_EDIT, new OverrideHandler(editFunction, uuid), "edit"));
			}
			if( echoService.canDelete(echoServer) )
			{
				actions.add(makeAction(LABEL_LINK_DELETE,
					new OverrideHandler(deleteFunction, uuid).addValidator(CONFIRM_DELETE), "delete"));
			}
		}
	}

	public static class ListEchoServersSectionModel extends OneColumnLayout.OneColumnLayoutModel
	{
		// Here there be dragons
	}

	public Link getAddServerLink()
	{
		return addServerLink;
	}

	public SelectionsTable getServersTable()
	{
		return serversTable;
	}
}
