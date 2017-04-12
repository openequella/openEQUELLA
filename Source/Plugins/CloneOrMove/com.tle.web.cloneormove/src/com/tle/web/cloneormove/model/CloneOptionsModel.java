package com.tle.web.cloneormove.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;

/**
 * @author aholland
 */
public class CloneOptionsModel extends DynamicHtmlListModel<NameValue>
{
	public enum CloneOption
	{
		CLONE()
		{
			@Override
			public String toString()
			{
				return Integer.toString(ordinal());
			}
		},
		CLONE_NO_ATTACHMENTS()
		{
			@Override
			public String toString()
			{
				return Integer.toString(ordinal());
			}
		},
		MOVE()
		{
			@Override
			public String toString()
			{
				return Integer.toString(ordinal());
			}
		}
	}

	private final boolean canMove;
	private final boolean canClone;
	private final boolean canCloneNoAttachments;

	public CloneOptionsModel(final boolean canMove, final boolean canClone, final boolean canCloneNoAttachments)
	{
		this.canMove = canMove;
		this.canClone = canClone;
		this.canCloneNoAttachments = canCloneNoAttachments;
	}

	@Override
	protected Iterable<NameValue> populateModel(SectionInfo info)
	{
		final List<NameValue> values = new ArrayList<NameValue>();
		if( isCanClone(info) )
		{
			values.add(new BundleNameValue(
				"com.tle.web.cloneormove.selectcollection.option.clone.clone", CloneOption.CLONE.toString())); //$NON-NLS-1$
		}
		if( isCanCloneNoAttachments(info) )
		{
			values
				.add(new BundleNameValue(
					"com.tle.web.cloneormove.selectcollection.option.clone.clonenoattachments", CloneOption.CLONE_NO_ATTACHMENTS.toString())); //$NON-NLS-1$
		}
		if( isCanMove(info) )
		{
			values.add(new BundleNameValue(
				"com.tle.web.cloneormove.selectcollection.option.clone.move", CloneOption.MOVE.toString())); //$NON-NLS-1$
		}
		return values;
	}

	protected boolean isCanMove(final SectionInfo info)
	{
		return canMove;
	}

	protected boolean isCanClone(final SectionInfo info)
	{
		return canClone;
	}

	protected boolean isCanCloneNoAttachments(final SectionInfo info)
	{
		return canCloneNoAttachments;
	}
}
