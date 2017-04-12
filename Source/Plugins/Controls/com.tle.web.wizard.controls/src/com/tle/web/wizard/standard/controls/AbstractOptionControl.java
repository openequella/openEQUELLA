package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.Item;
import com.tle.web.wizard.controls.OptionCtrl;

public abstract class AbstractOptionControl extends AbstractSimpleWebControl
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	protected OptionCtrl optionControl;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		optionControl = (OptionCtrl) control;
		super.setWrappedControl(control);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		Collection<String> vals = getList().getSelectedValuesAsStrings(info);
		optionControl.setValues(vals.toArray(new String[vals.size()]));
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		MultiSelectionList<Item> list = getList();
		list.setParameterId(getFormName());
		OptionsModel listModel = new OptionsModel(optionControl);
		list.setListModel(listModel);
		prepareList(listModel);
		if( optionControl.isReload() )
		{
			list.setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(getReloadFunction()));
		}

	}

	protected void prepareList(OptionsModel listModel)
	{
		// the other stuff
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		List<Item> items = optionControl.getItems();
		List<String> options = new ArrayList<String>();
		for( Item item : items )
		{
			if( item.isSelected() )
			{
				options.add(item.getValue());
			}
		}
		MultiSelectionList<Item> listComponent = getList();
		listComponent.setSelectedStringValues(context, options);
		extraRender(context);
		addDisabler(context, listComponent);
		return viewFactory.createResult(getTemplate(), context);
	}

	protected void extraRender(SectionInfo info)
	{
		// nothing
	}

	protected abstract String getTemplate();

	public abstract MultiSelectionList<Item> getList();
}
