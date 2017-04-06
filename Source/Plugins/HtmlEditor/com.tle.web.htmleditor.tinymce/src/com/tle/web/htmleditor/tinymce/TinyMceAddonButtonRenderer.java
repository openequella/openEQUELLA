package com.tle.web.htmleditor.tinymce;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;

/**
 * @author Aaron
 */
public class TinyMceAddonButtonRenderer extends DivRenderer
{
	public TinyMceAddonButtonRenderer(String imageUrl, Label alt)
	{
		super("tleSkin tleSkinSilver", new DivRenderer("a", "mceButton",
			new ImageRenderer(imageUrl, alt).addClass("mceIcon")));
	}
}
