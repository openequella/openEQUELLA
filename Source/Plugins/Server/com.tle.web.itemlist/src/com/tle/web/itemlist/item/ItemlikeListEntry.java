package com.tle.web.itemlist.item;

import java.util.List;

import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author Aaron
 */
public interface ItemlikeListEntry<I extends IItem<?>> extends ListEntry
{
	// This can be null in the case that the item no longer exists
	@Nullable
	I getItem();

	List<ViewableResource> getViewableResources();

	// Debateable...

	TagState getTag();

	Label getSelectLabel();

	Label getUnselectLabel();

	void setToggle(SectionRenderable toggle);

	void addExtras(SectionRenderable extra);

	UnmodifiableAttachments getAttachments();

	void setSelected(boolean selected);

	boolean isSelectable();

	void setSelectable(boolean selectable);
}
