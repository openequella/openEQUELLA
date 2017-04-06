package com.tle.web.payment.shop.section.viewitem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.core.payment.beans.store.StoreCatalogueAttachmentBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.web.api.item.interfaces.beans.NavigationNodeBean;
import com.tle.web.api.item.interfaces.beans.NavigationTabBean;
import com.tle.web.api.item.interfaces.beans.NavigationTreeBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewitem.summary.attachment.service.ViewAttachmentWebService;
import com.tle.web.viewurl.AttachmentDetail;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class ShopViewItemSection extends AbstractPrototypeSection<ShopViewItemSection.ShopViewItemModel>
	implements
		HtmlRenderer
{
	private static PluginResourceHelper resources = ResourcesService.getResourceHelper(ShopViewItemSection.class);

	@Inject
	private ViewAttachmentWebService viewAttachmentWebService;
	@Inject
	private ShopService shopService;

	@Component
	private Div div;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ShopViewItemModel model = getModel(context);

		final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(context);
		final StoreCatalogueItemBean item = iinfo.getItem();
		div.addReadyStatements(context, viewAttachmentWebService.createShowDetailsFunction(new ItemId(item.getUuid(),
			item.getVersion()), ".attachments-browse"));

		final Locale locale = CurrentLocale.getLocale();
		final NavigationTreeBean navigation = item.getNavigation();

		I18NStrings nameStrings = item.getNameStrings();
		if( nameStrings == null )
		{
			I18NString nameString = item.getName();
			if( nameString != null )
			{
				model.setName(new TextLabel(nameString.toString()));
			}
			else
			{
				model.setName(new TextLabel(item.getUuid()));
			}
		}
		else
		{
			model
				.setName(new TextLabel(LangUtils.getClosestObjectForLocale(item.getNameStrings().getStrings(), locale)));
		}
		if( item.getDescriptionStrings() != null )
		{
			model.setDescription(new TextLabel(LangUtils.getClosestObjectForLocale(item.getDescriptionStrings()
				.getStrings(), locale)));
		}
		else if( item.getDescription() != null )
		{
			model.setDescription(new TextLabel(item.getDescription().toString()));
		}

		final List<TagRenderer> attachmentRows = Lists.newArrayList();
		final List<StoreCatalogueAttachmentBean> attachments = item.getAttachments();
		if( attachments != null )
		{
			if( navigation == null || navigation.getNodes().size() == 0 )
			{
				int index = 0;
				for( StoreCatalogueAttachmentBean attachment : attachments )
				{
					attachmentRows.add(renderAttachmentRow(context, attachment, null, index, 0));
					index++;
				}
			}
			else
			{
				List<StoreCatalogueAttachmentBean> referenced = Lists.newArrayList();
				buildNavTree(attachments, navigation.getNodes(), attachmentRows, 0, referenced, context);

				if( !item.getNavigation().isHideUnreferencedAttachments() )
				{
					addUnreferencedAttachments(attachments, referenced, attachmentRows, context);
				}
			}
		}
		model.setAttachmentRows(attachmentRows);

		return view.createResult("shop/viewitem.ftl", context);
	}

	private void addUnreferencedAttachments(List<StoreCatalogueAttachmentBean> attachments,
		List<StoreCatalogueAttachmentBean> referenced, List<TagRenderer> attachmentRows, RenderEventContext context)
	{
		for( StoreCatalogueAttachmentBean attachment : attachments )
		{
			if( !referenced.contains(attachment) )
			{
				String parent = attachment.getParentZip();
				if( Check.isEmpty(parent) )
				{
					attachmentRows.add(renderAttachmentRow(context, attachment, null, attachments.indexOf(attachment),
						0));
					continue;
				}

				int level = 1;

				for( StoreCatalogueAttachmentBean referencedAttachment : referenced )
				{
					if( referencedAttachment.getUuid().equals(parent) )
					{
						level = 0;
						continue;
					}
				}
				attachmentRows.add(renderAttachmentRow(context, attachment, null, attachments.indexOf(attachment),
					level));
			}
		}
	}

	// It would be nice to use the code that the actual attachment view uses,
	// but since we only have the beans I think we need to replicate the logic
	// instead :(
	private void buildNavTree(List<StoreCatalogueAttachmentBean> attachments, List<NavigationNodeBean> navNodes,
		List<TagRenderer> attachmentRows, int level, List<StoreCatalogueAttachmentBean> referenced,
		RenderEventContext context)
	{
		for( NavigationNodeBean node : navNodes )
		{
			boolean bogus = node.getTabs().size() == 1 && node.getTabs().get(0).getAttachment() == null;
			boolean multiTab = node.getTabs().size() > 1;
			boolean hasChildren = node.getNodes().size() != 0;

			// Decide whether or not to add a phoney folder row
			if( multiTab || (bogus && hasChildren) )
			{
				TagRenderer li = renderFolderRow(attachmentRows.size(), level);
				attachmentRows.add(li);
			}

			if( bogus )
			{
				buildNavTree(attachments, node.getNodes(), attachmentRows, level + 1, referenced, context);
				continue;
			}

			// Add the actual attachment row(s)
			for( NavigationTabBean tab : node.getTabs() )
			{
				StoreCatalogueAttachmentBean attachment = null;
				for( StoreCatalogueAttachmentBean a : attachments )
				{
					// Pretty clunky to search them all every time
					// Could set up a map but not really worth it
					if( a.getUuid().equals(tab.getAttachment().getUuid()) )
					{
						attachment = a;
						break;
					}
				}

				String name = multiTab ? tab.getName() : node.getName();
				int taglevel = multiTab ? level + 1 : level;
				TagRenderer li = null;

				if( attachment != null )
				{
					li = renderAttachmentRow(context, attachment, name, attachments.indexOf(attachment), taglevel);
					referenced.add(attachment);
				}
				else
				{
					// If it's an EQUELLA item it will be stripped out, put in a
					// phoney node to replace it
					li = renderFolderRow(attachmentRows.size(), level);
				}

				if( hasChildren )
				{
					li.addClass("folder");
				}
				attachmentRows.add(li);
			}
			buildNavTree(attachments, node.getNodes(), attachmentRows, level + 1, referenced, context);
		}
	}

	private TagRenderer renderAttachmentRow(RenderEventContext context, StoreCatalogueAttachmentBean attachment,
		@Nullable String name, int index, int level)
	{
		final HtmlLinkState linkState = new HtmlLinkState();
		final String renderedName = name != null ? name : attachment.getDescription();
		final Label title;
		if( attachment.isPreview() )
		{
			title = new KeyLabel(resources.key("shop.viewitem.attachments.preview"), renderedName);
			linkState.addClass("defaultviewer");
		}
		else
		{
			title = new TextLabel(renderedName);
			linkState.setDisabled(true);
		}
		linkState.setBookmark(getViewAttachmentBookmark(context, attachment));

		final LinkRenderer link = new LinkRenderer(linkState);
		linkState.setId("attRow" + index);
		link.addClass("defaultviewer");
		link.setLabel(title);
		link.setTitle(title);

		final TagRenderer li = viewAttachmentWebService.makeRowRenderer(new TagState("li" + index), level, false);
		li.addClass("attachmentrow");

		// Unfortunately this will come across in the store's language
		List<AttachmentDetail> deets = Lists.newArrayList();
		@SuppressWarnings("unchecked")
		Map<String, String> details = (Map<String, String>) attachment.get("details");
		if( details != null )
		{
			for( Map.Entry<String, String> entry : details.entrySet() )
			{
				deets.add(new AttachmentDetail(new TextLabel(entry.getKey()), new TextLabel(entry.getValue())));
			}
		}
		li.setNestedRenderable(CombinedRenderer.combineMultipleResults(new DivRenderer(link), viewAttachmentWebService
			.makeShowDetailsLinkRenderer(), viewAttachmentWebService.makeAttachmentDetailsRenderer(context, div,
			attachment.getUuid(), index, createThumbRenderer(context, attachment, index), deets, null)));

		return li;
	}

	private TagRenderer renderFolderRow(int index, int level)
	{
		return viewAttachmentWebService.makeRowRenderer(new TagState("li" + index), level, true);
	}

	private Bookmark getViewAttachmentBookmark(SectionInfo info, StoreCatalogueAttachmentBean attachment)
	{
		if( !attachment.isPreview() )
		{
			return null;
		}
		if( attachment.isExternal() )
		{
			@SuppressWarnings("unchecked")
			Map<String, String> links = (Map<String, String>) attachment.get("links");
			return new SimpleBookmark(links.get("view"));
		}
		return new BookmarkAndModify(info, events.getNamedModifier("getAttachmentContents", attachment.getUuid()));
	}

	private SectionRenderable createThumbRenderer(SectionInfo info, StoreCatalogueAttachmentBean attachment, int index)
	{
		final Label description = new TextLabel(attachment.getDescription());
		@SuppressWarnings("unchecked")
		Map<String, String> links = (Map<String, String>) attachment.get("links");
		final ImageRenderer thumbImage = new ImageRenderer(links.get("thumbnail"), description);

		final HtmlLinkState linkState = new HtmlLinkState(getViewAttachmentBookmark(info, attachment));
		linkState.setId("attThmb" + index);
		if( !attachment.isPreview() )
		{
			linkState.setDisabled(true);
		}

		final LinkTagRenderer thumbRenderer = new LinkRenderer(linkState);
		thumbRenderer.setTitle(description);
		thumbRenderer.setLabel(description);
		if( attachment.isPreview() )
		{
			thumbRenderer.ensureClickable();
		}

		thumbRenderer.setNestedRenderable(thumbImage);
		thumbImage.setAlt(description);
		return thumbRenderer;
	}

	@EventHandlerMethod(preventXsrf = false)
	public void getAttachmentContents(SectionInfo info, String attachmentUuid) throws Exception
	{
		final ShopItemSectionInfo iinfo = ShopItemSectionInfo.getItemInfo(info);
		shopService.getPreviewAttachment(iinfo.getStore(), iinfo.getCatUuid(), iinfo.getItem().getUuid(),
			attachmentUuid, info.getRequest(), info.getResponse());
		info.setRendered();
	}

	@Override
	public ShopViewItemModel instantiateModel(SectionInfo info)
	{
		return new ShopViewItemModel();
	}

	public Div getDiv()
	{
		return div;
	}

	@NonNullByDefault(false)
	public static class AttachmentDetailsModel
	{
		private String id;
		private SectionRenderable thumbnail;
		private List<Pair<Label, Label>> details;

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public List<Pair<Label, Label>> getDetails()
		{
			return details;
		}

		public void addDetail(Label key, Label value)
		{
			if( this.details == null )
			{
				this.details = new ArrayList<Pair<Label, Label>>();
			}
			this.details.add(new Pair<Label, Label>(key, value));
		}
	}

	@NonNullByDefault(false)
	public static class ShopViewItemModel
	{
		private Label name;
		private Label description;
		private List<TagRenderer> attachmentRows;
		private SectionRenderable attachmentDetails;

		public Label getName()
		{
			return name;
		}

		public void setName(Label name)
		{
			this.name = name;
		}

		public Label getDescription()
		{
			return description;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}

		public List<TagRenderer> getAttachmentRows()
		{
			return attachmentRows;
		}

		public void setAttachmentRows(List<TagRenderer> attachmentRows)
		{
			this.attachmentRows = attachmentRows;
		}

		public SectionRenderable getAttachmentDetails()
		{
			return attachmentDetails;
		}

		public void setAttachmentDetails(SectionRenderable attachmentDetails)
		{
			this.attachmentDetails = attachmentDetails;
		}
	}
}
