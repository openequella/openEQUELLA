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

package com.tle.web.bulk.section;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.operation.BulkOperationExtension.OperationInfo;
import com.tle.web.bulk.operation.BulkResult;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.equella.render.StylishDropDownRenderer;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Pager;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.OkayableDialog;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.DivRenderer;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

@TreeIndexed
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractBulkResultsDialog<T extends ItemKey>
	extends
		EquellaDialog<AbstractBulkResultsDialog.Model>
	implements OkayableDialog
{
	protected static final int PER_PAGE = 8;

	private static final ExternallyDefinedFunction POLLING_CALL = new ExternallyDefinedFunction("setupPolling",
		AbstractBulkSelectionSection.BULKINCLUDE);

	@PlugKey("opresults.title")
	private static Label LABEL_TITLE;
	@PlugKey("opresults.confirm")
	private static Label LABEL_CONFIRM;
	@PlugKey("opresults.hideconfirm")
	private static Label LABEL_HIDECONFIRM;
	@PlugKey("opresults.showing")
	protected static String KEY_SHOWING; // NOSONAR
	@PlugKey("preview.error")
	private static String PREVIEW_ERROR_KEY;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private TaskService taskService;

	@TreeLookup
	private AbstractBulkSelectionSection<T> selectionSection;

	@Component
	@PlugKey("opresults.prev")
	private Button prevButton;
	@Component
	@PlugKey("opresults.next")
	private Button nextButton;
	@Component
	@PlugKey("opresults.execute")
	private Button okButton;
	@Component
	@PlugKey("opresults.preview")
	private Button previewButton;

	@Component(contexts = BookmarkEvent.CONTEXT_SESSION)
	private SingleSelectionList<OperationInfo> operationList;
	@Component(contexts = BookmarkEvent.CONTEXT_SESSION)
	private Pager pager;
	@Component
	private Table selectionsTable;
	@Component
	private Table bulkResultsTable;
	@Component
	private Tree previewTree;

	private JSCallable updateCall;
	@Nullable
	private JSCallable cancelCallback;

	private Collection<Button> extraNavigation;

	protected abstract DynamicHtmlListModel<OperationInfo> getBulkOperationList(SectionTree tree, String parentId);

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
		setAlwaysShowFooter(true);

		operationList.setListModel(getBulkOperationList(tree, id));
		operationList.setAlwaysSelect(true);
		operationList.addChangeEventHandler(getFooterUpdate(tree, events.getEventHandler("changeOperation")));
		// friggen stylish select HAX
		operationList.setStyle("width: 550px");

		updateCall = ajaxEvents.getAjaxFunction("updateResults");

		okButton.setClickHandler(events.getNamedHandler("execute").addValidator(new Confirm(LABEL_CONFIRM)));
		okButton.setComponentAttribute(ButtonType.class, ButtonType.SAVE);

		nextButton.setClickHandler(events.getNamedHandler("nav", false));
		nextButton.setDisplayed(false);
		nextButton.setComponentAttribute(ButtonType.class, ButtonType.NEXT);

		prevButton.setDisplayed(false);
		prevButton.setClickHandler(events.getNamedHandler("nav", true));
		prevButton.setComponentAttribute(ButtonType.class, ButtonType.PREV);

		previewButton.setDisplayed(false);
		previewButton.setClickHandler(events.getNamedHandler("preview"));
		previewButton.setComponentAttribute(ButtonType.class, ButtonType.NEXT);

		pager.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());

		previewTree.setModel(new ItemXmlTreeModel());
	}

	@Override
	public boolean isTreeIndexed()
	{
		return true;
	}

	@Override
	protected JSHandler getTemplateCloseFunction()
	{
		return new OverrideHandler(cancelCallback);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("open");
	}

	@EventHandlerMethod
	public void open(SectionInfo info, boolean execute)
	{
		String page = getModel(info).getPage();

		SectionUtils.clearModel(info, this);
		getModel(info).setForExecute(execute);
		getModel(info).setPage(page);
		super.showDialog(info);
	}

	@EventHandlerMethod
	public void changeOperation(SectionInfo info)
	{
		OperationInfo opInfo = operationList.getSelectedValue(info);
		opInfo.getOp().prepareDefaultOptions(info, opInfo.getOpId());
	}

	@Nullable
	protected OperationInfo getOperationInfo(SectionInfo info)
	{
		return operationList.getSelectedValue(info);
	}

	protected SectionRenderable renderOptions(RenderContext context, OperationInfo opInfo)
	{
		prevButton.setDisplayed(context, opInfo.getOp().showPreviousButton(context, opInfo.getOpId()));
		return opInfo.getOp().renderOptions(context, opInfo.getOpId());
	}

	protected SectionRenderable renderConfirmation(RenderContext context)
	{
		final Model model = getModel(context);

		List<T> pageOfIds;
		int totalSelections;
		if( selectionSection.useBitSet() )
		{
			LongSet bitSet = selectionSection.getBitSet(context);
			totalSelections = (int) bitSet.size();
			List<Long> setupAndGetPage = pager.setupAndGetPage(context, bitSet, PER_PAGE, 8);
			pageOfIds = getItemIds(setupAndGetPage);
		}
		else
		{
			final Set<T> selections = selectionSection.getSelections(context);
			totalSelections = selections.size();
			pageOfIds = pager.setupAndGetPage(context, selections, PER_PAGE, 8);
		}

		model.setOpResultCountLabel(getOpResultCountLabel(totalSelections));
		model.setTotalSelections(totalSelections);
		final int offset = (pager.getCurrentPage(context) - 1) * PER_PAGE;
		model.setShowingLabel(new KeyLabel(KEY_SHOWING, offset + 1, offset + pageOfIds.size(), totalSelections));

		final List<SelectionRow> rows = getRows(pageOfIds);
		if( Check.isEmpty(rows) )
		{
			nextButton.disable(context);
			okButton.disable(context);
		}
		else
		{
			for( SelectionRow row : rows )
			{
				TableCell labelCell = new TableCell(row.getLabel());
				labelCell.addClass("label");

				TableCell unselCell = new TableCell(row.getDeselect());
				unselCell.addClass("unselect");

				selectionsTable.addRow(context, labelCell, unselCell);
			}
			model.setSelectionRows(rows);
		}
		return viewFactory.createResult("bulkconfirm.ftl", this);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		Model model = getModel(context);
		String taskId = model.getTaskId();
		SectionRenderable contents;
		final OperationInfo opInfo = getOperationInfo(context);
		final BulkOperationExtension op = opInfo == null ? null : opInfo.getOp();
		final String opId = opInfo == null ? null : opInfo.getOpId();

		if( taskId != null )
		{
			contents = manifestNextPrevForTask(context, model, taskId, op, opId);
		}
		else
		{
			if( model.isShowOptions() )
			{
				if( opInfo == null )
				{
					throw new SectionsRuntimeException("Can't show options with no operation selected");
				}
				contents = renderOptions(context, opInfo);
			}
			else if( model.isShowPreview() )
			{
				contents = renderPreview(context, opInfo);
			}
			else
			{
				contents = renderConfirmation(context);
			}

			if( !model.isForExecute() )
			{
				nextButton.hide(context);
				prevButton.hide(context);
				okButton.hide(context);
			}
			else
			{
				manifestNextPrevForOp(context, model, op, opId);
			}

			if( model.isErrored() )
			{
				nextButton.hide(context);
				prevButton.show(context);
				okButton.hide(context);
			}

			operationList.addClass(context, "bulkactions");
			// friggen stylish select HAX
			operationList.getState(context).setAttribute(StylishDropDownRenderer.KEY_MAX_HEIGHT, 300);

			if( op != null && op.hasExtraNavigation(context, opId) && model.isShowOptions() )
			{
				extraNavigation = op.getExtraNavigation(context, opId);
			}
			else if( !Check.isEmpty(extraNavigation) )
			{
				extraNavigation.clear();
			}
		}
		model.setDialogContents(contents);

		return viewFactory.createResult("bulkdialog.ftl", this);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		ArrayList<Button> footerActions = Lists.newArrayList(prevButton, okButton, nextButton, previewButton);

		if( !Check.isEmpty(extraNavigation) && !getModel(context).isErrored() )
		{
			footerActions.addAll(extraNavigation);
		}
		return footerActions;
	}

	protected abstract Label getOpResultCountLabel(int totalSelections);

	protected abstract List<SelectionRow> getRows(List<T> pageOfIds);

	@EventHandlerMethod
	public void nav(SectionInfo info, boolean prev)
	{
		Model model = getModel(info);
		if( model.isShowPreview() )
		{
			model.setShowOptions(true);
			model.setShowPreview(false);
		}
		else
		{
			model.setShowOptions(!prev);
		}
	}

	@EventHandlerMethod
	public void preview(SectionInfo info)
	{
		Model model = getModel(info);
		model.setShowOptions(false);
		model.setShowPreview(true);
	}

	@EventHandlerMethod
	public void execute(SectionInfo info)
	{
		final OperationInfo opInfo = operationList.getSelectedValue(info);

		if( opInfo == null )
		{
			throw new SectionsRuntimeException("Can't execute the operation without appropriate privileges");
		}

		final String opId = opInfo.getOpId();
		final BulkOperationExtension op = opInfo.getOp();
		if( op.areOptionsFinished(info, opId) && op.validateOptions(info, opId) )
		{
			final Model model = getModel(info);
			model.setTaskId(selectionSection.executeWithExecutor(info, op.getExecutor(info, opId)));
		}
	}

	private SectionRenderable renderPreview(RenderContext context, OperationInfo opInfo)
	{
		ItemPack changedItem = null;
		Model model = getModel(context);
		long itemId = model.getPreviewItemId();
		try
		{
			if( itemId == 0 )
			{
				if( !selectionSection.useBitSet() )
				{
					Set<T> selections = selectionSection.getSelections(context);
					List<T> items = new ArrayList<T>(selections);
					long firstItemId = Long.decode(items.get(0).getUuid());
					changedItem = opInfo.getOp().runPreview(context, opInfo.getOpId(), firstItemId);
					model.setPreviewItemId(firstItemId);
				}
				else
				{
					LongSet bitSet = selectionSection.getBitSet(context);
					LongSet items = bitSet.clone();
					changedItem = opInfo.getOp().runPreview(context, opInfo.getOpId(), items.get(0));
					model.setPreviewItemId(items.get(0));
				}
			}
			else
			{
				changedItem = opInfo.getOp().runPreview(context, opInfo.getOpId(), itemId);
			}

		}
		catch( Exception e )
		{
			model.setErrored(true);
			model.setPreviewErrorLabel(new KeyLabel(PREVIEW_ERROR_KEY, e.getMessage()));
		}
		if( changedItem != null )
		{
			model.setPreviewItemXml(changedItem.getXml());
		}
		return viewFactory.createResult("bulkpreview.ftl", this);
	}

	private SectionRenderable manifestNextPrevForTask(RenderContext context, Model model, String taskId,
		@Nullable BulkOperationExtension op, @Nullable String opId)
	{
		okButton.hide(context);
		prevButton.hide(context);
		nextButton.hide(context);
		previewButton.hide(context);
		if( !Check.isEmpty(extraNavigation) )
		{
			extraNavigation.clear();
		}
		taskService.waitForTaskStatus(taskId, 10000);
		if( op != null && opId != null )
		{
			model.setOperationTitle(op.getStatusTitleLabel(context, opId));
		}
		context.getForm().addReadyStatements(POLLING_CALL, updateCall, Jq.$(bulkResultsTable),
			LABEL_HIDECONFIRM.getText());

		return viewFactory.createResult("bulkresults.ftl", this);
	}

	private void manifestNextPrevForOp(RenderContext context, Model model, @Nullable BulkOperationExtension op,
		@Nullable String opId)
	{
		if( opId == null )
		{
			throw new SectionsRuntimeException("Can't show execute buttons without an operation selected");
		}
		nextButton.setDisplayed(context, !model.isShowOptions() && !(op == null || !op.hasExtraOptions(context, opId)));

		if( op == null )
		{
			throw new SectionsRuntimeException("Unexpected null value for BulkOperationExtension op");
		}
		else
		{
			if( op.hasPreview(context, opId) && !model.isShowPreview() )
			{
				previewButton.setDisplayed(context, op.areOptionsFinished(context, opId) && model.isShowOptions());
				okButton.hide(context);
			}
			else if( model.isShowPreview() && op.hasPreview(context, opId) )
			{
				nextButton.hide(context);
				previewButton.hide(context);
				okButton.show(context);
				prevButton.show(context);
			}
			else
			{
				okButton.setDisplayed(context, op.areOptionsFinished(context, opId));
				previewButton.hide(context);
			}
		}
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		if( cancelCallback == null )
		{
			throw new SectionsRuntimeException("Must set cancel callback");
		}
	}

	@AjaxMethod
	public BulkResultUpdate updateResults(SectionInfo info, int offset)
	{
		Model model = getModel(info);
		String taskId = model.getTaskId();
		List<BulkResult> taskLog = Collections.emptyList();
		if( taskId != null )
		{
			taskService.askTaskChanges(Collections.singleton(taskId));
			TaskStatus status = taskService.waitForTaskStatus(taskId, 1000L);
			if( status != null )
			{
				Pair<Integer, List<BulkResult>> logRes = status.getTaskLog(offset, Integer.MAX_VALUE);
				taskLog = logRes.getSecond();
				offset = logRes.getFirst();
			}
			return new BulkResultUpdate(offset, taskLog, status == null || status.isFinished());
		}
		return new BulkResultUpdate(offset, taskLog, true);
	}

	@Nullable
	protected List<T> getItemIds(List<Long> longs)
	{
		return null;
	}

	@NonNullByDefault(false)
	@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
	public static class Model extends DialogModel
	{
		@Bookmarked
		private String taskId;
		@Bookmarked
		private boolean showOptions;
		@Bookmarked
		private boolean forExecute;
		@Bookmarked
		private boolean showPreview;
		private SectionRenderable dialogContents;
		private int totalSelections;
		private List<SelectionRow> selectionRows;
		private Label showingLabel;
		private Label operationTitle;
		private Label opResultCountLabel;
		private boolean errored;
		private PropBagEx previewItemXml;
		@Bookmarked
		private long previewItemId;
		private Label previewErrorLabel;
		@Bookmarked
		private String page;

		public Label getPreviewErrorLabel()
		{
			return previewErrorLabel;
		}

		public void setPreviewErrorLabel(Label previewErrorLabel)
		{
			this.previewErrorLabel = previewErrorLabel;
		}

		public long getPreviewItemId()
		{
			return previewItemId;
		}

		public void setPreviewItemId(long previewItemId)
		{
			this.previewItemId = previewItemId;
		}

		public PropBagEx getPreviewItemXml()
		{
			return previewItemXml;
		}

		public void setPreviewItemXml(PropBagEx previewItemXml)
		{
			this.previewItemXml = previewItemXml;
		}

		public String getTaskId()
		{
			return taskId;
		}

		public void setTaskId(String taskId)
		{
			this.taskId = taskId;
		}

		public Label getOperationTitle()
		{
			return operationTitle;
		}

		public void setOperationTitle(Label operationTitle)
		{
			this.operationTitle = operationTitle;
		}

		public List<SelectionRow> getSelectionRows()
		{
			return selectionRows;
		}

		public void setSelectionRows(List<SelectionRow> selectionRows)
		{
			this.selectionRows = selectionRows;
		}

		public int getTotalSelections()
		{
			return totalSelections;
		}

		public void setTotalSelections(int totalSelections)
		{
			this.totalSelections = totalSelections;
		}

		public boolean isShowOptions()
		{
			return showOptions;
		}

		public void setShowOptions(boolean showOptions)
		{
			this.showOptions = showOptions;
		}

		public SectionRenderable getDialogContents()
		{
			return dialogContents;
		}

		public void setDialogContents(SectionRenderable dialogContents)
		{
			this.dialogContents = dialogContents;
		}

		public boolean isForExecute()
		{
			return forExecute;
		}

		public void setForExecute(boolean forExecute)
		{
			this.forExecute = forExecute;
		}

		public Label getShowingLabel()
		{
			return showingLabel;
		}

		public void setShowingLabel(Label showingLabel)
		{
			this.showingLabel = showingLabel;
		}

		public void setOpResultCountLabel(Label opResultCountLabel)
		{
			this.opResultCountLabel = opResultCountLabel;
		}

		public Label getOpResultCountLabel()
		{
			return opResultCountLabel;
		}

		public boolean isErrored()
		{
			return errored;
		}

		public void setErrored(boolean errored)
		{
			this.errored = errored;
		}

		public boolean isShowPreview()
		{
			return showPreview;
		}

		public void setShowPreview(boolean showPreview)
		{
			this.showPreview = showPreview;
		}

		public String getPage()
		{
			return page;
		}

		public void setPage(String page)
		{
			this.page = page;
		}
	}

	public static class SelectionRow
	{
		private final Label label;
		private final HtmlComponentState deselect;

		public SelectionRow(Label label, HtmlComponentState deselect)
		{
			this.label = label;
			this.deselect = deselect;
		}

		public Label getLabel()
		{
			return label;
		}

		public HtmlComponentState getDeselect()
		{
			return deselect;
		}
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	public static class BulkResultUpdate
	{
		private final List<BulkResult> newResults;
		private final int offset;
		private final boolean finished;

		public BulkResultUpdate(int offset, List<BulkResult> newResults, boolean finished)
		{
			this.offset = offset;
			this.newResults = newResults;
			this.finished = finished;
		}

		public List<BulkResult> getNewResults()
		{
			return newResults;
		}

		public boolean isFinished()
		{
			return finished;
		}

		public int getOffset()
		{
			return offset;
		}
	}

	public class ItemXmlTreeModel implements HtmlTreeModel
	{

		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, @Nullable String xpath)
		{
			final List<HtmlTreeNode> list = Lists.newArrayList();
			final PropBagEx itemXml = getModel(info).getPreviewItemXml();
			if( itemXml == null )
			{
				return Collections.emptyList();
			}

			if( xpath == null )
			{
				xpath = "";
			}

			for( PropBagEx child : itemXml.iterator(xpath + "/*") )
			{
				String name = child.getNodeName();
				if( isAttribute(child) )
				{
					name = "@" + name;
				}
				String contents = child.getNode();
				String fullpath = Check.isEmpty(xpath) ? name : MessageFormat.format("{0}/{1}", xpath, name);
				list.add(new ItemTreeNode(name, contents, fullpath, isLeaf(child)));
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

	}

	public class ItemTreeNode implements HtmlTreeNode
	{
		private String name; // Node name
		private String content; // node content
		private String id; // Full xpath
		private boolean leaf; // No children

		public ItemTreeNode(String name, String content, String id, boolean isLeaf)
		{
			this.name = name;
			this.id = id;
			this.leaf = isLeaf;
			this.content = content;
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
			if( !Check.isEmpty(content) )
			{
				name = name + ": ";
				nodeDiv = new DivRenderer(
					new CombinedRenderer(new LabelRenderer(getLabel()), new LabelRenderer(new TextLabel(content))));
			}
			else
			{
				nodeDiv = new DivRenderer(new LabelRenderer(getLabel()));
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

	@Override
	public void setOkCallback(JSCallable okCallback)
	{
		// Implementing OkayableDialog for the setCancelCallback only
	}

	@Override
	public void setOkLabel(Label okLabel)
	{
		// Implementing OkayableDialog for the setCancelCallback only
	}

	public void setTaskId(SectionInfo info, String taskId)
	{
		getModel(info).setTaskId(taskId);
	}

	@Override
	public void setCancelCallback(JSCallable cancelCallback)
	{
		this.cancelCallback = addParentCallable(cancelCallback);
	}

	public Button getOkButton()
	{
		return okButton;
	}

	public Pager getPager()
	{
		return pager;
	}

	@Override
	public String getWidth()
	{
		return "600px";
	}

	@Override
	public String getHeight()
	{
		return "475px";
	}

	public SingleSelectionList<OperationInfo> getOperationList()
	{
		return operationList;
	}

	public Button getNextButton()
	{
		return nextButton;
	}

	public Button getPrevButton()
	{
		return prevButton;
	}

	public Table getSelectionsTable()
	{
		return selectionsTable;
	}

	public Table getBulkResultsTable()
	{
		return bulkResultsTable;
	}

	public Tree getPreviewTree()
	{
		return previewTree;
	}
}