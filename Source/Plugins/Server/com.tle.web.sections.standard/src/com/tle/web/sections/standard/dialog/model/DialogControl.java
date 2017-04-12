package com.tle.web.sections.standard.dialog.model;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.render.Label;

public class DialogControl
{
	private final Label label;
	private final SectionId control;
	private final Label help;

	public DialogControl(Label label, SectionId sectionId)
	{
		this(label, sectionId, null);
	}

	public DialogControl(Label label, SectionId sectionId, Label help)
	{
		this.label = label;
		this.control = sectionId;
		this.help = help;
	}

	public Label getLabel()
	{
		return label;
	}

	public SectionId getControl()
	{
		return control;
	}

	/**
	 * Get help. See a professional.
	 * 
	 * @return
	 */
	public Label getHelp()
	{
		return help;
	}
}
