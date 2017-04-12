package com.tle.web.qti.viewer.questions.renderer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import uk.ac.ed.ph.jqtiplus.node.content.basic.InlineStatic;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Prompt;

import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

/**
 * @author Aaron
 */
public class PromptRenderer extends QtiNodeRenderer
{
	private final Prompt model;

	@AssistedInject
	public PromptRenderer(@Assisted Prompt model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preProcess()
	{
		// Nah
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// Nah
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		Map<String, String> attr = Maps.newHashMap();
		attr.put("class", "prompt");
		writer.writeTag("div", attr);
		List<InlineStatic> inlines = model.getChildren();
		for( InlineStatic inline : inlines )
		{
			qfac.chooseRenderer(inline, getContext()).realRender(writer);
		}
		writer.endTag("div");
	}
}
