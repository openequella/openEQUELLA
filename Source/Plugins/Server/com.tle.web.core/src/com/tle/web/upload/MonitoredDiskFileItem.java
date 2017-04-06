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
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Original : plosson on 05-janv.-2006 10:46:33 - Last modified by
 *         $Author: plosson $ on $Date: 2006/01/05 10:09:38 $
 * @version 1.0 - Rev. $Revision: 1.1 $
 */
public class MonitoredDiskFileItem extends DiskFileItem
{
	Logger log = Logger.getLogger(this.getClass());
	private MonitoredOutputStream mos = null;
	private OutputStreamListener listener;

	public MonitoredDiskFileItem(String fieldName, String contentType, boolean isFormField, String fileName,
		int sizeThreshold, File repository, OutputStreamListener listener)
	{
		super(fieldName, contentType, isFormField, fileName, sizeThreshold, repository);
		log.debug("inside MonitoredDiskFileItem constructor ");
		this.listener = listener;
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		log.debug("inside getOutputStream() ");
		if( mos == null )
		{
			mos = new MonitoredOutputStream(super.getOutputStream(), listener);
		}
		log.debug("leaving getOutputStream() ");
		return mos;
	}
}
