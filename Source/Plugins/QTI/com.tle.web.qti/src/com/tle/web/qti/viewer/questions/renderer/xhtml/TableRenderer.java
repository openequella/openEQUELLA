package com.tle.web.qti.viewer.questions.renderer.xhtml;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Table;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;

/**
 * @author Aaron
 */
public class TableRenderer extends XhtmlElementRenderer
{
	@AssistedInject
	public TableRenderer(@Assisted Table model, @Assisted QtiViewerContext context)
	{
		super(model, context);
	}
}
