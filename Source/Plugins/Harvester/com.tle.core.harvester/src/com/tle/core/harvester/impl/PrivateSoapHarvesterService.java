package com.tle.core.harvester.impl;

import java.io.OutputStream;

import com.tle.beans.item.Item;

/**
 * @author will
 */
public interface PrivateSoapHarvesterService
{

	/**
	 * Zip up and download all of an items attachments.<br>
	 * The logged in user must have the DOWNLOAD_ITEM permission for the item
	 * for this method to work.
	 * 
	 * @param cos An opened OutputStream
	 * @param item The item to download
	 */
	void getItemAttachments(OutputStream cos, Item item) throws Exception;
}
