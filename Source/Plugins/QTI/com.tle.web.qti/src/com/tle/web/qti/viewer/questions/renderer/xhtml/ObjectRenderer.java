package com.tle.web.qti.viewer.questions.renderer.xhtml;

import java.util.Map;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * @author Aaron
 */
public class ObjectRenderer extends XhtmlElementRenderer
{
	@SuppressWarnings("unused")
	private final Object model;

	@AssistedInject
	public ObjectRenderer(@Assisted Object model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected void addAttributes(Map<String, String> attrs)
	{
		super.addAttributes(attrs);
		final String data = attrs.get("data");
		if( data != null )
		{
			attrs.put("data", getContext().getViewResourceUrl(data).getHref());
		}
	}
}
