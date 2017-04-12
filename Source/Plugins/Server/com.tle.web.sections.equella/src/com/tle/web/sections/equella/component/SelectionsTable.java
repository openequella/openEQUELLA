package com.tle.web.sections.equella.component;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.component.model.SelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.AbstractEventOnlyComponent;
import com.tle.web.sections.standard.AbstractTable;

/**
 * @author Aaron
 */
public class SelectionsTable extends AbstractTable<SelectionsTableState>
{
	private Label nothingSelectedText;
	private AbstractEventOnlyComponent<?> addAction;
	private SelectionsTableModel selectionsModel;
	private Boolean filterable;

	public SelectionsTable()
	{
		super("selectedstuff");
	}

	@Override
	public SelectionsTableState instantiateModelEx(SectionInfo info)
	{
		return new SelectionsTableState();
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		super.prepareModel(info);

		final SelectionsTableState state = getModel(info);

		final AbstractEventOnlyComponent<?> stateAddAction = state.getAddAction();
		if( stateAddAction == null )
		{
			state.setAddAction(addAction);
		}

		final Label stateNothingSelectedText = state.getNothingSelectedText();
		if( stateNothingSelectedText == null )
		{
			state.setNothingSelectedText(nothingSelectedText);
		}

		if( selectionsModel != null )
		{
			state.setSelections(selectionsModel.getSelections(info));
		}

		if( filterable != null )
		{
			state.setFilterable(filterable);
		}
	}

	public Label getNothingSelectedText()
	{
		return nothingSelectedText;
	}

	public void setNothingSelectedText(Label nothingSelectedText)
	{
		this.nothingSelectedText = nothingSelectedText;
	}

	public void setFilterable(boolean filterable)
	{
		this.filterable = filterable;
	}

	public AbstractEventOnlyComponent<?> getAddAction()
	{
		return addAction;
	}

	public void setAddAction(AbstractEventOnlyComponent<?> addAction)
	{
		this.addAction = addAction;
	}

	public SelectionsTableModel getSelectionsModel()
	{
		return selectionsModel;
	}

	public void setSelectionsModel(SelectionsTableModel selectionsModel)
	{
		this.selectionsModel = selectionsModel;
	}
}
