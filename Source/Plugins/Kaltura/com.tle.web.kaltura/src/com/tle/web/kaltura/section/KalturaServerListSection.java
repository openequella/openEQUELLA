package com.tle.web.kaltura.section;

import static com.tle.core.kaltura.KalturaConstants.PRIV_CREATE_KALTURA;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.resources.ResourcesService;
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
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState.TableCell;

@SuppressWarnings("nls")
@Bind
public class KalturaServerListSection
	extends
		AbstractPrototypeSection<KalturaServerListSection.ListKalturaServersSectionModel> implements HtmlRenderer
{
	private static final CssInclude CSS = CssInclude.include(
		ResourcesService.getResourceHelper(KalturaServerListSection.class).url("css/kalturaservers.css")).make();

	@PlugKey("kaltura.page.title")
	private static Label LABEL_PAGE_TITLE;
	@PlugKey("kaltura.link.add")
	private static Label LABEL_LINK_ADD;
	@PlugKey("kaltura.emptylist")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("kaltura.confirm.delete")
	private static Label LABEL_DELETE_CONFIRM;
	@PlugKey("kaltura.link.edit")
	private static Label LABEL_LINK_EDIT;
	@PlugKey("kaltura.link.delete")
	private static Label LABEL_LINK_DELETE;
	@PlugKey("kaltura.link.enable")
	private static Label LABEL_LINK_ENABLE;
	@PlugKey("kaltura.link.disable")
	private static Label LABEL_LINK_DISABLE;
	@PlugKey("kaltura.column.kalturaserver")
	private static Label LABEL_KALTURA_SERVER;
	@PlugKey("kaltura.column.serverstatus")
	private static Label LABEL_STATUS;
	@PlugKey("kaltura.column.serveractions")
	private static Label LABEL_ACTIONS;
	@PlugKey("kaltura.status.enabled")
	private static Label LABEL_STATUS_ENABLED;
	@PlugKey("kaltura.status.disabled")
	private static Label LABEL_STATUS_DISABLED;

	@Inject
	private KalturaService kalturaService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private TLEAclManager aclService;

	@TreeLookup
	private KalturaServerEditorSection addServerSection;

	@Component
	private Link addServerLink;
	@Component(name = "c")
	private SelectionsTable serversTable;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	private JSCallable editFunction;
	private JSCallable deleteFunction;
	private JSCallable enableDisableFunction;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		addServerLink.setLabel(LABEL_LINK_ADD);
		addServerLink.setClickHandler(events.getNamedHandler("newKalturaServer"));

		deleteFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("deleteKalturaServer"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "kalturaservers");

		editFunction = events.getSubmitValuesFunction("editKalturaServer");

		enableDisableFunction = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("enableDisableKalturaServer"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE),
			"kalturaservers");

		serversTable.setColumnHeadings(LABEL_KALTURA_SERVER, LABEL_STATUS, LABEL_ACTIONS);
		serversTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.NONE);
		serversTable.setSelectionsModel(new KalturaServerModel());
		serversTable.setNothingSelectedText(LABEL_EMPTY_LIST);
		serversTable.setAddAction(addServerLink);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ListKalturaServersSectionModel model = getModel(context);
		model.setPageTitle(LABEL_PAGE_TITLE);

		// don't show Add if not allowed
		final boolean canAdd = !aclService.filterNonGrantedPrivileges(PRIV_CREATE_KALTURA).isEmpty();
		addServerLink.setDisplayed(context, canAdd);

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult(OneColumnLayout.BODY,
			new CombinedRenderer(viewFactory.createResult("kalturaservers.ftl", this), CSS));
		return templateResult;
	}

	@EventHandlerMethod
	public void editKalturaServer(SectionInfo info, String uuid)
	{
		addServerSection.startEdit(info, uuid);
	}

	@EventHandlerMethod
	public void newKalturaServer(SectionInfo info)
	{
		addServerSection.createNew(info);
	}

	@EventHandlerMethod
	public void deleteKalturaServer(SectionInfo info, String uuid)
	{
		kalturaService.delete(kalturaService.getByUuid(uuid), true);
	}

	@EventHandlerMethod
	public void enableDisableKalturaServer(SectionInfo info, boolean enable, String uuid)
	{
		kalturaService.enable(kalturaService.getByUuid(uuid), enable);
	}

	@Override
	public Class<ListKalturaServersSectionModel> getModelClass()
	{
		return ListKalturaServersSectionModel.class;
	}

	private class KalturaServerModel extends DynamicSelectionsTableModel<BaseEntityLabel>
	{
		@Override
		protected List<BaseEntityLabel> getSourceList(SectionInfo info)
		{
			return kalturaService.listEditable();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, BaseEntityLabel kalturaServer,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new BundleLabel(kalturaServer.getBundleId(), bundleCache)));
			final String uuid = kalturaServer.getUuid();
			KalturaServer ks = kalturaService.getByUuid(uuid);

			TableCell status = selection.addColumn(ks.isEnabled() ? LABEL_STATUS_ENABLED : LABEL_STATUS_DISABLED);
			status.addClass("middle status");

			if( kalturaService.canEdit(kalturaServer) )
			{
				boolean enabled = ks.isEnabled();
				actions.add(makeAction(enabled ? LABEL_LINK_DISABLE : LABEL_LINK_ENABLE, new OverrideHandler(
					enableDisableFunction, !enabled, uuid), enabled ? "disable" : "enable"));
				actions.add(makeAction(LABEL_LINK_EDIT, new OverrideHandler(editFunction, uuid), "edit"));
			}
			if( kalturaService.canDelete(kalturaServer) )
			{
				actions
					.add(makeAction(LABEL_LINK_DELETE,
						new OverrideHandler(deleteFunction, uuid).addValidator(new Confirm(LABEL_DELETE_CONFIRM)),
						"delete"));
			}
		}
	}

	public static class ListKalturaServersSectionModel extends OneColumnLayout.OneColumnLayoutModel
	{
		private Label pageTitle;

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}
	}

	public SelectionsTable getServersTable()
	{
		return serversTable;
	}
}
