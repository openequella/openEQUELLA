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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.SimpleAjaxCallback;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.equella.search.AbstractSearchActionsSection;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryTimer;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.ReloadStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.OkayableDialog;

import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.wrappers.LongSet;

@NonNullByDefault
@TreeIndexed
@SuppressWarnings("nls")
public abstract class AbstractBulkSelectionSection<T extends ItemKey>
	extends
		AbstractPrototypeSection<AbstractBulkSelectionSection.Model<T>>
	implements
		HtmlRenderer,
		BeforeEventsListener,
		ReadyToRespondListener
{
	public static final String DIVID_SELECTBOX = "bulk-selection";

	public static final IncludeFile BULKINCLUDE = new IncludeFile(
		ResourcesService.getResourceHelper(AbstractBulkSelectionSection.class).url("scripts/bulkoperation.js"),
		JQueryTimer.PRERENDER, AjaxGenerator.AJAX_LIBRARY);

	static
	{
		PluginResourceHandler.init(AbstractBulkSelectionSection.class);
	}

	@PlugKey("selectionsbox.title")
	private static String titleKey;
	@PlugKey("selectionsbox.execute")
	private static Label LABEL_EXECUTE;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private TaskService taskService;
	@Inject
	private BulkOperationService bulkService;
	@Inject
	private UserSessionService userSessionService;

	@Component
	private Box box;
	@Component
	private Div div;
	@Component
	private Button selectAllButton;
	@Component
	private Link viewSelectedLink;
	@Component
	private Link unselectAllLink;
	@Component
	private Button executeButton;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> resultsSection;

	private SimpleAjaxCallback resultShowCallback;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		box.setNoMinMaxOnHeader(true);
		unselectAllLink.setLabel(getLabelUnselectAll());
		viewSelectedLink.setLabel(getLabelViewSelected());
		OkayableDialog bulkDialog = getBulkDialog();
		viewSelectedLink.setClickHandler(new OverrideHandler(bulkDialog.getOpenFunction(), false));
		unselectAllLink.setClickHandler(events.getNamedHandler("unselectAll"));
		selectAllButton.setClickHandler(events.getNamedHandler("selectAll"));
		selectAllButton.setDefaultRenderer(RendererConstants.LINK);
		selectAllButton.setLabel(getLabelSelectAll());

		executeButton.setLabel(LABEL_EXECUTE);
		executeButton.setStyleClass("execute-action");
		executeButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		executeButton.setClickHandler(new OverrideHandler(bulkDialog.getOpenFunction(), true));

		bulkDialog.setCancelCallback(new SimpleFunction("cancel_" + id,
			StatementBlock.get(new FunctionCallStatement(bulkDialog.getCloseFunction()), new ReloadStatement())));
		tree.setLayout(id, AbstractSearchActionsSection.AREA_SELECT);
	}

	public JSCallable getUpdateSelection(SectionTree tree, ParameterizedEvent event)
	{
		return ajax.getAjaxUpdateDomFunction(tree, null, event, ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE),
			DIVID_SELECTBOX);
	}

	protected abstract Label getLabelSelectAll();

	protected abstract Label getLabelUnselectAll();

	protected abstract Label getLabelViewSelected();

	protected abstract OkayableDialog getBulkDialog();

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		Model<T> model = getModel(context);
		int selectionCount;
		if( useBitSet() )
		{
			long size = model.getBitSet().size();
			if( size > Integer.MAX_VALUE )
			{
				throw new RuntimeException("Too many selections");
			}
			else
			{
				selectionCount = (int) size;
			}
		}
		else
		{
			selectionCount = model.getSelections().size();
		}
		box.setLabel(context, new KeyLabel(titleKey, selectionCount));
		selectAllButton.setDisplayed(context, resultsSection.getPaging().isResultsAvailable(context));
		model.setSelectionBoxCountLabel(getSelectionBoxCountLabel(selectionCount));
		setupButton(context, selectionCount);

		if( !showSelections(context) )
		{
			div.setDisplayed(context, false);
		}

		return viewFactory.createResult("bulkselect.ftl", this);
	}

	protected void setupButton(SectionInfo context, int selectionCount)
	{
		if( showExecuteButton(context) )
		{
			executeButton.setDisplayed(context, true);
			if( selectionCount > 0 )
			{
				executeButton.setClickHandler(context, new OverrideHandler(getBulkDialog().getOpenFunction(), true));
			}
			else
			{
				executeButton.setClickHandler(context, new OverrideHandler(Js.alert_s(getPleaseSelectLabel())));
			}
		}
		else
		{
			executeButton.setDisplayed(context, false);
		}

	}

	protected boolean showSelections(SectionInfo info)
	{
		return true;
	}

	protected boolean showExecuteButton(SectionInfo info)
	{
		return true;
	}

	protected abstract Label getPleaseSelectLabel();

	protected abstract Label getSelectionBoxCountLabel(int selectionCount);

	public String executeWithExecutor(SectionInfo info, BeanLocator<? extends BulkOperationExecutor> executor)
	{
		Model<T> model = getModel(info);
		ClusteredTask task = null;
		if( useBitSet() )
		{
			LongSet bitSet = model.getBitSet();
			LongSet items = bitSet.clone();
			bitSet.clear();
			model.setModifiedSelection(true);
			task = bulkService.createTask(items, executor);
		}
		else
		{
			Set<T> selections = model.getSelections();
			List<T> items = new ArrayList<T>(selections);
			selections.clear();
			model.setModifiedSelection(true);
			task = bulkService.createTask(items, executor);
		}
		return taskService.startTask(task);
	}

	@EventHandlerMethod
	public abstract void selectAll(SectionInfo info);

	@EventHandlerMethod
	public void unselectAll(SectionInfo info)
	{
		Model<T> model = getModel(info);
		if( useBitSet() )
		{
			model.getBitSet().clear();
		}
		else
		{
			model.getSelections().clear();
		}
		model.setModifiedSelection(true);
	}

	public Box getBox()
	{
		return box;
	}

	public Link getViewSelectedLink()
	{
		return viewSelectedLink;
	}

	public Link getUnselectAllLink()
	{
		return unselectAllLink;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model<T>();
	}

	public void addSelection(SectionInfo info, T itemId)
	{
		Model<T> model = getModel(info);
		if( useBitSet() && itemId instanceof ItemIdKey )
		{
			long key = ((ItemIdKey) itemId).getKey();
			LongSet bitSet = model.getBitSet();
			bitSet.add(key);
		}
		else
		{
			Set<T> selections = model.getSelections();
			selections.add(itemId);
		}
		model.setModifiedSelection(true);
	}

	public static class Model<T extends ItemKey>
	{
		@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION, name = "r")
		private boolean restoreSelection;
		private boolean modifiedSelection;
		private Set<T> selections = new LinkedHashSet<T>();
		private LongSet bitSet = new LongSet(new ConciseSet());

		private ObjectExpression dialogs = new ObjectExpression();
		private Label selectionBoxCountLabel;

		public Set<T> getSelections()
		{
			return selections;
		}

		public void setSelections(Set<T> selections)
		{
			this.selections = selections;
		}

		public ObjectExpression getDialogs()
		{
			return dialogs;
		}

		public void setDialogs(ObjectExpression dialogs)
		{
			this.dialogs = dialogs;
		}

		public boolean isRestoreSelection()
		{
			return restoreSelection;
		}

		public void setRestoreSelection(boolean restoreSelection)
		{
			this.restoreSelection = restoreSelection;
		}

		public boolean isModifiedSelection()
		{
			return modifiedSelection;
		}

		public void setModifiedSelection(boolean modifiedSelection)
		{
			this.modifiedSelection = modifiedSelection;
		}

		public void setSelectionBoxCountLabel(Label selectionBoxCountLabel)
		{
			this.selectionBoxCountLabel = selectionBoxCountLabel;
		}

		public Label getSelectionBoxCountLabel()
		{
			return selectionBoxCountLabel;
		}

		public LongSet getBitSet()
		{
			return bitSet;
		}

		public void setBitSet(LongSet bitSet)
		{
			this.bitSet = bitSet;
		}
	}

	public Button getExecuteButton()
	{
		return executeButton;
	}

	public SimpleAjaxCallback getResultShowCallback()
	{
		return resultShowCallback;
	}

	public Button getSelectAllButton()
	{
		return selectAllButton;
	}

	@Override
	public void beforeEvents(SectionInfo info)
	{
		Model<T> model = getModel(info);
		if( model.isRestoreSelection() )
		{
			if( useBitSet() )
			{
				LongSet selections = userSessionService.getAttribute(getKeySelections());
				if( selections != null )
				{
					model.setBitSet(selections);
				}
			}
			else
			{
				Set<T> selections = userSessionService.getAttribute(getKeySelections());
				if( selections != null )
				{
					model.setSelections(selections);
				}
			}
		}
		else
		{
			model.setModifiedSelection(true);
			model.setRestoreSelection(true);
		}
	}

	protected abstract String getKeySelections();

	protected abstract boolean useBitSet();

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		Model<T> model = getModel(info);
		if( model.isModifiedSelection() )
		{
			if( useBitSet() )
			{
				userSessionService.setAttribute(getKeySelections(), model.getBitSet());
			}
			else
			{
				userSessionService.setAttribute(getKeySelections(), model.getSelections());
			}
		}
	}

	public boolean isSelected(SectionInfo info, T itemId)
	{
		if( useBitSet() && itemId instanceof ItemIdKey )
		{
			long key = ((ItemIdKey) itemId).getKey();
			return getModel(info).getBitSet().contains(key);
		}
		return getModel(info).getSelections().contains(itemId);
	}

	public void removeSelection(SectionInfo info, T itemId)
	{
		Model<T> model = getModel(info);
		if( useBitSet() && itemId instanceof ItemIdKey )
		{
			long key = ((ItemIdKey) itemId).getKey();
			LongSet bitSet = model.getBitSet();
			model.setModifiedSelection(bitSet.remove(key));
		}
		else
		{
			Set<T> selections = model.getSelections();
			model.setModifiedSelection(selections.remove(itemId));
		}
	}

	public Set<T> getSelections(SectionInfo info)
	{
		return getModel(info).getSelections();
	}

	public LongSet getBitSet(SectionInfo info)
	{
		return getModel(info).getBitSet();
	}

	public void hideSelection(SectionInfo info)
	{
		box.getState(info).setDisplayed(false);
		executeButton.getState(info).setDisplayed(false);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "bss";
	}

	public Div getDiv()
	{
		return div;
	}
}
