package com.tle.core.services;

import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.scripting.ScriptEvaluator;
import com.tle.core.scripting.WorkflowScriptObjectContributor;

/**
 * @author aholland
 */
public interface WorkflowOperationService extends ScriptEvaluator, WorkflowScriptObjectContributor
{
	ScriptContext createScriptContext(ItemPack itemPack, FileHandle fileHandle,
		Map<String, Object> attributes, Map<String, Object> objects);

	boolean isAnOwner(Item item, String userUuid);

	UserBean getOwner(Item item);

	void updateMetadataBasedSecurity(PropBagEx itemxml, Item item);

}
