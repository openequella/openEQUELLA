package com.tle.core.item.scripting;

import java.util.Map;

import com.tle.beans.item.ItemPack;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.item.service.ItemService;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.common.usermanagement.user.CurrentUser;

/**
 * @author aholland
 */
public class WorkflowScriptContextParams extends StandardScriptContextParams
{
	private final ItemService service;

	public WorkflowScriptContextParams(ItemService service, ItemPack itemPack, FileHandle fileHandle,
		Map<String, Object> attributes)
	{
		super(itemPack, fileHandle, true, attributes);
		this.service = service;
	}

	@Override
	public boolean isAnOwner()
	{
		return service.isAnOwner(getItemPack().getItem(), CurrentUser.getDetails().getUniqueID());
	}
}
