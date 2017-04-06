/*
 * Created on Oct 26, 2005
 */
package com.tle.core.dao;

import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.core.hibernate.dao.GenericDao;

public interface AttachmentDao extends GenericDao<Attachment, Long>
{
	List<Attachment> findByMd5Sum(String md5Sum, ItemDefinition collection, boolean ignoreDeletedRejectedSuspenedItems);

	List<FileAttachment> findFilesWithNoMD5Sum();

	List<CustomAttachment> findResourceAttachmentsByQuery(String query, boolean liveOnly, String sortHql);
}
