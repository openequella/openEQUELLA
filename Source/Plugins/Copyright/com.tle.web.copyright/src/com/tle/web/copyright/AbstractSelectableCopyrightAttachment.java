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

package com.tle.web.copyright;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.web.integration.IntegrationInterface;
import com.tle.web.integration.extension.StructuredIntegrationSessionExtension;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectableAttachment;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.TargetStructure;

@NonNullByDefault
public abstract class AbstractSelectableCopyrightAttachment<H extends Holding, P extends Portion, S extends Section>
	implements
		SelectableAttachment
{

	private CopyrightService<H, P, S> copyrightService;
	@Inject
	private ActivationService activationService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private IntegrationService integrationService;

	@PostConstruct
	void setupService()
	{
		copyrightService = getCopyrightServiceImpl();
	}

	protected abstract CopyrightService<H, P, S> getCopyrightServiceImpl();

	@Override
	public boolean isAttachmentSelectable(SectionInfo info, IItem<?> item, @Nullable String attachmentUuid)
	{
		if( isItemCopyrighted(item) )
		{
			final IntegrationInterface integ = integrationService.getIntegrationInterface(info);
			// If not in a local selection session
			if( integ != null )
			{
				final String activationType = copyrightService.getActivationType();
				if( attachmentUuid != null )
				{
					if( activationService.isActiveOrPending(activationType, attachmentUuid)
						&& activationService.attachmentIsSelectableForCourse(activationType, attachmentUuid,
							getCourseCode(info)) )
					{
						return true;
					}
				}
				else
				{
					// Are any active or pending for 'select all' purposes?
					boolean anySelectable = false;
					for( IAttachment att : ((Item) item).getAttachmentsUnmodifiable() )
					{
						if( activationService.isActiveOrPending(activationType, att.getUuid())
							&& activationService.attachmentIsSelectableForCourse(activationType, att.getUuid(),
								getCourseCode(info)) )
						{
							anySelectable = true;
						}
					}
					if( anySelectable )
					{
						return true;
					}
				}

				return false;
			}
		}
		return true;
	}

	@Nullable
	private String getCourseCode(SectionInfo info)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		final TargetStructure structure = session.getStructure();
		String courseCode = structure.getAttribute(StructuredIntegrationSessionExtension.KEY_COURSE_CODE);
		if( courseCode == null )
		{
			courseCode = integrationService.getIntegrationInterface(info).getCourseInfoCode();
		}
		return courseCode;
	}

	@Override
	public boolean canBePushed(String attachmentUuid)
	{
		return activationService.isActiveOrPending(copyrightService.getActivationType(), attachmentUuid);
	}

	@Override
	public boolean isItemCopyrighted(IItem<?> item)
	{
		if( item instanceof Item )
		{
			return copyrightService.isCopyrightedItem((Item) item);
		}
		return false;
	}

	@Override
	public List<String> getApplicableCourseCodes(String attachmentUuid)
	{
		return activationService
			.getAllCurrentAndPendingActivations(copyrightService.getActivationType(), attachmentUuid).stream()
			.map(ar -> ar.getCourse().getCode()).collect(Collectors.toList());
	}

}
