package com.tle.web.copyright;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.web.integration.IntegrationInterface;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectableAttachment;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

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
	public boolean isAttachmentSelectable(SectionInfo info, IItem<?> item, String attachmentUuid)
	{
		if( isItemCopyrighted(item) )
		{
			String activationType = copyrightService.getActivationType();
			SelectionSession session = selectionService.getCurrentSession(info);
			if( session != null )
			{
				String courseCode = session.getStructure().getAttribute("courseCode");
				if( courseCode == null )
				{
					IntegrationInterface integ = integrationService.getIntegrationInterface(info);
					if( integ == null )
					{
						// we must be in a local selection session
						return true;
					}
					courseCode = integrationService.getIntegrationInterface(info).getCourseInfoCode();
				}
				if( attachmentUuid != null )
				{
					return activationService
						.attachmentIsSelectableForCourse(activationType, attachmentUuid, courseCode);
				}
				for( IAttachment att : ((Item) item).getAttachmentsUnmodifiable() )
				{
					if( activationService.attachmentIsSelectableForCourse(activationType, att.getUuid(), courseCode) )
					{
						return true;
					}
				}
			}
			else
			{
				if( attachmentUuid != null )
				{
					return activationService.isActiveOrPending(activationType, attachmentUuid);
				}
				for( IAttachment att : ((Item) item).getAttachmentsUnmodifiable() )
				{
					if( activationService.isActiveOrPending(activationType, att.getUuid()) )
					{
						return true;
					}
				}
			}
			return false;
		}
		return true;
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
