package com.tle.web.itemlist;

import java.util.Collection;
import java.util.List;

import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;

public interface ListEntry
{
	HtmlLinkState getTitle();

	Label getDescription();

	HtmlBooleanState getCheckbox();

	boolean isHilighted();

	void setHilighted(boolean highlighted);

	List<MetadataEntry> getMetadata();

	void addMetadata(MetadataEntry meta);

	void addDelimitedMetadata(Label label, Object... data);

	void addDelimitedMetadata(Label label, Collection<?> data);

	void setAttribute(Object key, Object value);

	boolean isFlagSet(String flagKey);

	void init(RenderContext context, ListSettings<? extends ListEntry> settings);

	void setInfo(SectionInfo info);

	void addRatingAction(int order, Object... ratingData);

	void addRatingAction(Object... ratingData);

	void addRatingMetadata(Object... ratingData);

	void addRatingMetadataWithOrder(int order, Object... ratingData);

	void setThumbnailCount(DivRenderer count);

	void addThumbnail(SectionRenderable renderable);
}
