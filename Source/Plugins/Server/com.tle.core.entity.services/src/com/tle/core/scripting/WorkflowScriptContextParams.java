package com.tle.core.scripting;

import java.util.Map;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemPack;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.core.services.WorkflowOperationService;
import com.tle.core.user.CurrentUser;

/**
 * @author aholland
 */
public class WorkflowScriptContextParams extends StandardScriptContextParams
{
	private final WorkflowOperationService service;

	public WorkflowScriptContextParams(WorkflowOperationService service, ItemPack itemPack,
		FileHandle fileHandle, Map<String, Object> attributes)
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
