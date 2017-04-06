package com.tle.web.controls.universal;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.web.sections.SectionInfo;

/**
 * An {@link AbstractDetailsAttachmentHandler} for when your {@link Attachment}
 * object carries all the state required to make modifications to the item.
 * 
 * @author jolz
 * @param <M>
 */
@NonNullByDefault
public abstract class BasicAbstractAttachmentHandler<M extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel>
	extends
		AbstractDetailsAttachmentHandler<M, BasicUniversalAttachment>
{
	@Override
	protected BasicUniversalAttachment createUniversalAttachmentForEdit(SectionInfo info, Attachment attachment)
	{
		return new BasicUniversalAttachment(attachment);
	}

	@Override
	protected List<BasicUniversalAttachment> createUniversalAttachments(SectionInfo info)
	{
		return ImmutableList.copyOf(Lists.transform(createAttachments(info),
			new Function<Attachment, BasicUniversalAttachment>()
			{
				@NonNullByDefault(false)
				@Override
				public BasicUniversalAttachment apply(Attachment input)
				{
					return new BasicUniversalAttachment(input);
				}
			}));
	}

	protected abstract List<Attachment> createAttachments(SectionInfo info);
}
