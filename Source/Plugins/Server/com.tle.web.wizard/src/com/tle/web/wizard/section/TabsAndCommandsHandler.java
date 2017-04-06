package com.tle.web.wizard.section;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;

public class TabsAndCommandsHandler extends CollectInterfaceHandler<SectionTabable>
{
	private static final String COMMANDS_KEY = "$COMMANDABLES$"; //$NON-NLS-1$

	public TabsAndCommandsHandler()
	{
		super(SectionTabable.class);
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		super.registered(id, tree, section);
		List<SectionCommandable> commandables = tree.getAttribute(COMMANDS_KEY);
		if( commandables == null )
		{
			commandables = new ArrayList<SectionCommandable>();
			tree.setAttribute(COMMANDS_KEY, commandables);
		}

		if( section instanceof SectionCommandable )
		{
			commandables.add((SectionCommandable) section);
		}
	}

	public List<SectionCommandable> getCommandables(SectionInfo info)
	{
		return info.getTreeAttribute(COMMANDS_KEY);
	}
}
