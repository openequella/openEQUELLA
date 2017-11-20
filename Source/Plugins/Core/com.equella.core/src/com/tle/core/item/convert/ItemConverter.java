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

package com.tle.core.item.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.tle.core.xml.service.impl.XmlServiceImpl;
import org.ccil.cowan.tagsoup.AttributesImpl;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.dytech.common.io.FileExtensionFilter;
import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.tle.beans.Institution;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ForeignItemKey;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemXml;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.impl.BaseEntityXmlConverter;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.SubItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.institution.convert.TransactionThreadPool;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.item.convert.WorkflowNodeConverter.WorkflowNodeSupplier;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.dao.ItemLockingDao;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.html.FindHrefHandler;
import com.tle.core.services.html.HrefCallback;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ItemConverter extends AbstractConverter<ItemConverter.ItemConverterInfo>
{
	private static final String ITEM_XML_FILE = "_ITEM/item.xml";
	private static final String ITEMS_EXPORT_IMPORT_FOLDER = "items";
	private static final String SYSTEM_ITEM_FOLDER = "_ITEM";

	@Inject
	private ItemDao itemDao;
	@Inject
	private ItemLockingDao itemLockingDao;
	@Inject
	private EntityRegistry registry;
	@Inject
	private ItemFileService itemFileService;

	private final Random random = new Random();

	private PluginTracker<ItemExtrasConverter> itemExtrasTracker;
	private FileExtensionFilter changeUrlFilter;
	private XStream xstream;

	@PostConstruct
	public void setupChangeUrlExtensions()
	{
		changeUrlFilter = new FileExtensionFilter();
		changeUrlFilter.addExtension("xml");
		changeUrlFilter.addExtension("xslt");
		changeUrlFilter.addExtension("html");
		changeUrlFilter.addExtension("htm");
		changeUrlFilter.addExtension("txt");
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_ITEMS) )
		{
			super.addTasks(type, tasks, params);
		}
	}

	@Override
	public void deleteIt(final TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
						 final String cid)
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(
				"institutions.converter.generic.calculateitems");
		params.setMessageCallback(message);

		doInTransaction(new Runnable()
		{
			@Override
			public void run()
			{
				itemLockingDao.deleteAll();
				itemLockingDao.flush();
				itemLockingDao.clear();
			}
		});

		final List<Long> items = itemDao.enumerateAllIds();
		message.setKey("institutions.converter.items.deletemsg");
		message.setTotal(items.size());
		TransactionThreadPool threadPool = new TransactionThreadPool(this, 4);
		for( final Long id : items )
		{
			threadPool.doInTransaction(new Runnable()
			{
				@Override
				public void run()
				{
					Item item = itemDao.findById(id);
					itemDao.delete(item);
					itemDao.flush();
					itemDao.clear();
					message.incrementCurrent();
				}
			});
			if( threadPool.hasException() )
			{
				break;
			}
		}
		threadPool.close();
	}

	@Override
	public void importIt(TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
						 String cid) throws IOException
	{
		final SubTemporaryFile allImportItemsFolder = new SubTemporaryFile(staging, ITEMS_EXPORT_IMPORT_FOLDER);

		final DefaultMessageCallback message = new DefaultMessageCallback(
				"institutions.converter.generic.calculateitems");
		params.setMessageCallback(message);
		message.setKey("institutions.converter.items.itemsmsg");

		final List<String> entries = xmlHelper.getXmlFileList(allImportItemsFolder);
		message.setTotal(entries.size());

		final Map<Object, Object> sharedState = Collections.synchronizedMap(Maps.newHashMap());
		final Collection<PostReadMigrator<ItemConverterInfo>> migrations = getMigrations(params);
		TransactionThreadPool threadPool = newThreadPool(1);
		for( final String entry : entries )
		{
			threadPool.doInTransaction(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final String dataFolderPath = entry.replace(".xml", Constants.BLANK);

						Item item = new Item();
						DataHolder dh = getXStream().newDataHolder();
						dh.put(WorkflowNodeSupplier.class, new ItemWorkflowNodeSupplier(item));
						xmlHelper.readXmlFile(allImportItemsFolder, entry, getXStream(), item, dh);
						Long origId = item.getId();

						// Fix up some weird cases where Data Created was null
						// in 4.0 exports. May as well check Date Modified too.
						Date c = item.getDateCreated();
						Date m = item.getDateModified();
						// Add some randomness to make the chance of finding an
						// old stale
						// indexed item negligible
						item.setDateForIndex(new Date(Math.abs(random.nextLong() % 1000L)));
						if( c == null )
						{
							c = m != null ? m : new Date();
							item.setDateCreated(c);
						}

						if( m == null )
						{
							item.setDateModified(c);
						}

						// If an item def doesn't have a workflow, then the item
						// should definitely not have any moderation status.
						if( item.getItemDefinition().getWorkflow() == null )
						{
							ModerationStatus moderation = item.getModeration();
							if( moderation != null )
							{
								moderation.getStatuses().clear();
							}
						}

						// data folder
						final SubTemporaryFile importItemFolder = new SubTemporaryFile(allImportItemsFolder,
								dataFolderPath);
						final ItemFile itemFolder = itemFileService.getItemFile(item);
						if( !params.hasFlag(ConverterParams.NO_ITEMSATTACHMENTS) )
						{
							fileSystemService.commitFiles(importItemFolder, itemFolder);
						}
						else
						{
							fileSystemService.copy(importItemFolder, itemFolder);
						}

						// This needs to happen before the PostReadMigrations
						changeHardcodedUrls(params, itemFolder);

						ItemConverterInfo info = new ItemConverterInfo(item, itemFolder, sharedState, params);
						runMigrations(migrations, info);
						// just to initialise it
						item.getNavigationSettings();
						item.setInstitution(institution);
						initialiserService.initialiseClones(item);

						storeXml(params, item, itemFolder, institution);

						itemDao.save(item);
						saveList(item.getComments());
						saveList(item.getAcceptances());
						runExtras(info, getXStream(),
								new SubTemporaryFile(allImportItemsFolder, dataFolderPath + "-extra"), true);
						itemDao.flush();
						itemDao.clear();

						params.getItems().put(origId, item.getId());

						message.incrementCurrent();
					}
					catch( Exception e )
					{
						throw new RuntimeException("Error in " + entry, e);
					}
				}
			});
			if( threadPool.hasException() )
			{
				break;
			}
		}
		threadPool.close();
	}

	private TransactionThreadPool newThreadPool(int threads)
	{
		// FIXME see EQ-2532 come up with a better solution
		return new TransactionThreadPool(this, 1);
	}

	void runExtras(ItemConverterInfo info, XStream xs, SubTemporaryFile extrasFolder, boolean doImport)
			throws IOException
	{
		Map<String, ItemExtrasConverter> convertersMap = itemExtrasTracker.getBeanMap();
		for( ItemExtrasConverter converter : convertersMap.values() )
		{
			if( doImport )
			{
				converter.importExtras(info, xs, extrasFolder);
			}
			else
			{
				converter.exportExtras(info, xs, extrasFolder);
			}
		}
	}

	private void changeHardcodedUrls(ConverterParams params, FileHandle folder) throws IOException
	{
		if( params.getOldServerURL() == null )
		{
			// Not much we can do without it...
			return;
		}

		FileEntry fileTree = fileSystemService.enumerateTree(folder, Constants.BLANK, changeUrlFilter);
		List<String> paths = fileTree.foldToPaths();
		for( final String filePath : paths )
		{
			StringWriter w = new StringWriter();
			try( UnicodeReader reader = new UnicodeReader(fileSystemService.read(folder, filePath), Constants.UTF8) )
			{
				CharStreams.copy(reader, w);
			}

			String infile = w.getBuffer().toString();

			final String newHtml = changeHardcodedUrls(infile, params.getOldServerURL(), params.getCurrentServerURL());
			if( newHtml != null )
			{
				fileSystemService.write(folder, filePath, new StringReader(newHtml), false);
			}
		}
	}

	private String changeHardcodedUrls(String html, final URL oldUrl, final URL newUrl)
	{
		final XMLReader p = new Parser();
		final InputSource s = new InputSource();
		final StringWriter w = new StringWriter();
		final ItemConverterHrefCallback cb = new ItemConverterHrefCallback(oldUrl, newUrl);
		final FindHrefHandler x = new FindHrefHandler(w, cb, true, true);

		p.setContentHandler(x);
		s.setCharacterStream(new StringReader(html));
		try
		{
			p.parse(s);
			if( cb.wasChanged() )
			{
				return w.toString();
			}
			return null;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public static class ItemConverterHrefCallback implements HrefCallback
	{
		private boolean changed;
		private final String oldUrl;
		private final String newUrl;

		protected ItemConverterHrefCallback(URL oldUrl, URL newUrl)
		{
			this.oldUrl = oldUrl.toString();
			this.newUrl = newUrl.toString();
		}

		@Override
		public String textFound(String text)
		{
			String newText = text;
			for( String proto : new String[]{"http", "https"} )
			{
				String protoText = proto + "://";
				int protoIndex = 0;
				protoIndex = newText.toLowerCase().indexOf(protoText, protoIndex);
				while( protoIndex >= 0 )
				{
					// look for end of url
					int urlend = protoIndex;
					while( urlend < newText.length() && !ws(newText.charAt(urlend)) )
					{
						urlend++;
					}

					String url = newText.substring(protoIndex, urlend);
					String newurl = hrefFound("", url, null);
					if( newurl != null )
					{
						newText = newText.replace(url, newurl);
					}

					protoIndex = newText.toLowerCase().indexOf(protoText, urlend);
				}
			}
			return newText;
		}

		private boolean ws(char c)
		{
			return c == ' ' || c == '\t' || c == '\r' || c == '\n';
		}

		@Override
		public String hrefFound(String tag, String attribute, AttributesImpl atts)
		{
			return convert(attribute);
		}

		private String convert(String url)
		{
			try
			{
				final URL u = new URL(url);
				final String foundPath = u.toString();
				if( foundPath.startsWith(oldUrl) )
				{
					changed = true;
					final String replacedPath = foundPath.replace(oldUrl, newUrl).replaceAll("\\+", "%20");
					final String foundUrl = u.toString();
					return foundUrl.replace(foundPath, replacedPath);
				}
				return null;
			}
			catch( IOException e )
			{
				return null;
			}
		}

		public boolean wasChanged()
		{
			return changed;
		}
	}

	protected void storeXml(ConverterParams params, Item item, ItemFile file, Institution institution)
	{
		try( InputStream xmlStream = fileSystemService.read(file, ITEM_XML_FILE) )
		{
			ByteArrayOutputStream xmlData = new ByteArrayOutputStream();
			ByteStreams.copy(xmlStream, xmlData);
			item.setItemXml(new ItemXml(xmlData.toString(Constants.UTF8)));
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
		fileSystemService.removeFile(file, ITEM_XML_FILE);
	}

	private void saveList(List<? extends ForeignItemKey> alist)
	{
		if( alist != null )
		{
			for( ForeignItemKey obj : alist )
			{
				itemDao.saveAny(obj);
			}
		}
	}

	@Override
	public void exportIt(final TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
						 String cid) throws IOException
	{
		final SubTemporaryFile allExportedItemsFolder = new SubTemporaryFile(staging, ITEMS_EXPORT_IMPORT_FOLDER);

		// write out the format details
		xmlHelper.writeExportFormatXmlFile(allExportedItemsFolder, true);

		final boolean attachments = !params.hasFlag(ConverterParams.NO_ITEMSATTACHMENTS);
		final DefaultMessageCallback message = new DefaultMessageCallback(
				"institutions.converter.generic.calculateitems");
		params.setMessageCallback(message);
		List<ItemIdKey> ids = itemDao.listAll(CurrentInstitution.get());
		message.setKey("institutions.converter.items.itemsmsg");
		message.setTotal(ids.size());

		TransactionThreadPool pool = newThreadPool(4);
		final Map<Object, Object> sharedState = Collections.synchronizedMap(Maps.newHashMap());
		for( final ItemIdKey id : ids )
		{
			pool.doInTransaction(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final Item item = itemDao.findByItemId(id);
						final long itemId = item.getId();
						final ItemFile itemFile = itemFileService.getItemFile(item);

						// base folder (may be shared with other items).
						final BucketFile bucketFolder = new BucketFile(allExportedItemsFolder, itemId);
						// data folder (item specific)
						final SubTemporaryFile exportedItemFolder = new SubTemporaryFile(bucketFolder,
								Long.toString(itemId));
						// _ITEM folder (item specific)
						final SubTemporaryFile exportedSystemItemFolder = new SubTemporaryFile(exportedItemFolder,
								SYSTEM_ITEM_FOLDER);

						ItemConverterInfo info = new ItemConverterInfo(item, itemFile, sharedState, params);
						if( fileSystemService.fileExists(itemFile) )
						{
							if( attachments )
							{
								fileSystemService.copyToStaging(itemFile, exportedItemFolder, false);
							}
							else
							{
								fileSystemService.copyToStaging(new SubItemFile(itemFile, SYSTEM_ITEM_FOLDER),
										exportedSystemItemFolder, false);
							}
						}

						xmlHelper.writeXmlFile(bucketFolder, Long.toString(itemId) + ".xml", item, getXStream());
						runExtras(info, getXStream(),
								new SubTemporaryFile(bucketFolder, Long.toString(itemId) + "-extra"), false);

						exportXml(params, item, exportedSystemItemFolder, institution);
						message.incrementCurrent();
						itemDao.clear();
					}
					catch( Exception e )
					{
						throw new RuntimeException("Error in " + id, e);
					}
				}
			});
			if( pool.hasException() )
			{
				break;
			}
		}
		pool.close();
	}

	private synchronized XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = new XmlServiceImpl.ExtXStream(getClass().getClassLoader()) {
				@Override
				protected MapperWrapper wrapMapper(MapperWrapper next) {
					return new HibernateMapper(next);
				}
			};
			xstream.registerConverter(new WorkflowNodeConverter());
			xstream.registerConverter(new BaseEntityXmlConverter(registry));
			xstream.registerConverter(new HibernateProxyConverter());
			xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
			xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
		}
		return xstream;
	}

	void exportXml(ConverterParams params, Item item, SubTemporaryFile exportedSystemItemFolder,
				   Institution institution)
	{
		try
		{
			fileSystemService.write(exportedSystemItemFolder, "item.xml", new StringReader(item.getItemXml().getXml()),
					false);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public ConverterId getConverterId()
	{
		return ConverterId.ITEMS;
	}

	public static class ItemConverterInfo
	{
		public ItemConverterInfo(Item item, FileHandle itemFolder, Map<Object, Object> state, ConverterParams params)
		{
			this.item = item;
			this.fileHandle = itemFolder;
			this.state = state;
			this.params = params;
		}

		private final Item item;
		private final FileHandle fileHandle;
		private final Map<Object, Object> state;
		private final Map<Object, Object> perItem = Maps.newHashMap();
		private final ConverterParams params;

		public ConverterParams getParams()
		{
			return params;
		}

		@SuppressWarnings("unchecked")
		public <T extends Object> T getState(Object key)
		{
			return (T) state.get(key);
		}

		public void setState(Object key, Object value)
		{
			state.put(key, value);
		}

		@SuppressWarnings("unchecked")
		public <T extends Object> T getItemAttribute(Object key)
		{
			return (T) perItem.get(key);
		}

		public void setItemAttribute(Object key, Object value)
		{
			perItem.put(key, value);
		}

		public Item getItem()
		{
			return item;
		}

		public FileHandle getFileHandle()
		{
			return fileHandle;
		}
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
			throws IOException
	{
		throw new Error();
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
			throws IOException
	{
		throw new Error();
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		throw new Error();
	}

	@Inject
	@Override
	public void setPluginService(PluginService pluginService)
	{
		super.setPluginService(pluginService);

		itemExtrasTracker = new PluginTracker<ItemExtrasConverter>(pluginService, "com.tle.core.item.convert",
				"itemExtrasConverter", null, new PluginTracker.ExtensionParamComparator("order"));
		itemExtrasTracker.setBeanKey("class");
	}

	public interface ItemExtrasConverter
	{
		void importExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder) throws IOException;

		void exportExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder) throws IOException;
	}

	public static class ItemWorkflowNodeSupplier implements WorkflowNodeSupplier
	{
		private Map<String, WorkflowNode> map;
		private final Item item;

		public ItemWorkflowNodeSupplier(Item item)
		{
			this.item = item;
		}

		@Override
		public long getIdForNode(String workflowUuid, String uuid)
		{
			if( map == null )
			{
				ItemDefinition def = item.getItemDefinition();
				if( def != null )
				{
					Workflow workflow = def.getWorkflow();
					if( workflow != null )
					{
						map = workflow.getAllNodesAsMap();
						return map.get(uuid).getId();
					}
				}
				return 0;
			}
			else
			{
				return map.get(uuid).getId();
			}
		}
	}

}
