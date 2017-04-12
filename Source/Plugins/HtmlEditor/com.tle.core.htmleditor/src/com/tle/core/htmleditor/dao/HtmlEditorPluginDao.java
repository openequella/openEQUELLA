package com.tle.core.htmleditor.dao;

import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.dao.AbstractEntityDao;

/**
 * @author aholland
 */
public interface HtmlEditorPluginDao extends AbstractEntityDao<HtmlEditorPlugin>
{
	HtmlEditorPlugin getByPluginId(String pluginId);

	void changeUserId(String fromUserId, String toUserId);
}
