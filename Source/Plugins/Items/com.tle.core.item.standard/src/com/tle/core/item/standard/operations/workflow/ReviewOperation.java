/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import java.util.Date;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.exceptions.AccessDeniedException;

/**
 * @author jmaginnis
 */
@SecureItemStatus({ItemStatus.LIVE, ItemStatus.ARCHIVED})
@SecureOnCall(priv = "REVIEW_ITEM")
public class ReviewOperation extends TaskOperation
{
	private final boolean force;

	@AssistedInject
	protected ReviewOperation()
	{
		this(true);
	}

	@AssistedInject
	protected ReviewOperation(@Assisted boolean force)
	{
		this.force = force;
	}

	/**
	 * @throws WorkflowException
	 */
	@SuppressWarnings("nls")
	@Override
	public boolean execute()
	{
		if( getWorkflow() == null )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.itemadmin.error.noworkflow"));
		}

		ModerationStatus status = getModerationStatus();
		Date reviewdate = status.getReviewDate();
		Date datenow = getParams().getDateNow();
		boolean needsReview = reviewdate != null && (datenow.compareTo(reviewdate) > 0);

		if( force || needsReview )
		{
			// Item requires review
			setState(ItemStatus.REVIEW);
			resetWorkflow();
			updateModeration();
			return true;
		}
		return false;

	}
}
