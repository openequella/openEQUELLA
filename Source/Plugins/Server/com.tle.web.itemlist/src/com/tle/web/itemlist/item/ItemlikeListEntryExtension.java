package com.tle.web.itemlist.item;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;

@NonNullByDefault
public interface ItemlikeListEntryExtension<I extends IItem<?>, LE extends ItemlikeListEntry<I>>
{
	@SuppressWarnings("nls")
	String TYPE_STANDARD = "standard";

	@Nullable
	ProcessEntryCallback<I, LE> processEntries(RenderContext context, List<LE> entries, ListSettings<LE> listSettings);

	void register(SectionTree tree, String parentId);

	@Nullable
	String getItemExtensionType();

	interface ProcessEntryCallback<I extends IItem<?>, LE extends ItemlikeListEntry<I>>
	{
		void processEntry(LE entry);
	}
}
