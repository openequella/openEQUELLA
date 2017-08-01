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

package com.tle.web.portal.standard.renderer;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.dytech.edge.common.Constants;
import com.google.common.io.Closeables;
import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.io.WireFeedInput;
import com.tle.common.util.ByteLimitedInputStream;
import com.tle.common.util.ByteLimitedInputStream.ByteLimitExceededException;
import com.tle.core.filesystem.CachedFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.portal.standard.PortalStandardConstants;
import com.tle.web.portal.standard.editor.RssPortletEditorSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class RssPortletRenderer extends PortletContentRenderer<RssPortletRenderer.RssPortletRendererModel>
{
	protected static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(RssPortletRenderer.class);

	private static Logger LOGGER = Logger.getLogger(RssPortletRenderer.class);

	@Inject
	private FileSystemService fileService;
	@Inject
	private PortalStandardConstants portalSettings;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	private final TagState rssEventsTag = new TagState();

	// Could probably be rolled into the same button, but it's simpler this way
	@Component
	private Button showMoreButton;
	@Component
	private Button showLessButton;

	@Override
	@SuppressWarnings("unchecked")
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		final RssPortletRendererModel model = getModel(context);

		CachedFile rssCache = new CachedFile(portlet.getUuid());
		InputStream cacheStream = null;
		try
		{
			final long timeout = portalSettings.getRssCacheTimeout();
			cacheStream = fileService.retrieveCachedFile(rssCache, PortalStandardConstants.FEED_CACHE_FILE, timeout);
			if( cacheStream == null )
			{
				HttpMethodBase method = new GetMethod(portlet.getConfig());
				method.setFollowRedirects(true);

				HttpClient client = new HttpClient();
				client.executeMethod(method);

				try( InputStream remoteStream = new ByteLimitedInputStream(method.getResponseBodyAsStream(),
					portalSettings.getMaxRssByteSize()) )
				{
					fileService.write(rssCache, PortalStandardConstants.FEED_CACHE_FILE, remoteStream, false);
					cacheStream = fileService.retrieveCachedFile(rssCache, PortalStandardConstants.FEED_CACHE_FILE,
						timeout);
				}
			}

			SAXBuilder saxBuilder = createBuilder();
			final Document doc = saxBuilder.build(cacheStream);

			if( doc.getRootElement().getChildren("tooBig").size() > 0 )
			{
				return tooBig();
			}
			String titleOnly = portlet.getAttribute(RssPortletEditorSection.KEY_TITLEONLY);
			final boolean showDescription = !(titleOnly != null
				&& titleOnly.equals(RssPortletEditorSection.KEY_TITLEONLY));
			final WireFeed feed = new WireFeedInput().build(doc);

			final List<RssEntry> displayEntries = new ArrayList<RssEntry>();
			if( feed instanceof Channel )
			{
				Channel channel = (Channel) feed;

				model.setTitle(
					createEntry(channel.getTitle(), channel.getDescription(), channel.getLink(), channel.getPubDate()));

				int maxCounter = (model.isShowMore() ? portalSettings.getMaxRssResults()
					: Integer.parseInt(portlet.getAttribute(RssPortletEditorSection.KEY_RESULTCOUNT)));

				List<Item> items = channel.getItems();
				for( Item item : items )
				{
					String description = null;
					if( showDescription )
					{
						description = item.getDescription().getValue();
					}
					displayEntries.add(createEntry(item.getTitle(), description, item.getLink(), item.getPubDate()));
					maxCounter--;
					if( maxCounter == 0 )
					{
						break;
					}
				}

			}
			else if( feed instanceof Feed )
			{
				Feed f = (Feed) feed;

				String titleLink = "javascript:void(0);";
				List<Link> altLinks = f.getAlternateLinks();
				for( Link altLink : altLinks )
				{
					if( altLink.getRel() != null && altLink.getRel().equals("self") )
					{
						titleLink = altLink.getHref();
					}
				}
				Content subTitle = f.getSubtitle();
				model.setTitle(createEntry(f.getTitle(), subTitle != null ? subTitle.getValue() : Constants.BLANK,
					titleLink, f.getUpdated()));

				List<Entry> entries = f.getEntries();
				for( Entry entry : entries )
				{
					String entryLink = "javascript:void(0);";
					List<Link> entryLinks = entry.getAlternateLinks();
					for( Link altLink : entryLinks )
					{
						if( altLink.getRel() == null || altLink.getRel().equals("alternate") )
						{
							entryLink = altLink.getHref();
						}
					}
					String description = null;
					if( showDescription )
					{
						description = entry.getSummary().getValue();
					}
					displayEntries.add(createEntry(entry.getTitle(), description, entryLink, entry.getUpdated()));
				}
			}
			model.setEntries(displayEntries);

			return view.createResult("rssportlet.ftl", context);
		}
		catch( ByteLimitExceededException limited )
		{
			LOGGER.warn("RSS portlet attempted to download an RSS feed of size greater than "
				+ portalSettings.getMaxRssByteSize() + " bytes");
			// cache a placeholder so that we don't attempt to keep downloading
			// massive feeds!
			fileService.write(rssCache, PortalStandardConstants.FEED_CACHE_FILE,
				new StringReader("<rss><tooBig>1</tooBig></rss>"), false);

			return tooBig();
		}
		catch( Exception e )
		{
			LOGGER.error("Error displaying RSS portlet [" + portlet.getId() + "]", e);

			// return a 'cannot display portlet' result
			return new SimpleSectionResult(RESOURCES.getString("generic.error.unspecified"));
		}
		finally
		{
			Closeables.close(cacheStream, false);
		}
	}

	private SAXBuilder createBuilder()
	{
		return new SAXBuilder(new XMLReaderJDOMFactory()
		{
			@Override
			public XMLReader createXMLReader() throws JDOMException
			{
				SAXParserFactory fac = SAXParserFactory.newInstance();
				// All JDOM parsers are namespace aware.
				fac.setNamespaceAware(true);
				fac.setValidating(false);
				try
				{
					fac.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					return fac.newSAXParser().getXMLReader();
				}
				catch (ParserConfigurationException | SAXException e)
				{
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean isValidating()
			{
				return false;
			}
		}, null, null);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	// As Thomas would say, "story of my life"
	private SectionRenderable tooBig()
	{
		return new SimpleSectionResult(RESOURCES.getString("rss.error.toobig"));
	}

	private RssEntry createEntry(String title, String description, String url, Date date)
	{
		final HtmlLinkState linkState = new HtmlLinkState(new SimpleBookmark(url));
		final TextLabel label = new TextLabel(title, true);
		linkState.setLabel(label);
		linkState.setTarget("_blank");
		return new RssEntry(new LinkRenderer(linkState), description,
			date != null ? JQueryTimeAgo.timeAgoTag(date) : null);
	}

	@EventHandlerMethod
	public void showMore(SectionInfo info)
	{
		RssPortletRendererModel model = getModel(info);
		model.setShowMore(true);
	}

	@EventHandlerMethod
	public void showLess(SectionInfo info)
	{
		RssPortletRendererModel model = getModel(info);
		model.setShowMore(false);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		UpdateDomFunction showMoreFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("showMore"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id + "rssPortlet");
		showMoreButton.setClickHandler(new OverrideHandler(showMoreFunc));

		UpdateDomFunction showLessFunc = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("showLess"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id + "rssPortlet");
		showLessButton.setClickHandler(new OverrideHandler(showLessFunc));
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "prs";
	}

	@Override
	public Class<RssPortletRendererModel> getModelClass()
	{
		return RssPortletRendererModel.class;
	}

	public TagState getRssEventsTag()
	{
		return rssEventsTag;
	}

	public Button getShowMoreButton()
	{
		return showMoreButton;
	}

	public Button getShowLessButton()
	{
		return showLessButton;
	}

	public static class RssPortletRendererModel
	{
		@Bookmarked(name = "m")
		private boolean showMore;
		private RssEntry title;
		private List<RssEntry> entries;

		public boolean isShowMore()
		{
			return showMore;
		}

		public void setShowMore(boolean showMore)
		{
			this.showMore = showMore;
		}

		public RssEntry getTitle()
		{
			return title;
		}

		public void setTitle(RssEntry title)
		{
			this.title = title;
		}

		public List<RssEntry> getEntries()
		{
			return entries;
		}

		public void setEntries(List<RssEntry> entries)
		{
			this.entries = entries;
		}
	}

	public static class RssEntry
	{
		private final LinkRenderer title;
		private final String description;
		private final SectionRenderable date;

		public RssEntry(LinkRenderer title, String description, SectionRenderable date)
		{
			this.title = title;
			this.description = description;
			this.date = date;
		}

		public LinkRenderer getTitle()
		{
			return title;
		}

		public String getDescription()
		{
			return description;
		}

		public SectionRenderable getDate()
		{
			return date;
		}
	}
}
