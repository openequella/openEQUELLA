package com.tle.web.template.section.event;

import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public final class BlueBarConstants
{
	static
	{
		PluginResourceHandler.init(BlueBarConstants.class);
	}

	public static final String BLUEBAR_PREFIX = "bluebar_";

	@PlugKey("buttonbar.helpbutton")
	private static Label LABEL_HELPBUTTON;
	@PlugKey("buttonbar.screenoptionsbutton")
	private static Label LABEL_SCREENOPTIONS;

	public enum Type
	{
		SCREENOPTIONS(LABEL_SCREENOPTIONS, 200), HELP(LABEL_HELPBUTTON, 100);

		private final Label label;
		private final int priority;

		Type(Label label, int priority)
		{
			this.label = label;
			this.priority = priority;
		}

		private String getKey()
		{
			return name().toLowerCase();
		}

		public BlueBarRenderable content(SectionRenderable renderable)
		{
			return new BlueBarRenderable(getKey(), label, renderable, priority);
		}

	}

	private BlueBarConstants()
	{
	}
}
