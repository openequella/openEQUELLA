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

package com.tle.web.bulk.metadata.section;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.beans.entity.Schema;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.bulk.metadata.model.BulkEditMetadataModel;
import com.tle.web.bulk.metadata.model.Modification;
import com.tle.web.bulk.metadata.model.Modification.ModificationKeys;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@Bind
@SuppressWarnings("nls")
public class BulkEditMetadataSection extends AbstractPrototypeSection<BulkEditMetadataModel> implements HtmlRenderer
{
	private enum ActionTypes
	{
		NONE, REPLACE, SET, ADD
	}

	private final static String AJAX_DIV = "metadata-edit";
	private final static String BUTTON_FOOTER_DIV = "bss_bulkDialogfooter";

	@PlugKey("modifications.table.none")
	private static Label NO_MODS_LABEL;
	@PlugKey("schema.node.add")
	private static Label ADD_NODE_LABEL;
	@PlugKey("action.add")
	private static Label CHOOSE_ACTION_LABEL;
	@PlugKey("schema.select.default")
	private static String SCHEMA_DEFAULT;
	@PlugKey("action.actionlist.")
	private static String KEY_ACTIONS_PFX;
	@PlugKey("action.set.always")
	private static String SET_OPTION_ALWAYS;
	@PlugKey("action.set.exists")
	private static String SET_OPTION_EXISTS;
	@PlugKey("action.set.create")
	private static String SET_OPTION_CREATE;
	@PlugKey("modifications.table.header.nodes")
	private static Label HEADER_NODES;
	@PlugKey("modifications.table.header.info")
	private static Label HEADER_INFO;
	@PlugKey("modifications.table.header.actions")
	private static Label HEADER_ACTIONS;
	@PlugKey("modifications.table.action.moveup")
	private static Label MOVE_UP_LABEL;
	@PlugKey("modifications.table.action.movedown")
	private static Label MOVE_DOWN_LABEL;
	@PlugKey("modifications.table.action.delete")
	private static Label DELETE_MOD_LABEL;
	@PlugKey("modifications.table.action.edit")
	private static Label EDIT_MOD_LABEL;
	@PlugKey("schema.validation.blankterms")
	private static Label BLANK_TERMS_LABEL;
	@PlugKey("modifications.info.with")
	private static String WITH;
	@PlugKey("action.validation.blankfind")
	private static Label BLANK_FIND_LABEL;
	@PlugKey("action.validation.blankxml")
	private static Label BLANK_XML_LABEL;
	@PlugKey("modficattions.table.action.delete.confirm")
	private static Label ACTION_DELTE_CONFIRM;
	@PlugKey("schema.node.remove")
	private static Label REMOVE_NODE_LABEL;

	@PlugURL("images/up.gif")
	private static String URL_ICON_UP;
	@PlugURL("images/down.gif")
	private static String URL_ICON_DOWN;

	@EventFactory
	protected EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private SchemaService schemaService;

	@Component(name = "mdt")
	private SelectionsTable modsTable;
	@Component(name = "am")
	@PlugKey("modifications.button.add")
	private Button addModification;
	@Component(name = "scd")
	private SingleSelectionList<Schema> schemaList;
	@Component(name = "st")
	private Tree schemaTree;
	@Component(name = "pv")
	private TextField pathValues;
	@Component(name = "ca")
	private Button chooseActionButton;
	@Component(name = "al")
	private SingleSelectionList<ActionTypes> actionList;
	@Component(name = "fnd", stateful = false)
	private TextField findTextField;
	@Component(name = "rpc", stateful = false)
	private TextField replaceTextField;
	@Component(name = "xml", stateful = false)
	private TextField addXMLTextArea;
	@Component(name = "stf", stateful = false)
	private TextField setTextField;
	@Component(name = "to")
	private SingleSelectionList<NameValue> setTextOptions;
	@Component(name = "sa")
	@PlugKey("action.save")
	private Button saveActionButton;
	@Component(name = "mr")
	@PlugKey("schema.return.modification")
	private Button modificationReturnButton;
	@Component(name = "en")
	@PlugKey("action.changenodes")
	private Button editNodesButton;

	private SimpleFunction addNodeFunc;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ActionTypes selection = actionList.getSelectedValue(context);

