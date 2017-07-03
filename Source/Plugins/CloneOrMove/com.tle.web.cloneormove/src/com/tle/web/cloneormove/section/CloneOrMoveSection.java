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

package com.tle.web.cloneormove.section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.cloneormove.model.CloneOrMoveModel;
import com.tle.web.cloneormove.model.ContributableCollectionsModel;
import com.tle.web.cloneormove.model.SchemaTransformsModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.section.ItemAdminResultsDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlListModel;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class CloneOrMoveSection extends AbstractPrototypeSection<CloneOrMoveModel> implements HtmlRenderer
{
	public static final String CLONE_ITEM = "CLONE_ITEM"; //$NON-NLS-1$
	public static final String MOVE_ITEM = "MOVE_ITEM"; //$NON-NLS-1$

	@PlugKey("selectcollection.button.clone")
	private static Label CLONE_LABEL;
	@PlugKey("selectcollection.button.move")
	private static Label MOVE_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	private JSCallable okFunction; // wired into the proceed button
	@Component
	private Button proceedButton;
	@Component(name = "c")
	private SingleSelectionList<ItemDefinition> collections;
	@Component(name = "s")
	private SingleSelectionList<String> schemaImports;
	@Component(name = "o")
	private SingleSelectionList<NameValue> cloneOptions;
	@Component(name = "so")
	private SingleSelectionList<Void> submitOptions;

	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;
	@Inject
	private ItemDefinitionService itemDefService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private BundleCache bundleCache;

	private boolean forBulk;

	@Nullable
	@TreeLookup(mandatory = false)
	private ItemAdminResultsDialog dialog;

	@Override
	public SectionResult renderHtml(RenderEventContext info)
	{
		CloneOrMoveModel model = getModel(info);

		boolean showSchemas = (schemaImports.getListModel().getOptions(info).size() > 1);
		schemaImports.setDisabled(info, !showSchemas);

		if( forBulk && !model.isHideClone() )
		{
			ItemDefinition col = collections.getSelectedValue(info);
			if( col != null )
			{
				if( col.getWorkflow() != null )
				{
					model.setSubmitLabel("selectcollection.label.submitworkflow"); //$NON-NLS-1$
				}
				else
				{
					model.setSubmitLabel("selectcollection.label.submit"); //$NON-NLS-1$
				}
			}
		}

		HtmlListModel<NameValue> cloneListModel = cloneOptions.getListModel();
		List<Option<NameValue>> cloneOpts = cloneListModel.getOptions(info);

		boolean showCloneOpts = (cloneOpts.size() > 1);
		model.setShowCloneOptions(showCloneOpts);
		cloneOptions.setDisplayed(info, showCloneOpts);

		if( Check.isEmpty(collections.getSelectedValueAsString(info)) )
		{
			proceedButton.setDisabled(info, true);
		}

		JSExpression schemaImportExpression = (showSchemas ? schemaImports.createGetExpression()
			: new StringExpression("")); //$NON-NLS-1$
		Object cloneParam = showCloneOpts ? cloneOptions.createGetExpression()
			: cloneOptions.getSelectedValueAsString(info);
		Object submitParam = model.getSubmitLabel() != null ? submitOptions.createGetExpression() : ""; //$NON-NLS-1$
		if( !forBulk )
		{
			proceedButton.setClickHandler(info, new OverrideHandler(okFunction, cloneParam,
				collections.createGetExpression(), schemaImportExpression, submitParam));
		}
		if( model.isMove() )
		{
			proceedButton.setLabel(info, MOVE_LABEL);
		}
		else
		{
			proceedButton.setLabel(info, CLONE_LABEL);
		}

		return viewFactory.createResult("clonemovebody.ftl", info); //$NON-NLS-1$
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		collections.setListModel(new ContributableCollectionsModel(itemDefService, bundleCache));
		schemaImports.setListModel(new SchemaTransformsModel(collections, schemaService));
		submitOptions.setListModel(new SubmitOptionsModel());
		submitOptions.setAlwaysSelect(true);
		cloneOptions.setAlwaysSelect(true);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		if( dialog != null )
		{
			collections.addChangeEventHandler(dialog.getFooterUpdate(tree, null, "collectionOptions"));
		}
		else
		{
			collections.addChangeEventHandler(ajax.getAjaxUpdateDomFunction(tree, null, null, "collectionOptions"));
		}
	}

	/**
	 * For JS side callbacks
	 * 
	 * @param okFunction
	 */
	public void setClientSideCallbacks(JSCallable proceedFunction)
	{
		okFunction = proceedFunction;
	}

	public void setCloneOptionsModel(HtmlListModel<NameValue> cloneOpts)
	{
		cloneOptions.setListModel(cloneOpts);
	}

	public void setAllowCollectionChange(SectionInfo info, boolean allow)
	{
		getModel(info).setAllowCollectionChange(allow);
	}

	/**
	 * Purely optional. Call this in your render method before rendering the
	 * CloneOrMoveSection
	 * 
	 * @param info
	 * @param source
	 * @param dest
	 */
	public void setSchemas(SectionInfo info, Schema source, Schema dest)
	{
		CloneOrMoveModel model = getModel(info);
		model.setSourceSchema(source);
		model.setDestSchema(dest);
	}

	public void setMove(SectionInfo info, boolean move)
	{
		getModel(info).setMove(move);
	}

	public void setHideClone(SectionInfo info, boolean hideClone)
	{
		getModel(info).setHideClone(hideClone);
	}

	public boolean isHideClone(SectionInfo info)
	{
		return getModel(info).isHideClone();
	}

	public void setHideCloneNoAttachments(SectionInfo info, boolean hideCloneNoAttachments)
	{
		getModel(info).setHideCloneNoAttachments(hideCloneNoAttachments);
	}

	public boolean isHideCloneNoAttachments(SectionInfo info)
	{
		return getModel(info).isHideCloneNoAttachments();
	}

	public void setHideMove(SectionInfo info, boolean hideMove)
	{
		getModel(info).setHideMove(hideMove);
	}

	public boolean isHideMove(SectionInfo info)
	{
		return getModel(info).isHideMove();
	}

	@Nullable
	public ItemDefinition getCurrentSelectedItemdef(SectionInfo info)
	{
		return collections.getSelectedValue(info);
	}

	public void setCurrentSelectedItemdef(SectionInfo info, String itemdefUuid)
	{
		collections.setSelectedStringValue(info, itemdefUuid);
	}

	public Button getProceedButton()
	{
		return proceedButton;
	}

	public SingleSelectionList<ItemDefinition> getCollections()
	{
		return collections;
	}

	public SingleSelectionList<String> getSchemaImports()
	{
		return schemaImports;
	}

	public SingleSelectionList<NameValue> getCloneOptions()
	{
		return cloneOptions;
	}

	@Override
	public Class<CloneOrMoveModel> getModelClass()
	{
		return CloneOrMoveModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "scll"; //$NON-NLS-1$
	}

	public class SubmitOptionsModel extends DynamicHtmlListModel<Void>
	{
		@Override
		protected Iterable<Option<Void>> populateOptions(SectionInfo info)
		{
			ArrayList<Option<Void>> options = new ArrayList<Option<Void>>();
			ItemDefinition col = collections.getSelectedValue(info);
			options.add(new VoidKeyOption("com.tle.web.wizard.command.save.savedraft", //$NON-NLS-1$
				"draft")); //$NON-NLS-1$
			if( col != null && col.getWorkflow() != null )
			{
				options.add(new VoidKeyOption("com.tle.web.wizard.command.save.submit", //$NON-NLS-1$
					"submit")); //$NON-NLS-1$
			}
			else
			{
				options.add(new VoidKeyOption("com.tle.web.wizard.command.save.submitnoworkflow", //$NON-NLS-1$
					"submit")); //$NON-NLS-1$
			}
			return options;
		}

		@Override
		protected Iterable<Void> populateModel(SectionInfo info)
		{
			return null;
		}
	}

	public SingleSelectionList<Void> getSubmitOptions()
	{
		return submitOptions;
	}

	public void setForBulk(boolean forBulk)
	{
		this.forBulk = forBulk;
	}

	public boolean isForBulk()
	{
		return forBulk;
	}

}
