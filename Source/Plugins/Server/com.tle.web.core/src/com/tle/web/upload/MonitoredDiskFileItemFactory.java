/*
 * Licence: Use this however/wherever you like, just don't blame me if it breaks
 * anything. Credit: If you're nice, you'll leave this bit: Class by
 * Pierre-Alexandre Losson -- http://www.telio.be/blog email :
 * plosson@users.sourceforge.net
 */
/*
 * Changed for Part 2, by Ken Cochrane http://KenCochrane.net ,
 * http://CampRate.com , http://PopcornMonsters.com
 */
package com.tle.web.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Original : plosson on 05-janv.-2006 10:46:26 - Last modified by
 *         $Author: plosson $ on $Date: 2006/01/05 10:09:38 $
 * @version 1.0 - Rev. $Revision: 1.1 $
 */
public class MonitoredDiskFileItemFactory extends DiskFileItemFactory
{
	Logger log = Logger.getLogger(this.getClass());
	private OutputStreamListener listener = null;

	public MonitoredDiskFileItemFactory(OutputStreamListener listener)
	{
		super();
		log.debug("inside MonitoredDiskFileItemFactory constructor (listener) ");
		this.listener = listener;
	}

	public MonitoredDiskFileItemFactory(int sizeThreshold, File repository, OutputStreamListener listener)
	{
		super(sizeThreshold, repository);
		log.debug("inside MonitoredDiskFileItemFactory constructor ");
		this.listener = listener;
	}

	@Override
	public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName)
	{
		log.debug("inside MonitoredDiskFileItemFactory createItem ");
		return new MonitoredDiskFileItem(fieldName, contentType, isFormField, fileName, getSizeThreshold(),
			getRepository(), listener);
	}
}
