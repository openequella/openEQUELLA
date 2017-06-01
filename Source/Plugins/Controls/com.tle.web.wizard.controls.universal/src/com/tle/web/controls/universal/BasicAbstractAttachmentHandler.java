/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
