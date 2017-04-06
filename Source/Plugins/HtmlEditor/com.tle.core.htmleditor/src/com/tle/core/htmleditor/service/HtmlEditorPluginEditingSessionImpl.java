package com.tle.core.htmleditor.service;

import com.tle.common.EntityPack;
import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

/**
 * @author aholland
 */
public class HtmlEditorPluginEditingSessionImpl
	extends
		EntityEditingSessionImpl<HtmlEditorPluginEditingBean, HtmlEditorPlugin>
	implements
		HtmlEditorPluginEditingSession
{
	private static final long serialVersionUID = 1L;

	public HtmlEditorPluginEditingSessionImpl(String sessionId, EntityPack<HtmlEditorPlugin> pack,
		HtmlEditorPluginEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
