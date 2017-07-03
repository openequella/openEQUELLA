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

package com.tle.web.entities.section;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.Check;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.i18n.BundleCache;
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
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.Js;
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
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * Make sure your section tree contains a TreeIndexed subclass of
 * AbstractEntityContributeSection if you wish to add or edit entities.
 * 
 * @author Aaron
 * @param <E> The entity type. E.g. Connector
 * @param <M> Your section model. Must extend
 *            AbstractShowEntitiesSection.AbstractShowEntitiesModel
 */
@SuppressWarnings("nls")
public abstract class AbstractShowEntitiesSection<E extends BaseEntity, M extends AbstractShowEntitiesSection.AbstractShowEntitiesModel>
	extends
		AbstractPrototypeSection<M>
	implements HtmlRenderer
{
	public static final String ENABLED = "enabled";
	public static final String DISABLED = "disabled";
	public static final String PENDING = "pending";

	@PlugKey("link.edit")
	private static Label LABEL_LINK_EDIT;
	@PlugKey("link.delete")
	private static Label LABEL_LINK_DELETE;
	@PlugKey("link.clone")
	private static Label LABEL_LINK_CLONE;

	@PlugKey("link.disable")
	private static Label LABEL_LINK_DISABLE;
	@PlugKey("link.enable")
	private static Label LABEL_LINK_ENABLE;

	@TreeLookup(mandatory = false)
	private AbstractEntityContributeSection<?, E, ?> contribSection;

	@Inject
	private BundleCache bundleCache;

	@Component(name = "add")
	private Link addLink;
	@Component(name = "ent")
	private SelectionsTable entTable;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	protected AjaxGenerator ajax;

	protected boolean noArea;
	protected JSCallable toggleEnabledFunction;

	private JSCallable cloneFunction;
	private JSCallable editFunction;
	private JSCallable deleteFunction;

	protected abstract AbstractEntityService<?, E> getEntityService();

	protected abstract Label getTitleLabel(SectionInfo info);

	protected abstract Label getAddLabel();

	protected abstract Label getEntityColumnLabel();

	protected abstract Label getEmptyListLabel();

	protected abstract Label getDeleteConfirmLabel(SectionInfo info, E entity);

	protected abstract boolean canAdd(SectionInfo info);

	/**
	 * If you return true, you must override getInUseLabel(SectionInfo, E)
	 * 
	 * @param info
	 * @param entity
	 * @return
	 */
	protected abstract boolean isInUse(SectionInfo info, E entity);

	/**
	 * Stuff rendered above the table (if any)
	 * 
	 * @param context
	 * @return
	 */
	protected SectionRenderable renderTop(RenderEventContext context)
	{
		return null;
	}

	/**
	 * Stuff rendered below the table (if any)
	 * 
	 * @param context
	 * @return
	 */
	protected SectionRenderable renderBottom(RenderEventContext context)
	{
		return null;
	}

	protected boolean isDisplayed(SectionInfo info)
	{
		return true;
	}

	public String getAjaxId()
	{
		return "entities";
	}

	protected Label getEditLabel(SectionInfo info, E entity)
	{
		return LABEL_LINK_EDIT;
	}

	protected Label getDeleteLabel(SectionInfo info, E entity)
	{
		return LABEL_LINK_DELETE;
	}

	/**
	 * @param info
	 * @return
	 */
	protected Label getCloneLabel(SectionInfo info, E entity)
	{
		return LABEL_LINK_CLONE;
	}

	protected Label getInUseLabel(SectionInfo info, E entity)
	{
		return null;
	}

	public Label getDisableLink()
	{
		return LABEL_LINK_DISABLE;
	}

	public Label getEnableLink()
	{
		return LABEL_LINK_ENABLE;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final M model = getModel(context);
		final boolean displayed = isDisplayed(context);
		model.setDisplayed(displayed);
		if( displayed )
		{
			final SelectionsTableState tableState = entTable.getState(context);

			final TableHeaderRow header = tableState.addHeaderRow(getColumnHeadings().toArray());
			addDynamicColumnHeadings(context, header);
			header.addCell(null);

			model.setTop(renderTop(context));
			model.setBottom(renderBottom(context));

			model.setPageTitle(getTitleLabel(context));

			// don't show Add if not allowed
			addLink.setDisplayed(context, canAdd(context));
		}

		return view.createResult("entities.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		addLink.setLabel(getAddLabel());
		addLink.setClickHandler(getNewEntityHandler());

		deleteFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteEntity"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), getAjaxId());
		editFunction = events.getSubmitValuesFunction("editEntity");
		cloneFunction = events.getSubmitValuesFunction("cloneEntity");
		toggleEnabledFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("toggleEnabled"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), getAjaxId());

		entTable.setColumnSorts(Sort.PRIMARY_ASC);
		entTable.setSelectionsModel(new EntitiesListModel());
		entTable.setNothingSelectedText(getEmptyListLabel());
		entTable.setAddAction(addLink);
	}

	@EventHandlerMethod
	public void toggleEnabled(SectionInfo info, String uuid)
	{
		getEntityService().toggleEnabled(uuid);
	}

	protected JSHandler getNewEntityHandler()
	{
		return events.getNamedHandler("newEntity");
	}

	protected List<Object> getColumnHeadings()
	{
		return Collections.unmodifiableList(Collections.singletonList((Object) getEntityColumnLabel()));
	}

	protected void addDynamicColumnHeadings(SectionInfo info, TableHeaderRow header)
	{
		// None
	}

	protected void addDynamicColumnData(SectionInfo info, E ent, SelectionsTableSelection row)
	{
		// None
	}

	@EventHandlerMethod
	public final void editEntity(SectionInfo info, String uuid)
	{
		editExisting(info, uuid);
	}

	protected void editExisting(SectionInfo info, String uuid)
	{
		contribSection.startEdit(info, uuid, false);
	}

	@EventHandlerMethod
	public final void newEntity(SectionInfo info)
	{
		createNew(info);
	}

	protected void createNew(SectionInfo info)
	{
		contribSection.createNew(info);
	}

	@EventHandlerMethod
	public final void deleteEntity(SectionInfo info, String uuid)
	{
		doDeleteEntity(info, uuid);

	}

	protected void doDeleteEntity(SectionInfo info, String uuid)
	{
		AbstractEntityService<?, E> entityService = getEntityService();
		E ent = entityService.getByUuid(uuid);
		// Hmm, we should really have some sort of "do you want to unlock"
		// confirmation
		entityService.cancelEdit(ent.getId(), true);
		entityService.delete(ent, true);
	}

	protected String getRowClass(SectionInfo info, E ent)
	{
		if( ent.isDisabled() )
		{
			return DISABLED;
		}
		return ENABLED;
	}

	/**
	 * Assumed yes
	 * 
	 * @param info
	 * @param ent
	 * @return
	 */
	protected boolean canDisable(SectionInfo info, E ent)
	{
		return true;
	}

	/**
	 * Assumed yes (because you can get to the page) unless you want to disallow
	 * editing.
	 * 
	 * @param info
	 * @param ent
	 * @return
	 */
	protected boolean canEdit(SectionInfo info, E ent)
	{
		return true;
	}

	protected boolean canDelete(SectionInfo info, E ent)
	{
		return ent.isDisabled() && getEntityService().canDelete(ent);
	}

	// FIXME: parameter inconsistency. Should all use id probably
	@EventHandlerMethod
	public final void cloneEntity(SectionInfo info, String uuid)
	{
		contribSection.startEdit(info, uuid, true);
	}

	/**
	 * Override and return false if you don't support clone
	 * 
	 * @param ent
	 * @return
	 */
	protected boolean canClone(SectionInfo info, E ent)
	{
		AbstractEntityService<?, E> entityService = getEntityService();
		return entityService.canCreate() && entityService.canEdit(ent);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AbstractShowEntitiesModel();
	}

	protected AbstractEntityContributeSection<?, E, ?> getContribSection(SectionInfo info)
	{
		return contribSection;
	}

	public SelectionsTable getEntTable()
	{
		return entTable;
	}

	protected List<E> getEntityList(SectionInfo info)
	{
		return getEntityService().enumerateEditable();
	}

	public boolean isNoArea()
	{
		return noArea;
	}

	protected SectionRenderable createViewLink(SectionInfo info, E ent)
	{
		return new LabelRenderer(new BundleLabel(ent.getName(), bundleCache));
	}

	protected SectionRenderable createToggleEnabledLink(SectionInfo info, E ent)
	{
		final HtmlLinkState actionLink = new HtmlLinkState(new OverrideHandler(toggleEnabledFunction, ent.getUuid()));
		final LinkRenderer link = new LinkRenderer(actionLink);
		link.setLabel(ent.isDisabled() ? LABEL_LINK_ENABLE : LABEL_LINK_DISABLE);
		return link;
	}

	protected SectionRenderable createEditLink(SectionInfo info, E ent)
	{
		final LinkRenderer link = new LinkRenderer(new HtmlLinkState(new OverrideHandler(editFunction, ent.getUuid())));
		link.setLabel(getEditLabel(info, ent));
		return link;
	}

	protected SectionRenderable createCloneLink(SectionInfo info, E ent)
	{
		final LinkRenderer link = new LinkRenderer(
			new HtmlLinkState(new OverrideHandler(cloneFunction, ent.getUuid())));
		link.setLabel(getCloneLabel(info, ent));
		return link;
	}

	protected SectionRenderable createDeleteLink(SectionInfo info, E ent)
	{
		final LinkRenderer link;
		if( isInUse(info, ent) )
		{
			link = new LinkRenderer(
				new HtmlLinkState(new OverrideHandler(Js.alert_s(getInUseLabel(info, ent).getText()))));
			link.setLabel(getDeleteLabel(info, ent));
		}
		else
		{
			link = new LinkRenderer(new HtmlLinkState(
				new OverrideHandler(deleteFunction, ent.getUuid()).addValidator(getDeleteValidator(info, ent))));
			link.setLabel(getDeleteLabel(info, ent));
		}
		return link;
	}

	/**
	 * By default, a validator on delete is a simple confirm dialog, drawing on
	 * a customisable message. Override where a more elaborate validator is
	 * called for.
	 * 
	 * @param info
	 * @param ent
	 * @return
	 */
	protected JSValidator getDeleteValidator(SectionInfo info, E ent)
	{
		return new Confirm(getDeleteConfirmLabel(info, ent));
	}

	protected void addCustomActions(SectionInfo info, SelectionsTableSelection selection, E ent,
		List<SectionRenderable> actions, int index)
	{
		// Nothing by default
	}

	private class EntitiesListModel extends DynamicSelectionsTableModel<E>
	{
		@Override
		protected List<E> getSourceList(SectionInfo info)
		{
			return getEntityList(info);
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, E ent,
			List<SectionRenderable> actions, int index)
		{
			addDynamicColumnData(info, ent, selection);

			selection.setViewAction(createViewLink(info, ent));

			addCustomActions(info, selection, ent, actions, index);

			if( canDisable(info, ent) )
			{
				actions.add(createToggleEnabledLink(info, ent));
			}
			if( canEdit(info, ent) )
			{
				actions.add(createEditLink(info, ent));
			}
			if( canDelete(info, ent) )
			{
				actions.add(createDeleteLink(info, ent));
			}
			if( canClone(info, ent) )
			{
				actions.add(createCloneLink(info, ent));
			}

			String rowClass = getRowClass(info, ent);
			if( !Check.isEmpty(rowClass) )
			{
				selection.addClass(rowClass);
			}
		}
	}

	public static class AbstractShowEntitiesModel extends OneColumnLayout.OneColumnLayoutModel
	{
		private Label pageTitle;
		private boolean displayed;
		private SectionRenderable top;
		private SectionRenderable bottom;

		public SectionRenderable getTop()
		{
			return top;
		}

		public void setTop(SectionRenderable top)
		{
			this.top = top;
		}

		public SectionRenderable getBottom()
		{
			return bottom;
		}

		public void setBottom(SectionRenderable bottom)
		{
			this.bottom = bottom;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public boolean isDisplayed()
		{
			return displayed;
		}

		public void setDisplayed(boolean displayed)
		{
			this.displayed = displayed;
		}
	}
}
