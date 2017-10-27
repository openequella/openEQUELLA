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

package com.tle.core.item.helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageBundle.DeleteHandler;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.LocaleUtils;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.plugins.PluginTracker;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ItemHelper
{
	private static final int DEFAULT_TRUNCATION_LENGTH = 355;

	@Inject
	private ItemDetailsHelper detailsHelper;
	@Inject
	private AttachmentHelper attachmentHelper;
	@Inject
	private BadUrlHelper badUrlHelper;
	@Inject
	private HistoryEventHelper historyEventHelper;
	@Inject
	private CollaboratorsHelper collaboratorsHelper;
	@Inject
	private DRMHelper drmHelper;
	@Inject
	private ModerationHelper moderationHelper;

	private final List<AbstractHelper> defaultHelpers;
	private List<AbstractHelper> displayHelpers;
	private List<AbstractHelper> importHelpers;

	@Inject
	private PluginTracker<AbstractHelper> tracker;

	public ItemHelper()
	{
		defaultHelpers = new ArrayList<AbstractHelper>();
	}

	@PostConstruct
	public void postConstruct()
	{
		init(detailsHelper);
		init(attachmentHelper);
		init(badUrlHelper);
		init(historyEventHelper);
		init(collaboratorsHelper);
		init(drmHelper);
		init(moderationHelper);
	}

	protected <T extends AbstractHelper> void init(final T helper)
	{
		defaultHelpers.add(helper);
	}

	private List<AbstractHelper> getHelpers(ItemHelperSettings settings)
	{
		List<AbstractHelper> h = new ArrayList<AbstractHelper>(12);
		h.add(attachmentHelper);
		h.add(detailsHelper);
		h.add(collaboratorsHelper);
		h.add(drmHelper);
		if( settings.isEverything() )
		{
			h.add(badUrlHelper);
			h.add(historyEventHelper);
			h.add(moderationHelper);
			h.addAll(getDisplayHelpers());
		}
		else
		{
			h.addAll(getImportHelpers());
		}

		return h;
	}

	private Collection<? extends AbstractHelper> getImportHelpers()
	{
		ensureHelpers();
		return importHelpers;
	}

	private List<? extends AbstractHelper> getDisplayHelpers()
	{
		ensureHelpers();
		return displayHelpers;
	}

	private synchronized void ensureHelpers()
	{
		if( importHelpers == null || tracker.needsUpdate() )
		{
			importHelpers = new ArrayList<AbstractHelper>();
			displayHelpers = new ArrayList<AbstractHelper>();
			List<Extension> extensions = tracker.getExtensions();
			for( Extension extension : extensions )
			{
				AbstractHelper helper = tracker.getBeanByParameter(extension, "bean");
				if( extension.getParameter("display").valueAsBoolean() )
				{
					displayHelpers.add(helper);
				}
				if( extension.getParameter("import").valueAsBoolean() )
				{
					importHelpers.add(helper);
				}
			}
		}
	}

	public PropBagEx convertToXml(ItemPack<Item> pack, ItemHelperSettings settings)
	{
		List<AbstractHelper> helpers2 = getHelpers(settings);
		return convertToXml(pack, helpers2);
	}

	public void convertToItemPack(ItemPack<Item> pack, ItemHelperSettings settings)
	{
		// FIXME: magic hard coded strings (was SaveOperation.KEY_PRESAVE etc)
		// In fact, we really shouldn't need to do this at all
		pack.setAttribute("preSaveOperations", new ArrayList<WorkflowOperation>());
		pack.setAttribute("postSaveOperations", new ArrayList<WorkflowOperation>());
		List<AbstractHelper> helpers2 = getHelpers(settings);
		convertToItemPack(pack, helpers2);
	}

	public static class ItemHelperSettings
	{
		private final boolean everything;

		public ItemHelperSettings(boolean everything)
		{
			this.everything = everything;
		}

		public boolean isEverything()
		{
			return everything;
		}
	}

	/**
	 * Gets the titles and values for each display node.
	 */
	public static List<Pair<LanguageBundle, LanguageBundle>> getValuesForDisplayNodes(
		final Collection<DisplayNode> displayNodes, final PropBagEx itemxml)
	{
		final List<Pair<LanguageBundle, LanguageBundle>> results = new ArrayList<Pair<LanguageBundle, LanguageBundle>>();
		for( final DisplayNode displayNode : displayNodes )
		{
			final LanguageBundle values = getDisplayNodeValues(displayNode, itemxml, new DisplayNodeFilter()
			{
				@Override
				public void visit(StringBuilder value)
				{
					final Integer trunc = displayNode.getTruncateLength();
					final int truncateLength = (trunc == null ? DEFAULT_TRUNCATION_LENGTH : trunc);
					if( truncateLength != 0 && value.length() > truncateLength )
					{
						value = value.delete(truncateLength, value.length());
						value.append("...");
					}
				}
			});

			if( values != null && !Check.isEmpty(values.getStrings()) )
			{
				final LanguageBundle titles = displayNode.getTitle();
				results.add(new Pair<LanguageBundle, LanguageBundle>(titles, values));
			}
		}
		return results;
	}

	/**
	 * Get a mapping of locales to concatenated display node values.
	 */
	public static LanguageBundle getDisplayNodeValues(final DisplayNode displayNode, final PropBagEx xml,
		final DisplayNodeFilter filter)
	{
		// Locales to concatenated node values
		final Map<String, StringBuilder> values = new HashMap<String, StringBuilder>();

		final String target = displayNode.getNode();
		if( Check.isEmpty(target) )
		{
			return null;
		}

		// Retrieve all values matching the display node target.
		final Locale locale = CurrentLocale.getLocale();

		if( target.indexOf('@') != -1 )
		{
			appendSingleValue(values, LocaleUtils.getExactKey(locale), displayNode, xml.getNode(target));
		}
		else
		{
			for( final PropBagEx subXml : xml.iterateAll(target) )
			{
				appendToValues(values, subXml, locale, displayNode);
			}
		}

		if( values.isEmpty() )
		{
			return null;
		}

		// Allow for filtering
		if( filter != null )
		{
			for( final StringBuilder value : values.values() )
			{
				filter.visit(value);
			}
		}

		final LanguageBundle bundle = new LanguageBundle();

		for( final Entry<String, StringBuilder> entry : values.entrySet() )
		{
			final String text = entry.getValue().toString();
			if( text.length() > 0 )
			{
				final Locale valueLocale = new Locale(entry.getKey());
				LangUtils.createLanguageString(bundle, valueLocale, text);
			}
		}

		return bundle;
	}

	private interface DisplayNodeFilter
	{
		void visit(StringBuilder value);
	}

	/**
	 * Append values to a mapping of locales to existing concatenated display
	 * node values.
	 */
	private static void appendToValues(final Map<String, StringBuilder> values, final PropBagEx xml,
		final Locale locale, final DisplayNode displayNode)
	{
		final LanguageBundle bundle = LangUtils.getBundleFromXml(xml, locale);
		for( final LanguageString langstring : bundle.getStrings().values() )
		{
			String value = langstring.getText();

			if( displayNode.isDateType() )
			{
				try
				{
					value = new UtcDate(value, Dates.ISO).format(Dates.DATE_ONLY);
				}
				catch( final ParseException ex )
				{
					try
					{
						value = UtcDate.conceptualDate(value).format(Dates.DATE_ONLY);
					}
					catch( ParseException e )
					{
						// ignore
					}
				}
			}
			else if( displayNode.isURLType() )
			{
				value = "<a href=\"" + value + "\">" + Utils.ent(value) + "</a>";
			}
			else if( displayNode.isTextType() )
			{
				value = Utils.ent(value).replaceAll("\n", "\n<br>");
			}
			appendSingleValue(values, langstring.getLocale(), displayNode, value);
		}
	}

	private static void appendSingleValue(final Map<String, StringBuilder> values, final String locale,
		final DisplayNode displayNode, final String value)
	{
		// Try to append it to the existing value
		StringBuilder vb = values.get(locale);
		if( vb == null )
		{
			vb = new StringBuilder(value);
			values.put(locale, vb);
		}
		else
		{
			vb.append(displayNode.getSplitter()).append(value);
		}
	}

	public void updateItemFromXml(final ItemPack<Item> itemPack, DeleteHandler deleteHandler, boolean cleanMetadata)
	{
		final PropBagEx itemXml = itemPack.getXml();
		final Item item = itemPack.getItem();
		final ItemDefinition itemdef = item.getItemDefinition();
		final Schema schema = itemdef.getSchema();

		String namePath = "/item/name";
		String descPath = "/item/description";
		if( schema != null )
		{
			namePath = schema.getItemNamePath();
			descPath = schema.getItemDescriptionPath();
			if( cleanMetadata )
			{
				if( !namePath.equals("/item/name") )
				{
					itemXml.deleteAll("item/name");
				}

				if( !descPath.equals("/item/description") )
				{
					itemXml.deleteAll("item/description");
				}
			}
		}

		item.setName(LanguageBundle.edit(item.getName(), LangUtils.getBundleFromXml(itemXml.getSubtree(namePath)),
			deleteHandler));
		item.setDescription(LanguageBundle.edit(item.getDescription(),
			LangUtils.getBundleFromXml(itemXml.getSubtree(descPath)), deleteHandler));

		// Generate search details
		final SearchDetails details = itemdef.getSearchDetails();
		if( details != null && !Check.isEmpty(details.getDisplayNodes()) )
		{
			final Collection<DisplayNode> nodes = details.getDisplayNodes();
			item.setSearchDetails(ItemHelper.getValuesForDisplayNodes(nodes, convertToXml(itemPack)));
		}
		else
		{
			item.setSearchDetails(null);
		}
	}

	public PropBagEx convertToXml(final ItemPack<Item> pack)
	{
		return convertToXml(pack, defaultHelpers);
	}

	protected PropBagEx convertToXml(final ItemPack<Item> pack, final List<AbstractHelper> helpers)
	{
		final PropBagEx xml = (PropBagEx) pack.getXml().clone();
		final PropBagEx item = xml.aquireSubtree("item");
		final Item bean = pack.getItem();

		for( final AbstractHelper helper : helpers )
		{
			helper.initialise(bean);
			helper.load(item, pack);
		}

		return xml;
	}

	public ItemPack<Item> convertToItemPack(final PropBagEx xml)
	{
		final ItemPack<Item> pack = new ItemPack<>();
		pack.setXml(xml);
		pack.setItem(new Item());
		convertToItemPack(pack, defaultHelpers);
		return pack;
	}

	/**
	 * Both Item and XML must exist in Pack
	 */
	private void convertToItemPack(final ItemPack<Item> pack, final List<AbstractHelper> helpers)
	{
		final PropBagEx xml = pack.getXml().getSubtree("item");
		if( xml != null )
		{
			final Set<String> handled = new HashSet<String>();
			for( final AbstractHelper helper : helpers )
			{
				helper.save(xml, pack, handled);
			}
			for( String h : handled )
			{
				xml.deleteNode(h);
			}
		}
	}

	public static ItemStatus statusStringToEnum(final String status)
	{
		return (status.length() == 0 ? ItemStatus.LIVE : ItemStatus.valueOf(status.toUpperCase()));
	}

	public void updateItemFromXml(ItemPack<Item> itemPack)
	{
		updateItemFromXml(itemPack, new DeleteHandler()
		{
			@Override
			public void deleteBundleObject(Object obj)
			{
				// non-hibernate only
			}
		}, true);
	}
}
