package com.tle.web.payment.section.catalogue;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.RegionService;
import com.tle.core.payment.service.session.CatalogueEditingSession;
import com.tle.core.payment.service.session.CatalogueEditingSession.CatalogueEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.user.UserService;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.payment.section.catalogue.CatalogueEditorSection.CatalogueEditorModel;
import com.tle.web.recipientselector.ExpressionSelectorDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.sections.standard.renderers.list.CheckListRenderer;

@SuppressWarnings("nls")
@Bind
public class CatalogueEditorSection extends AbstractEntityEditor<CatalogueEditingBean, Catalogue, CatalogueEditorModel>
{
	private static final String KEY_NO_DYNAMIC_COLLECTION = "NONE";

	@PlugKey("catalogue.edit.label.enable")
	private static Label LABEL_ENABLE;
	@PlugKey("catalogue.edit.dynamiccollection.none")
	private static Label LABEL_NO_DYNAMIC_COLLECTION;
	@PlugKey("catalogue.edit.expressionselector.title")
	private static Label LABEL_MANAGE_EXPRESSION;
	@PlugKey("catalogue.edit.mandatory.user")
	private static Label LABEL_ERROR_MANDATORY;

	@Inject
	private CatalogueService catalogueService;
	@Inject
	private RegionService regionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private DynaCollectionService dynamicCollectionService;
	@Inject
	private UserService userService;
	@Inject
	private ComponentFactory componentFactory;

	@Component(name = "e", stateful = false)
	private Checkbox enabled;

	@Component(name = "rr")
	private Checkbox restrictToRegions;

