package com.tle.web.sections.equella.component.model;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.AbstractEventOnlyComponent;
import com.tle.web.sections.standard.model.TableState;

/**
 * The class formerly known as CurrentlySelectedStuff
 * 
 * @author aholland
 */
public class SelectionsTableState extends TableState
{
	private Label nothingSelectedText;
	private AbstractEventOnlyComponent<?> addAction;
	private List<SelectionsTableSelection> selections;

	@SuppressWarnings("nls")
	public SelectionsTableState()
	{
		super("selectedstuff");
	}

	public List<SelectionsTableSelection> getSelections()
	{
		if( selections == null )
		{
			selections = Lists.newArrayList();
		}
		return selections;
	}

	public void setSelections(List<SelectionsTableSelection> selections)
	{
		this.selections = selections;
	}

	public Label getNothingSelectedText()
	{
		return nothingSelectedText;
	}

	public void setNothingSelectedText(Label nothingSelectedText)
	{
		this.nothingSelectedText = nothingSelectedText;
	}

	public AbstractEventOnlyComponent<?> getAddAction()
	{
		return addAction;
	}

	public void setAddAction(AbstractEventOnlyComponent<?> addAction)
	{
		this.addAction = addAction;
	}
}
