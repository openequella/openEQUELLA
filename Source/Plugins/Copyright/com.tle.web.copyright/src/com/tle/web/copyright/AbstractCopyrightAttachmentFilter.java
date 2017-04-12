package com.tle.web.copyright;

import javax.annotation.PostConstruct;

import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.attachments.AttachmentView;
import com.tle.web.viewitem.summary.section.SummarySection;

public abstract class AbstractCopyrightAttachmentFilter<H extends Holding, P extends Portion, S extends Section>
	implements
		AttachmentViewFilter
{

	private CopyrightService<H, P, S> copyrightService;
	private CopyrightWebService<H> copyrightWebService;

	@PostConstruct
	void setupService()
	{
		copyrightService = getCopyrightServiceImpl();
		copyrightWebService = getCopyrightWebServiceImpl();
	}

	protected abstract CopyrightWebService<H> getCopyrightWebServiceImpl();

	protected abstract CopyrightService<H, P, S> getCopyrightServiceImpl();

	@SuppressWarnings("nls")
	@Override
	public boolean shouldBeDisplayed(SectionInfo info, AttachmentView attachmentView)
	{
		if( !isApplicable(info, attachmentView.getViewableResource().getViewableItem()) )
		{
			return true;
		}

		final Item item = (Item) attachmentView.getViewableResource().getViewableItem().getItem();
		final IAttachment attachment = attachmentView.getAttachment();
		if( attachment.getAttachmentType() == AttachmentType.CUSTOM )
		{
			final CustomAttachment custom = (CustomAttachment) attachment;
			final Holding holding = copyrightWebService.getHolding(info, item);
			if( "resource".equals(custom.getType()) && custom.getData("uuid").equals(holding.getItem().getUuid()) )
			{
				return false;
			}
		}

		if( info.lookupSection(SummarySection.class) == null )
		{
			return true;
		}
		final boolean showAnyway = info.getBooleanAttribute("showCopyrightAttachments");
		return showAnyway
			|| (copyrightService.getSectionForAttachment(item, attachmentView.getAttachment().getUuid()) == null);
	}

	private boolean isApplicable(SectionInfo info, ViewableItem<?> vItem)
	{
		final IItem item = vItem.getItem();
		if( item instanceof Item )
		{
			if( copyrightService.isCopyrightedItem((Item) item) && vItem.isItemForReal() )
			{
				Holding holding = copyrightWebService.getHolding(info, (Item) item);
				if( holding != null )
				{
					return true;
				}
			}
		}
		return false;
	}
}