	@Component(name = "rl", stateful = false)
	private RegionsList regionsList;
	@Component(name = "dc", stateful = false)
	private SingleSelectionList<BaseEntityLabel> dynamicCollections;
	@Inject
	private ExpressionSelectorDialog manageSelector;
	@Component
	private Div manageDiv;

	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<CatalogueEditingBean, Catalogue> session)
	{
		final CatalogueEditorModel model = getModel(context);
		String manageExpression = manageSelector.getExpression(context);

		if( manageExpression == null )
		{
			manageExpression = Recipient.OWNER.getPrefix();
			manageSelector.setExpression(context, manageExpression);
		}

		if( !Check.isEmpty(manageExpression) )
		{
			model.setManageExpressionPretty(new ExpressionFormatter(userService).convertToInfix(manageExpression));
		}

		model.setDisplayRegions(restrictToRegions.isChecked(context));

		return view.createResult("editcatalogue.ftl", this);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		regionsList.setListModel(new DynamicHtmlListModel<BaseEntityLabel>()
		{
			@Override
			protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
			{
				return regionService.listEnabled();
			}

			@Override
			protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel region)
			{
				return new NameValueOption<BaseEntityLabel>(new BundleNameValue(region.getBundleId(), Long
					.toString(region.getId()), bundleCache), region);
			}
		});
		dynamicCollections.setListModel(new DynamicHtmlListModel<BaseEntityLabel>()
		{
			@Override
			protected Option<BaseEntityLabel> getTopOption()
			{
				return new SimpleOption<BaseEntityLabel>(LABEL_NO_DYNAMIC_COLLECTION.getText(),
					KEY_NO_DYNAMIC_COLLECTION, null);
			}

			@Override
			protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
			{
				return dynamicCollectionService.listAll();
			}

			@Override
			protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel obj)
			{
				return new NameValueOption<BaseEntityLabel>(new BundleNameValue(obj.getBundleId(), Long.toString(obj
					.getId()), bundleCache), obj);
			}
		});

		manageSelector.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("manageExpression"), "manage"));
		componentFactory.registerComponent(getSectionId(), "ms", tree, manageSelector);
		manageSelector.setTitle(LABEL_MANAGE_EXPRESSION);

		restrictToRegions.setEventHandler(
			"change",
			new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("toggleRegions"),
				"regions")));
	}

	@EventHandlerMethod
	public void manageExpression(SectionInfo info, String selectorId, String expression)
	{
		final CatalogueEditingSession session = catalogueService.loadSession(getModel(info).getSessionId());
		final CatalogueEditingBean bean = session.getBean();
		validateManageExpression(expression, session);
		bean.setManageCatalogueExpression(expression);
	}

	@EventHandlerMethod
	public void toggleRegions(SectionInfo info)
	{
		// Nada?
	}

	private void validateManageExpression(String expression,
		EntityEditingSession<CatalogueEditingBean, Catalogue> session)
	{
		Map<String, Object> errors = session.getValidationErrors();
		session.setValid(true);
		errors.clear();

		if( Check.isEmpty(expression) || expression.trim().isEmpty() )
		{
			errors.put("manager", LABEL_ERROR_MANDATORY);
			session.setValid(false);
		}
	}

	@Override
	protected AbstractEntityService<CatalogueEditingBean, Catalogue> getEntityService()
	{
		return catalogueService;
	}

	@Override
	protected Catalogue createNewEntity(SectionInfo info)
	{
		return new Catalogue();
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<CatalogueEditingBean, Catalogue> session)
	{
		final CatalogueEditingBean catBean = session.getBean();

		final boolean filtered = catBean.isRegionFiltered();
		restrictToRegions.setChecked(info, filtered);
		if( filtered )
		{
			regionsList.setSelectedStringValues(info,
				Collections2.transform(catBean.getRegions(), new Function<Long, String>()
				{
					@Override
					public String apply(Long input)
					{
						return Long.toString(input);
					}
				}));
		}

		final String dynamic = catBean.getDynamicCollection() == null ? KEY_NO_DYNAMIC_COLLECTION : Long
			.toString(catBean.getDynamicCollection());
		dynamicCollections.setSelectedStringValue(info, dynamic);

		final String manageExpression = catBean.getManageCatalogueExpression();
		manageSelector.setExpression(info, manageExpression);

		enabled.setChecked(info, catBean.isEnabled());
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<CatalogueEditingBean, Catalogue> session,
		boolean validate)
	{
		final CatalogueEditingBean catBean = session.getBean();
		final List<Long> r = catBean.getRegions();
		r.clear();

		final boolean filtered = restrictToRegions.isChecked(info);
		catBean.setRegionFiltered(filtered);
		if( filtered )
		{
			r.addAll(Collections2.transform(regionsList.getSelectedValuesAsStrings(info), new Function<String, Long>()
			{
				@Override
				public Long apply(String input)
				{
					return Long.valueOf(input);
				}
			}));
		}
		final BaseEntityLabel dynamic = dynamicCollections.getSelectedValue(info);
		catBean.setDynamicCollection(dynamic == null ? null : dynamic.getId());
		catBean.setEnabled(enabled.isChecked(info));

		if( validate )
		{
			validateManageExpression(catBean.getManageCatalogueExpression(), session);
		}
		else
		{
			final String manageExpression = manageSelector.getExpression(info);
			catBean.setManageCatalogueExpression(manageExpression);
		}
	}

	public Label getEnabledLabel()
	{
		return LABEL_ENABLE;
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	public RegionsList getRegionsList()
	{
		return regionsList;
	}

	public SingleSelectionList<BaseEntityLabel> getDynamicCollections()
	{
		return dynamicCollections;
	}

	public ExpressionSelectorDialog getManageSelector()
	{
		return manageSelector;
	}

	public Div getManageDiv()
	{
		return manageDiv;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CatalogueEditorModel();
	}

	public Checkbox getRestrictToRegions()
	{
		return restrictToRegions;
	}

	public static class RegionsList extends MultiSelectionList<BaseEntityLabel>
	{
		public RegionsList()
		{
			super();
			setDefaultRenderer("checklist");
		}

		@Override
		public void rendererSelected(RenderContext info, SectionRenderable renderer)
		{
			CheckListRenderer clrenderer = (CheckListRenderer) renderer;
			clrenderer.setAsList(true);
		}
	}

	public class CatalogueEditorModel
		extends
			AbstractEntityEditor<CatalogueEditingBean, Catalogue, CatalogueEditorModel>.AbstractEntityEditorModel
	{
		private String manageExpressionPretty;
		private boolean displayRegions;

		public String getManageExpressionPretty()
		{
			return manageExpressionPretty;
		}

		public void setManageExpressionPretty(String manageExpressionPretty)
		{
			this.manageExpressionPretty = manageExpressionPretty;
		}

		public boolean isDisplayRegions()
		{
			return displayRegions;
		}

		public void setDisplayRegions(boolean displayRegions)
		{
			this.displayRegions = displayRegions;
		}
	}
}
