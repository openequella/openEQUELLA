package com.tle.web.itemlist.item;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.i18n.BundleCache;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.HighlightableBundleLabel;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractItemlikeListEntry<I extends IItem<?>> extends AbstractListEntry
	implements
		ItemlikeListEntry<I>
{
	@PlugKey("unselectresult")
	private static Label LABEL_UNSELECT;
	@PlugKey("selectresult")
	private static Label LABEL_SELECT;

	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	protected BundleCache bundleCache;

	/* @LazyNonNull */@Nullable
	private List<ViewableResource> viewableResources;
	/* @LazyNonNull */@Nullable
	private UnmodifiableAttachments attachments;
	/* @LazyNonNull */@Nullable
	private ViewableItem<I> viewableItem;
	private I item;
	private final List<SectionRenderable> extras = new ArrayList<>();
	private SectionRenderable toggle;
	private FreetextResult freetextData;
	private boolean selectable;

	protected abstract ViewableItem<I> createViewableItem();

	protected abstract UnmodifiableAttachments loadAttachments();

	protected abstract Bookmark getTitleLink();

	@Override
	public HtmlLinkState getTitle()
	{
		HtmlLinkState link = new HtmlLinkState(getTitleLabel(), getTitleLink());
		link.setTitle(new ItemNameLabel(item, bundleCache));
		link.setData("itemuuid", item.getUuid());
		link.setData("itemversion", Integer.toString(item.getVersion()));
		link.setData("extensiontype", getViewableItem().getItemExtensionType());
		return link;
	}

	public Label getTitleLabel()
	{
		return new HighlightableBundleLabel(item.getName(), item.getUuid(), bundleCache,
			listSettings.getHilightedWords(), false);
	}

	@Override
	public Label getDescription()
	{
		return new HighlightableBundleLabel(item.getDescription(), "", bundleCache, listSettings.getHilightedWords(),
			true);
	}

	@Override
	public UnmodifiableAttachments getAttachments()
	{
		if( attachments == null )
		{
			attachments = loadAttachments();
		}
		return attachments;
	}

	public void setItem(I item)
	{
		this.item = item;
	}

	public ViewableItem<I> getViewableItem()
	{
		if( viewableItem == null )
		{
			viewableItem = createViewableItem();
		}
		return viewableItem;
	}

	public SectionRenderable getLastModified()
	{
		return getTimeRenderer(item.getDateModified());
	}

	@Override
	public List<ViewableResource> getViewableResources()
	{
		if( viewableResources == null )
		{
			viewableResources = new ArrayList<ViewableResource>();

			final ViewableItem<I> vitem = getViewableItem();
			for( IAttachment attachment : getAttachments() )
			{
				viewableResources.add(attachmentResourceService.getViewableResource(info, vitem, attachment));
			}
		}
		return viewableResources;
	}

	public static <I extends IItem<?>> List<I> getItems(List<? extends ItemlikeListEntry<I>> entries)
	{
		List<I> items = new ArrayList<>();
		for( ItemlikeListEntry<I> entry : entries )
		{
			items.add(entry.getItem());
		}
		return items;
	}

	@Nullable
	public FreetextResult getFreetextData()
	{
		return freetextData;
	}

	public void setFreetextData(FreetextResult freetextData)
	{
		this.freetextData = freetextData;
	}

	@Override
	public I getItem()
	{
		return item;
	}

	@Override
	public void init(RenderContext context, ListSettings<? extends ListEntry> settings)
	{
		super.init(context, settings);
		bundleCache.addBundle(item.getName());
		bundleCache.addBundle(item.getDescription());
	}

	public List<SectionRenderable> getExtras()
	{
		return extras;
	}

	@Override
	public void addExtras(SectionRenderable extra)
	{
		this.extras.add(extra);
	}

	public SectionRenderable getToggle()
	{
		return toggle;
	}

	@Override
	public void setToggle(SectionRenderable toggle)
	{
		this.toggle = toggle;
	}

	@Override
	public Label getSelectLabel()
	{
		return LABEL_SELECT;
	}

	@Override
	public Label getUnselectLabel()
	{
		return LABEL_UNSELECT;
	}

	@Override
	public boolean isSelectable()
	{
		return selectable;
	}

	@Override
	public void setSelectable(boolean selectable)
	{
		this.selectable = selectable;
	}
}
