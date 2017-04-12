package com.tle.web.cloud.view.section;

import java.util.ArrayList;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.summary.section.AbstractDisplayNodesSection;
import com.tle.web.viewitem.summary.section.AbstractDisplayNodesSection.DisplayNodesModel;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class CloudDisplayNodesSection extends AbstractDisplayNodesSection<CloudItem, DisplayNodesModel>
{
	@PlugKey("viewitem.subject.label")
	private static Label LABEL_SUBJECT;
	@PlugKey("viewitem.educationlevel.label")
	private static Label LABEL_EDUCATION_LEVEL;
	@PlugKey("viewitem.license.label")
	private static Label LABEL_LICENSE;
	@PlugKey("viewitem.rights.label")
	private static Label LABEL_RIGHTS;
	@PlugKey("viewitem.contributor.label")
	private static Label LABEL_CONTRIBUTOR;
	@PlugKey("viewitem.publisher.label")
	private static Label LABEL_PUBLISHER;

	@Override
	protected ViewableItem<CloudItem> getViewableItem(SectionInfo info)
	{
		return CloudItemSectionInfo.getItemInfo(info).getViewableItem();
	}

	@Nullable
	@Override
	protected List<AbstractDisplayNodesSection.Entry> getEntries(RenderEventContext context,
		ViewableItem<CloudItem> vitem)
	{
		final List<AbstractDisplayNodesSection.Entry> entries = new ArrayList<>();
		final PropBagEx xml = vitem.getItemxml();

		final String subject = xml.getNode("oer/dc/subject", null);
		if( subject != null )
		{
			final AbstractDisplayNodesSection.Entry entry = new AbstractDisplayNodesSection.Entry(LABEL_SUBJECT,
				new LabelRenderer(new TextLabel(subject)), -1);
			entry.setFullspan(true);
			entries.add(entry);
		}

		final String eduLevel = xml.getNode("oer/dc/terms/educationLevel", null);
		if( eduLevel != null )
		{
			final AbstractDisplayNodesSection.Entry entry = new AbstractDisplayNodesSection.Entry(
				LABEL_EDUCATION_LEVEL, new LabelRenderer(new TextLabel(eduLevel)), -1);
			entry.setFullspan(true);
			entries.add(entry);
		}

		final String license = xml.getNode("oer/eq/license_type", null);
		if( license != null )
		{
			final String licenseUrl = xml.getNode("oer/dc/terms/license", null);
			final SectionRenderable licRenderable;
			if( licenseUrl != null )
			{
				final HtmlLinkState linkState = new HtmlLinkState(new TextLabel(license),
					new SimpleBookmark(licenseUrl));
				linkState.setTarget("_blank");
				licRenderable = new LinkRenderer(linkState);
			}
			else
			{
				licRenderable = new LabelRenderer(new TextLabel(license));
			}

			final AbstractDisplayNodesSection.Entry entry = new AbstractDisplayNodesSection.Entry(LABEL_LICENSE,
				licRenderable, -1);
			entry.setFullspan(false);
			entries.add(entry);
		}

		final String rights = xml.getNode("oer/dc/terms/rights", null);
		if( rights != null )
		{
			final AbstractDisplayNodesSection.Entry entry = new AbstractDisplayNodesSection.Entry(LABEL_RIGHTS,
				new LabelRenderer(new TextLabel(rights)), -1);
			entry.setFullspan(false);
			entries.add(entry);
		}

		final String contributor = xml.getNode("oer/dc/contributor", null);
		if( contributor != null )
		{
			final AbstractDisplayNodesSection.Entry entry = new AbstractDisplayNodesSection.Entry(LABEL_CONTRIBUTOR,
				new LabelRenderer(new TextLabel(contributor)), -1);
			entry.setFullspan(true);
			entries.add(entry);
		}

		final String publisher = xml.getNode("oer/dc/publisher", null);
		if( publisher != null )
		{
			final AbstractDisplayNodesSection.Entry entry = new AbstractDisplayNodesSection.Entry(LABEL_PUBLISHER,
				new LabelRenderer(new TextLabel(publisher)), -1);
			entry.setFullspan(true);
			entries.add(entry);
		}

		return entries;
	}
}
