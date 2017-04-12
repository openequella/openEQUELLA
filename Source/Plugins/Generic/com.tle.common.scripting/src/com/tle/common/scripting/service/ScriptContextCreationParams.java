package com.tle.common.scripting.service;

import java.util.Map;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;

/**
 * Provided to ScriptObjectContributors to give them some context to create
 * script objects with
 * 
 * @author aholland
 */
public interface ScriptContextCreationParams
{
	ItemPack<Item> getItemPack();

	/**
	 * A map of additional objects for context. These do not get injected into
	 * script altough the ScriptObjectContributors may wrap them and then inject
	 * them. Don't return null
	 * 
	 * @return
	 */
	Map<String, Object> getAttributes();

	boolean isAnOwner();

	boolean isModerationAllowed();

	FileHandle getFileHandle();

	boolean isAllowSystemCalls();
}