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

package com.tle.core.item.convert.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.Language;
import com.tle.beans.entity.LanguageString;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.convert.LanguagesConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.ItemXmlMigrator;
import com.tle.core.institution.convert.XmlMigrator;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public final class LanguageBundleMigration extends XmlMigrator implements ItemXmlMigrator
{
	@Override
	public void execute(final TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final Locale locale = new Locale(CurrentLocale.getLocale().getLanguage());

		List<Language> languages = new ArrayList<Language>();

		Language lang = new Language();
		lang.setLanguage(locale.getLanguage());
		lang.setCountry(locale.getCountry());
		languages.add(lang);

		PropBagEx langXml = new PropBagEx(xmlHelper.writeToXmlString(languages));
		xmlHelper.writeFromPropBagEx(staging, LanguagesConverter.LANGUAGES_FILE, langXml);

		for( Mapping mapping : getXPathMappings() )
		{
			final TemporaryFileHandle entryFile = new SubTemporaryFile(staging, mapping.getDirectory());
			for( String filename : xmlHelper.getXmlFileList(entryFile) )
			{
				PropBagEx xml = xmlHelper.readToPropBagEx(entryFile, filename);

				for( String xpath : mapping.getPaths() )
				{
					for( PropBagEx subXml : xml.iterateAll(xpath) )
					{
						convertToLanguageBundle(subXml, locale);
					}
				}

				if( mapping.getCustomProcessor() != null )
				{
					mapping.getCustomProcessor().process(xml, locale);
				}

				xmlHelper.writeFromPropBagEx(entryFile, filename, xml);
			}
		}
	}

	private List<Mapping> getXPathMappings()
	{
		List<Mapping> map = new ArrayList<Mapping>();

		Mapping.create(map, "filtergroup").addPath("name", "description", "filters/*/name", "filters/*/description");

		Mapping.create(map, "hierarchy").addPath("*/name", "*/keyResourcesSectionName", "*/longDescription",
			"*/shortDescription", "*/subtopicsSectionName", "*/resultsSectionName");

		Mapping.create(map, "itemdefinition").addPath("name", "description", "slow/*/displayNodes/*/title")
			.customProcessor(CONTRIBUTE_PROCESSOR);

		Mapping.create(map, "navigationgroup").addPath("name", "description");
		Mapping.create(map, "navigationlink").addPath("name", "description");

		Mapping.create(map, "powersearch").addPath("name", "description").customProcessor(POWERSEARCH_PROCESSOR);

		Mapping.create(map, "courseinfo").addPath("name", "description");

		Mapping.create(map, "schema").addPath("name", "description");

		Mapping.create(map, "workflow").addPath("name", "description").customProcessor(WORKFLOW_PROCESSOR);

		Mapping.create(map, "federatedsearch").addPath("name", "description");

		Mapping.create(map, "report").addPath("name", "description");

		return map;
	}

	private static void convertToLanguageBundle(final PropBagEx xml, final Locale locale)
	{
		if( xml != null )
		{
			LanguageString langstring = LangUtils.createLanguageString(null, locale, xml.getNode());

			xml.setNode("", "");
			PropBagEx entry = xml.newSubtree("strings/entry");
			entry.setNode("string", langstring.getLocale());

			PropBagEx ls = entry.newSubtree("com.tle.beans.entity.LanguageString");
			ls.setNode("locale", langstring.getLocale());
			ls.setNode("priority", langstring.getPriority());
			ls.setNode("text", langstring.getText());
			ls.setNode("bundle/@reference", "../../../..");
		}
	}

	private static void convertWizardControls(final PropBagEx controlsXml, final Locale locale)
	{
		if( controlsXml != null )
		{
			for( PropBagEx controlXml : controlsXml.iterator() )
			{
				convertWizardControl(controlXml, locale);
			}
		}
	}

	private static void convertWizardControl(final PropBagEx controlXml, final Locale locale)
	{
		convertToLanguageBundle(controlXml.getSubtree("title"), locale);
		convertToLanguageBundle(controlXml.getSubtree("description"), locale);

		for( PropBagEx itemNameXml : controlXml.iterateAll("items/*/name") )
		{
			convertToLanguageBundle(itemNameXml, locale);
		}

		for( PropBagEx groupItemXml : controlXml.iterateAll("groups/*") )
		{
			groupItemXml.setNode("name", groupItemXml.getNode("@name"));
			groupItemXml.deleteNode("@name");

			convertToLanguageBundle(groupItemXml.getSubtree("name"), locale);
			convertWizardControls(groupItemXml.getSubtree("controls"), locale);
		}

		if( controlXml.nodeExists("@noun") )
		{
			controlXml.setNode("noun", controlXml.getNode("@noun"));
			controlXml.deleteNode("@noun");

			convertToLanguageBundle(controlXml.getSubtree("noun"), locale);
		}

		convertWizardControls(controlXml.getSubtree("controls"), locale);
	}

	private static final class Mapping
	{
		private final String directory;
		private final List<String> paths = new ArrayList<String>();

		private CustomProcessor processor;

		public static Mapping create(List<Mapping> map, String directory)
		{
			Mapping result = new Mapping(directory);
			map.add(result);
			return result;
		}

		private Mapping(String directory)
		{
			this.directory = directory;
		}

		public Mapping addPath(String... paths)
		{
			for( String path : paths )
			{
				this.paths.add(path);
			}
			return this;
		}

		public Mapping customProcessor(CustomProcessor processor)
		{
			this.processor = processor;
			return this;
		}

		public String getDirectory()
		{
			return directory;
		}

		public List<String> getPaths()
		{
			return paths;
		}

		public CustomProcessor getCustomProcessor()
		{
			return processor;
		}
	}

	/**
	 * @author Nicholas Read
	 */
	private interface CustomProcessor
	{
		void process(PropBagEx xml, Locale locale);
	}

	private final CustomProcessor CONTRIBUTE_PROCESSOR = new CustomProcessor()
	{
		@Override
		public void process(PropBagEx xml, Locale locale)
		{
			for( PropBagEx pageXml : xml.iterateAll("slow/wizard/pages/*") )
			{
				convertToLanguageBundle(pageXml.getSubtree("title"), locale);
				convertToLanguageBundle(pageXml.getSubtree("error"), locale);
				convertToLanguageBundle(pageXml.getSubtree("inactiveError"), locale);
				convertWizardControls(pageXml.getSubtree("controls"), locale);
			}
		}
	};

	private final CustomProcessor POWERSEARCH_PROCESSOR = new CustomProcessor()
	{
		@Override
		public void process(PropBagEx xml, Locale locale)
		{
			convertWizardControls(xml.getSubtree("wizard/controls"), locale);
		}
	};

	private static final CustomProcessor WORKFLOW_PROCESSOR = new CustomProcessor()
	{
		@Override
		public void process(PropBagEx xml, Locale locale)
		{
			convertWorkflow(xml.getSubtree("root"), locale);
		}

		public void convertWorkflow(PropBagEx xml, Locale locale)
		{
			convertToLanguageBundle(xml.getSubtree("name"), locale);
			convertToLanguageBundle(xml.getSubtree("description"), locale);
			for( PropBagEx childXml : xml.iterateAll("children/*") )
			{
				convertWorkflow(childXml, locale);
			}
		}
	};

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		final Locale locale = new Locale(CurrentLocale.getLocale().getLanguage());
		convertToLanguageBundle(xml.getSubtree("name"), locale);
		convertToLanguageBundle(xml.getSubtree("description"), locale);
		return true;
	}

	@Override
	public void afterMigrate(ConverterParams params, SubTemporaryFile file) throws Exception
	{
		// NOOP
	}

	@Override
	public void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file)
		throws Exception
	{
		// NOOP
	}
}
