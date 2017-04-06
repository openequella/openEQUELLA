package com.tle.core.pss.service;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.WorkflowParams;
import com.tle.web.scorm.ScormUtils;

@Bind
public class EnqueuePearsonScormServicesOperation extends AbstractWorkflowOperation
{
	@Inject
	private PearsonScormServicesService pssService;

	@Override
	public boolean execute()
	{
		if( pssService.isEnabled() )
		{
			final Item item = getItem();
			CustomAttachment scormAtt = new UnmodifiableAttachments(item)
				.getFirstCustomOfType(ScormUtils.ATTACHMENT_TYPE);

			if( scormAtt != null )
			{
				params.addAfterCommitHook(WorkflowParams.COMMIT_HOOK_PRIORITY_LOW, new Runnable()
				{
					@Override
					public void run()
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
				});
			}
			else if( params.isUpdate() )
			{
				// May ask PSS to delete a SCORM package that
				// does not exist on PSS but who cares!?
				pssService.deleteScormPackage(item, true);
			}
		}

		return false;
	}
}
