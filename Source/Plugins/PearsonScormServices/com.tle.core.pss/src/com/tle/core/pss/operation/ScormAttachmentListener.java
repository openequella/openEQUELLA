package com.tle.core.pss.operation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemAttachmentListener;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.pss.service.PearsonScormServicesService;
import com.tle.web.scorm.ScormUtils;

@Bind
@Singleton
public class ScormAttachmentListener implements ItemAttachmentListener
{
	@Inject
	private PearsonScormServicesService pssService;

	@Override
	public void attachmentsChanged(ItemEditor editor, Item item, FileHandle fileHandle)
	{
		if( pssService.isEnabled() )
		{
			CustomAttachment scormAtt = new UnmodifiableAttachments(item)
				.getFirstCustomOfType(ScormUtils.ATTACHMENT_TYPE);

			if( scormAtt != null )
			{
				ItemStatus itemStatus = item.getStatus();
				if( ItemStatus.DELETED.equals(itemStatus) )
				{
					pssService.deleteScormPackage(item, false);
				}
				else
				{
					pssService.addScormPackage(item, scormAtt);
				}
			}
			else if( !editor.isNewItem() )
			{
				pssService.deleteScormPackage(item, true);
			}
		}
	}
}
