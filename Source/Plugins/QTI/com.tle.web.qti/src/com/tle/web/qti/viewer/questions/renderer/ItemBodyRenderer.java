package com.tle.web.qti.viewer.questions.renderer;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

public class ItemBodyRenderer extends QtiNodeRenderer
{
	private final ItemBody model;

	@AssistedInject
	public ItemBodyRenderer(@Assisted ItemBody model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected String getTagName()
	{
		return "div";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends QtiNode> Iterator<T> getChildIterator()
	{
		return (Iterator<T>) model.getBlocks().iterator();
	}

	@Override
	protected void addAttributes(Map<String, String> attrs)
	{
		super.addAttributes(attrs);
		attrs.put("class", "itembody");
	}
}