		if( selection == null || selection == ActionTypes.NONE )
		{
			saveActionButton.disable(context);
		}
		else
		{
			setUpValidators(context, selection);
		}
		hideButtons(context);
		return viewFactory.createResult("bulkmetadata.ftl", context);
	}

	private void hideButtons(RenderEventContext context)
	{
		BulkEditMetadataModel model = getModel(context);
		modificationReturnButton.setDisplayed(context, model.isSchemaSelection());
		chooseActionButton.setDisplayed(context, model.isSchemaSelection());
		editNodesButton.setDisplayed(context, model.isActionSelection());
		saveActionButton.setDisplayed(context, model.isActionSelection());
		if( model.isSchemaSelection() )
		{
			chooseActionButton.setClickHandler(context,
				new StatementHandler(getAjaxUpate(getTree(), "chooseAction", false, AJAX_DIV, BUTTON_FOOTER_DIV))
					.addValidator(
						pathValues.createNotBlankValidator().setFailureStatements(Js.alert_s(BLANK_TERMS_LABEL))));
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		UpdateDomFunction move = getAjaxUpate(tree, "moveModification", false, AJAX_DIV);
		UpdateDomFunction delete = getAjaxUpate(tree, "deleteModification", false, AJAX_DIV, BUTTON_FOOTER_DIV);
		UpdateDomFunction edit = getAjaxUpate(tree, "editModification", false, AJAX_DIV, BUTTON_FOOTER_DIV);

		modsTable.setColumnHeadings(HEADER_NODES, HEADER_INFO, HEADER_ACTIONS);
		modsTable.setNothingSelectedText(NO_MODS_LABEL);
		modsTable.setSelectionsModel(new ModificationTableModel(move, delete, edit));
		modsTable.setAddAction(addModification);

		addModification
			.setClickHandler(new StatementHandler(getAjaxUpate(tree, "addMod", false, AJAX_DIV, BUTTON_FOOTER_DIV)));
		editNodesButton
			.setClickHandler(new StatementHandler(getAjaxUpate(tree, "addMod", false, AJAX_DIV, BUTTON_FOOTER_DIV)));
		editNodesButton.setComponentAttribute(ButtonType.class, ButtonType.EDIT);

		schemaList.setListModel(new SchemaListModel());
		StatementHandler treeUpdate = new StatementHandler(getAjaxUpate(tree, "schemaChange", true, AJAX_DIV));
		schemaList.setEventHandler(JSHandler.EVENT_CHANGE, treeUpdate);

		schemaTree.setModel(new SchemaTreeModel());

		ScriptVariable nodeVar = new ScriptVariable("node");
		addNodeFunc = new SimpleFunction("addNode",
			new FunctionCallStatement(getAjaxUpate(tree, "addNode", false, "selected-nodes"), nodeVar), nodeVar);

		chooseActionButton.setLabel(new IconLabel(Icon.COG, CHOOSE_ACTION_LABEL));

		StatementHandler actionUpdate = new StatementHandler(
			getAjaxUpate(tree, "actionChange", true, "action-form", BUTTON_FOOTER_DIV));
		actionList.setEventHandler(JSHandler.EVENT_CHANGE, actionUpdate);
		actionList.setListModel(new EnumListModel<ActionTypes>(KEY_ACTIONS_PFX, true, ActionTypes.values()));

		SimpleHtmlListModel<NameValue> setTextOptionsList = new SimpleHtmlListModel<NameValue>(
			new BundleNameValue(SET_OPTION_ALWAYS, "always"), new BundleNameValue(SET_OPTION_EXISTS, "exists"),
			new BundleNameValue(SET_OPTION_CREATE, "create"));
		setTextOptions.setListModel(setTextOptionsList);
		setTextOptions.setAlwaysSelect(true);

		modificationReturnButton
			.setClickHandler(new StatementHandler(getAjaxUpate(tree, "modReturn", false, AJAX_DIV, BUTTON_FOOTER_DIV)));
		modificationReturnButton.setComponentAttribute(ButtonType.class, ButtonType.PREV);

		pathValues.setEventHandler(JSHandler.EVENT_KEYUP,
			new StatementHandler(getAjaxUpate(tree, "addNode", false, "treePanel"), Constants.BLANK));

		saveActionButton.setComponentAttribute(ButtonType.class, ButtonType.SAVE);

	}

	private void setUpValidators(SectionInfo context, ActionTypes selection)
	{
		if( selection.equals(ActionTypes.SET) )
		{
			saveActionButton.setClickHandler(context,
				new StatementHandler(getAjaxUpate(getTree(), "saveAction", true, AJAX_DIV, BUTTON_FOOTER_DIV)));
		}
		else if( selection.equals(ActionTypes.REPLACE) )
		{
			saveActionButton.setClickHandler(context,
				new StatementHandler(getAjaxUpate(getTree(), "saveAction", true, AJAX_DIV, BUTTON_FOOTER_DIV))
					.addValidator(
						findTextField.createNotBlankValidator().setFailureStatements(Js.alert_s(BLANK_FIND_LABEL))));
		}
		else if( selection.equals(ActionTypes.ADD) )
		{
			saveActionButton.setClickHandler(context,
				new StatementHandler(getAjaxUpate(getTree(), "saveAction", true, AJAX_DIV, BUTTON_FOOTER_DIV))
					.addValidator(
						addXMLTextArea.createNotBlankValidator().setFailureStatements(Js.alert_s(BLANK_XML_LABEL))));
		}
	}

	private UpdateDomFunction getAjaxUpate(SectionTree tree, String eventHandlerName, boolean useLoad,
		String... ajaxIds)
	{
		JSCallable effectFunction = useLoad ? ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING)
			: ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);
		return ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler(eventHandlerName), effectFunction,
			ajaxIds);
	}

	@EventHandlerMethod
	public void modReturn(SectionInfo info)
	{
		BulkEditMetadataModel model = getModel(info);
		model.setSchemaSelection(false);
		model.wipe();
	}

	@EventHandlerMethod
	public void saveAction(SectionInfo info)
	{
		BulkEditMetadataModel model = getModel(info);
		model.setSchemaSelection(false);
		model.setActionSelection(false);
		StringBuilder modInfo = new StringBuilder();
		modInfo.append(actionList.getSelectedValueAsString(info) + ": ");
		Map<ModificationKeys, String> params = new HashMap<Modification.ModificationKeys, String>();
		if( actionList.getSelectedValue(info) == ActionTypes.ADD )
		{
			addQuotes(modInfo, addXMLTextArea.getValue(info));
			params.put(ModificationKeys.ADD_XML, addXMLTextArea.getValue(info));
		}
		else if( actionList.getSelectedValue(info) == ActionTypes.SET )
		{
			addQuotes(modInfo, setTextField.getValue(info));
			withConjunction(modInfo);
			addQuotes(modInfo, setTextOptions.getSelectedValueAsString(info));

			params.put(ModificationKeys.SET_TEXT, setTextField.getValue(info));
			params.put(ModificationKeys.SET_TEXT_OPTION, setTextOptions.getSelectedValueAsString(info));

		}
		else if( actionList.getSelectedValue(info) == ActionTypes.REPLACE )
		{
			addQuotes(modInfo, findTextField.getValue(info));
			withConjunction(modInfo);
			addQuotes(modInfo, replaceTextField.getValue(info));

			params.put(ModificationKeys.REPLACE_FIND, findTextField.getValue(info));
			params.put(ModificationKeys.REPLACE_WITH, replaceTextField.getValue(info));
		}
		params.put(ModificationKeys.ACTION, actionList.getSelectedValueAsString(info));
		if( model.isEdit() )
		{
			model.addModification(modInfo.toString(), params, model.getEditIndex());
		}
		else
		{
			model.addModification(modInfo.toString(), params);
		}
		model.wipe();

	}

	private void withConjunction(StringBuilder sb)
	{
		sb.append(' ').append(new BundleNameValue(WITH, null).getLabel()).append(' ');
	}

	private void addQuotes(StringBuilder sb, String text)
	{
		sb.append('\'').append(text).append('\'');
	}

	@EventHandlerMethod
	public void schemaChange(SectionInfo info)
	{
		BulkEditMetadataModel model = getModel(info);
		if( schemaList.getSelectedValue(info) == null )
		{
			model.setSelectedSchema(0);
		}
		else
		{
			model.setSelectedSchema(schemaList.getSelectedValue(info).getId());
		}
	}

	@EventHandlerMethod
	public void actionChange(SectionInfo info)
	{
		BulkEditMetadataModel model = getModel(info);
		if( actionList.getSelectedValue(info) == ActionTypes.ADD )
		{
			model.setActionAdd();
		}
		else if( actionList.getSelectedValue(info) == ActionTypes.REPLACE )
		{
			model.setActionReplace();
		}
		else if( actionList.getSelectedValue(info) == ActionTypes.SET )
		{
			model.setActionSet();
		}
	}

	@EventHandlerMethod
	public void addMod(SectionInfo info)
	{
		BulkEditMetadataModel model = getModel(info);
		model.setSchemaSelection(true);
		model.setActionSelection(false);
		if( Check.isEmpty(model.getNodeDisplay()) )
		{
			// no nodes selected, must be a new modification
			pathValues.setValue(info, Constants.BLANK);
			schemaList.setSelectedStringValue(info, "0");
			actionList.setSelectedValue(info, ActionTypes.NONE);
			if( !Check.isEmpty(model.getSelectedNodes()) )
			{
				model.getSelectedNodes().clear();
			}
		}
	}

	@EventHandlerMethod
	public void chooseAction(SectionInfo info)
	{
		BulkEditMetadataModel model = getModel(info);
		String selectedNodes = pathValues.getValue(info);
		List<String> nodePaths = Arrays.asList(selectedNodes.split("\\s*,\\s*"));
		model.setSelectedNodes(nodePaths);
		model.setNodeDisplay(selectedNodes);
		model.setSchemaSelection(false);
		model.setActionSelection(true);
		// FIXME: these components should be stateless and not need this dodgy
		// reset
		if( actionList.getSelectedValue(info) == null || actionList.getSelectedValue(info).equals(ActionTypes.NONE) )
		{
			findTextField.setValue(info, " ");
			replaceTextField.setValue(info, " ");
			setTextField.setValue(info, " ");
			addXMLTextArea.setValue(info, " ");
		}
	}

	public class SchemaListModel extends DynamicHtmlListModel<Schema>
	{

		@Override
		protected Option<Schema> convertToOption(SectionInfo info, Schema schema)
		{
			return new NameValueOption<Schema>(
				new BundleNameValue(schema.getName(), String.valueOf(schema.getId()), bundleCache), schema);
		}

		@Override
		protected Iterable<Schema> populateModel(SectionInfo info)
		{
			return schemaService.enumerate();
		}

		@Override
		protected Option<Schema> getTopOption()
		{
			return new NameValueOption<Schema>(new BundleNameValue(SCHEMA_DEFAULT, "0"), null);
		}
	}

	public class SchemaTreeModel implements HtmlTreeModel
	{
		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, String xpath)
		{
			final List<HtmlTreeNode> list = Lists.newArrayList();
			Long schemaId = getModel(info).getSelectedSchema();
			if( schemaId == 0 )
			{
				return Collections.emptyList();
			}
			Schema schema = schemaService.get(schemaId);

			if( xpath == null )
			{
				xpath = "";
			}

			final PropBagEx schemaXml = schema.getDefinitionNonThreadSafe();
			for( PropBagEx child : schemaXml.iterator(xpath + "/*") )
			{
				String name = child.getNodeName();
				if( isAttribute(child) )
				{
					name = "@" + name;
				}
				String fullpath = Check.isEmpty(xpath) ? name : MessageFormat.format("{0}/{1}", xpath, name);
				list.add(new SchemaTreeNode(name, fullpath, isLeaf(child), isSelected(info, fullpath)));
			}

			return list;
		}

		private boolean isAttribute(PropBagEx xml)
		{
			return xml.isNodeTrue("@attribute");
		}

		private boolean isLeaf(PropBagEx xml)
		{
			return !xml.nodeExists("*");
		}

		private boolean isSelected(SectionInfo info, String nodePath)
		{
			List<String> selectedNodes = getModel(info).getSelectedNodes();
			if( Check.isEmpty(selectedNodes) )
			{
				return false;
			}
			else
			{
				return selectedNodes.contains(nodePath);
			}
		}
	}

	public class SchemaTreeNode implements HtmlTreeNode
	{
		private String name; // Node name
		private String id; // Full xpath
		private boolean leaf; // No children
		private boolean selected;

		public SchemaTreeNode(String name, String id, boolean isLeaf, boolean selected)
		{
			this.name = name;
			this.id = id;
			this.leaf = isLeaf;
			this.selected = selected;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public SectionRenderable getRenderer()
		{
			DivRenderer nodeDiv;
			if( !selected )
			{
				HtmlComponentState selectLink = new HtmlComponentState(new OverrideHandler(addNodeFunc, id));
				selectLink.setLabel(ADD_NODE_LABEL);
				selectLink.addClass("add");
				nodeDiv = new DivRenderer(
					new CombinedRenderer(new LabelRenderer(getLabel()), new LinkRenderer(selectLink)));
			}
			else
			{
				HtmlComponentState deselectLink = new HtmlComponentState(
					new OverrideHandler(getAjaxUpate(getTree(), "removeNode", false, "selected-nodes"), id));
				deselectLink.setLabel(REMOVE_NODE_LABEL);
				deselectLink.addClass("unselect");
				nodeDiv = new DivRenderer(
					new CombinedRenderer(new LabelRenderer(getLabel()), new LinkRenderer(deselectLink)));

				nodeDiv.addClass("selected-node");

			}
			return nodeDiv;
		}

		@Override
		public Label getLabel()
		{
			return new TextLabel(name);
		}

		@Override
		public boolean isLeaf()
		{

			return leaf;
		}

	}

	@EventHandlerMethod
	public void removeNode(SectionInfo info, String path)
	{
		BulkEditMetadataModel model = getModel(info);
		model.setSelectedSchema(schemaList.getSelectedValue(info).getId());
		List<String> selectedNodes = model.getSelectedNodes();
		selectedNodes.remove(path);
		model.setSelectedNodes(selectedNodes);
		String display = Constants.BLANK;
		if( Check.isEmpty(selectedNodes) )
		{
			pathValues.setValue(info, Constants.BLANK);
		}
		else
		{
			StringBuffer sb = new StringBuffer();
			for( String entry : selectedNodes )
			{
				if( !Check.isEmpty(entry) )
				{
					sb.append(entry);
					sb.append(", ");
				}
			}
			if( sb.length() > 0 )
			{
				display += sb.toString();
			}
			pathValues.setValue(info, display.substring(0, display.length() - 2));
		}
	}

	@EventHandlerMethod
	public void addNode(SectionInfo info, String path)
	{
		BulkEditMetadataModel model = getModel(info);
		if( schemaList.getSelectedValue(info) != null )
		{
			model.setSelectedSchema(schemaList.getSelectedValue(info).getId());
		}
		if( !Check.isEmpty(path) ) // empty using the path values keyup
		{
			if( Check.isEmpty(pathValues.getValue(info)) )
			{
				pathValues.setValue(info, path);
			}
			else
			{
				List<String> nodePaths = Arrays.asList(pathValues.getValue(info).split("\\s*,\\s*"));
				if( nodePaths.contains(path) )
				{
					return;
				}
				pathValues.setValue(info, pathValues.getValue(info) + ", " + path);
			}
		}
		String selectedNodes = pathValues.getValue(info);
		List<String> nodePaths = Arrays.asList(selectedNodes.split("\\s*,\\s*"));
		model.setSelectedNodes(nodePaths);
	}

	private class ModificationTableModel extends DynamicSelectionsTableModel<Modification>
	{
		private final UpdateDomFunction moveFunction;
		private final UpdateDomFunction deleteFunction;
		private final UpdateDomFunction editFunction;

		public ModificationTableModel(UpdateDomFunction moveFunction, UpdateDomFunction deleteFunction,
			UpdateDomFunction editFunction)
		{
			this.moveFunction = moveFunction;
			this.deleteFunction = deleteFunction;
			this.editFunction = editFunction;
		}

		@Override
		protected Collection<Modification> getSourceList(SectionInfo info)
		{
			return getModel(info).getModifications();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, Modification mod,
			List<SectionRenderable> actions, int index)
		{
			selection.setName(new TextLabel(mod.getNodeDisplay()));
			List<TableCell> cells = selection.getCells();
			TableCell modInfo = new TableCell(new TextLabel(mod.getInfo()));
			cells.add(modInfo);

			actions.add(makeAction(EDIT_MOD_LABEL, new OverrideHandler(editFunction, index)));
			actions.add(makeAction(DELETE_MOD_LABEL,
				new OverrideHandler(deleteFunction, index).addValidator(new Confirm(ACTION_DELTE_CONFIRM))));

			final HtmlLinkState upLinkState = new HtmlLinkState(new OverrideHandler(moveFunction, index, true));
			final LinkRenderer upLink = new LinkRenderer(upLinkState);
			upLink.setNestedRenderable(new ImageRenderer(URL_ICON_UP, MOVE_UP_LABEL));

			final HtmlLinkState downLinkState = new HtmlLinkState(new OverrideHandler(moveFunction, index, false));
			final LinkRenderer downLink = new LinkRenderer(downLinkState);
			downLink.setNestedRenderable(new ImageRenderer(URL_ICON_DOWN, MOVE_DOWN_LABEL));

			actions.add(CombinedRenderer.combineMultipleResults(upLink, downLink));
		}

	}

	@EventHandlerMethod
	public void moveModification(SectionInfo info, int index, boolean up)
	{
		BulkEditMetadataModel model = getModel(info);

		final List<Modification> mods = model.getModifications();
		final Modification mod = mods.get(index);

		int i = index;
		if( up && index > 0 )
		{
			i--;
		}
		else if( !up && index < mods.size() - 1 )
		{
			i++;
		}
		mods.remove(index);
		mods.add(i, mod);

		model.setModifications(mods);

	}

	@EventHandlerMethod
	public void deleteModification(SectionInfo info, int index)
	{
		BulkEditMetadataModel model = getModel(info);
		List<Modification> mods = model.getModifications();
		mods.remove(index);
		model.setModifications(mods);
	}

	@EventHandlerMethod
	public void editModification(SectionInfo info, int index)
	{
		BulkEditMetadataModel model = getModel(info);
		final Modification mod = model.getModifications().get(index);
		model.setEdit(true);
		model.setEditIndex(index);
		model.setNodeDisplay(mod.getNodeDisplay());
		model.setSchemaSelection(true);
		model.setSelectedNodes(mod.getNodes());
		model.setSelectedSchema(0);
		final Map<ModificationKeys, String> actionOptions = mod.getParmas();
		String action = actionOptions.get(ModificationKeys.ACTION);
		actionList.setSelectedStringValue(info, action);
		pathValues.setValue(info, mod.getNodeDisplay());
		schemaList.setSelectedStringValue(info, "0");
		if( action.equalsIgnoreCase(ActionTypes.REPLACE.toString().toLowerCase()) )
		{
			replaceTextField.setValue(info, actionOptions.get(ModificationKeys.REPLACE_WITH));
			findTextField.setValue(info, actionOptions.get(ModificationKeys.REPLACE_FIND));
			model.setActionReplace();
		}
		else if( action.equalsIgnoreCase(ActionTypes.SET.toString().toLowerCase()) )
		{
			setTextField.setValue(info, actionOptions.get(ModificationKeys.SET_TEXT));
			setTextOptions.setSelectedStringValue(info, actionOptions.get(ModificationKeys.SET_TEXT_OPTION));
			model.setActionSet();
		}
		else if( action.equalsIgnoreCase(ActionTypes.ADD.toString().toLowerCase()) )
		{
			addXMLTextArea.setValue(info, actionOptions.get(ModificationKeys.ADD_XML));
			model.setActionAdd();
		}
	}

	@Override
	public Class<BulkEditMetadataModel> getModelClass()
	{

		return BulkEditMetadataModel.class;
	}

	public SelectionsTable getModsTable()
	{
		return modsTable;
	}

	public Button getAddModification()
	{
		return addModification;
	}

	public SingleSelectionList<Schema> getSchemaList()
	{
		return schemaList;
	}

	public Tree getSchemaTree()
	{
		return schemaTree;
	}

	public TextField getPathValues()
	{
		return pathValues;
	}

	public SimpleFunction getAddNodeFunc()
	{
		return addNodeFunc;
	}

	public Button getChooseActionButton()
	{
		return chooseActionButton;
	}

	public SingleSelectionList<ActionTypes> getActionList()
	{
		return actionList;
	}

	public TextField getFindTextField()
	{
		return findTextField;
	}

	public TextField getReplaceTextField()
	{
		return replaceTextField;
	}

	public TextField getAddXMLTextArea()
	{
		return addXMLTextArea;
	}

	public TextField getSetTextField()
	{
		return setTextField;
	}

	public SingleSelectionList<NameValue> getSetTextOptions()
	{
		return setTextOptions;
	}

	public Button getSaveActionButton()
	{
		return saveActionButton;
	}

	public Button getModificationReturnButton()
	{
		return modificationReturnButton;
	}

	public Button getEditNodesButton()
	{
		return editNodesButton;
	}
}
