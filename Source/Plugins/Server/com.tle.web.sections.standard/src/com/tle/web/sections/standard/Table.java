package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.js.JSTableComponent;
import com.tle.web.sections.standard.js.impl.DelayedJSTableComponent;
import com.tle.web.sections.standard.model.TableModel;
import com.tle.web.sections.standard.model.TableState;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class Table extends AbstractTable<TableState> implements JSTableComponent
{
	private boolean wrap;
	@Nullable
	private TableModel model;
	private boolean filterable = true;
	@Nullable
	private DelayedJSTableComponent delayedTable;

	public Table()
	{
		super("table");
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		delayedTable = new DelayedJSTableComponent(this);
	}

	@Override
	public TableState instantiateModelEx(@Nullable SectionInfo info)
	{
		return new TableState();
	}

	@Override
	protected TableState setupState(SectionInfo info, TableState state)
	{
		super.setupState(info, state);
		state.setWrap(wrap);
		state.setFilterable(filterable);
		return state;
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		super.prepareModel(info);

		if( model != null )
		{
			final TableState state = getState(info);
			state.setRows(model.getRows(info));
		}
	}

	public void makePresentation(SectionInfo info)
	{
		getState(info).makePresentation();
	}

	public boolean isWrap()
	{
		return wrap;
	}

	public void setWrap(boolean wrap)
	{
		this.wrap = wrap;
	}

	public TableModel getTableModel()
	{
		return model;
	}

	public void setTableModel(TableModel tableModel)
	{
		this.model = tableModel;
	}

	@Override
	public JSCallable createFilterFunction()
	{
		return delayedTable.createFilterFunction();
	}

	@Override
	public void rendererSelected(RenderContext info, SectionRenderable renderer)
	{
		delayedTable.rendererSelected(info, renderer);
		super.rendererSelected(info, renderer);
	}

	public boolean isFilterable()
	{
		return filterable;
	}

	public void setFilterable(boolean filterable)
	{
		this.filterable = filterable;
	}
}
