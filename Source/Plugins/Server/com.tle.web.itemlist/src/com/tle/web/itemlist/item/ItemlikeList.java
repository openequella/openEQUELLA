package com.tle.web.itemlist.item;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.ListEntriesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;

@NonNullByDefault
public interface ItemlikeList<I extends IItem<?>, LE extends ItemlikeListEntry<I>> extends ListEntriesSection<LE>
{
	LE addItem(SectionInfo info, I item, @Nullable FreetextResult result);

	void setNullItemsRemovedOnModel(SectionInfo info, boolean nullItemsRemoved);

	ListSettings<LE> getListSettings(SectionInfo info);

	List<LE> initEntries(RenderContext context);
}
