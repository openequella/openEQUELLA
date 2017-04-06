package com.tle.web.wizard.section;

import java.util.List;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.impl.WizardCommand;

public interface SectionCommandable extends Section
{
	void addCommands(SectionInfo info, List<WizardCommand> commandList);
}
