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

package com.tle.mets.metsimport;

import static com.tle.mets.MetsConstants.METS_FILENAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveEntry;
import com.tle.ims.service.IMSService;
import com.tle.mets.importerexporters.AttachmentAdder;
import com.tle.mets.importerexporters.MetsAttachmentImporterExporter;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.PackageTreeBuilder;
import com.tle.web.wizard.PackageInfo;

import edu.harvard.hul.ois.mets.BinData;
import edu.harvard.hul.ois.mets.Div;
import edu.harvard.hul.ois.mets.FContent;
import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.Fptr;
import edu.harvard.hul.ois.mets.MdWrap;
import edu.harvard.hul.ois.mets.Mets;
import edu.harvard.hul.ois.mets.Mptr;
import edu.harvard.hul.ois.mets.Stream;
import edu.harvard.hul.ois.mets.StructMap;
import edu.harvard.hul.ois.mets.TechMD;
import edu.harvard.hul.ois.mets.TransformFile;
import edu.harvard.hul.ois.mets.XmlData;
import edu.harvard.hul.ois.mets.helper.Any;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsException;
import edu.harvard.hul.ois.mets.helper.MetsReader;
import edu.harvard.hul.ois.mets.helper.MetsValidator;
import edu.harvard.hul.ois.mets.helper.PCData;

@Bind
@Singleton
@SuppressWarnings("nls")
public class METSTreeBuilder implements PackageTreeBuilder
{
	private static final Logger LOGGER = Logger.getLogger(METSTreeBuilder.class);

	private PluginTracker<MetsAttachmentImporterExporter> attachmentExporters;

	@Inject
	private IMSService imsService;
	@Inject
	private FileSystemService fileSystem;

	@Override
	public PackageInfo createTree(Item item, FileHandle staging, String packageExtractedFolder,
		String originalPackagePath, String packageName, boolean expand)
	{
		PackageInfo result = new PackageInfo();
		result.setValid(false);

		boolean nonZip = packageName.toLowerCase().endsWith(".xml");
		try( InputStream manifest = imsService.getMetsManifestAsStream(staging, nonZip ? "" : packageExtractedFolder,
			nonZip ? originalPackagePath : METS_FILENAME, true) )
		{
			if( manifest != null )
			{
				Mets mets;
				try
				{
					mets = Mets.reader(new MetsReader(manifest, LOGGER.isDebugEnabled()));

					mets.validate(new MetsValidator());

					// ok we have a valid mets tree, so start building!
					METSTreeHelper treeHelper = new METSTreeHelper(item, staging, packageExtractedFolder, packageName,
						mets.getLABEL());
					treeHelper.populateTree(findStructMaps(mets));

					result.setCreatedAttachments(treeHelper.getCreatedAttachments());
					result.setTitle(treeHelper.title);
					result.setScormVersion(null);
					result.setValid(true);
				}
				catch( MetsException e )
				{
					result.setError(CurrentLocale.get("com.tle.mets.metsimport.notvalidmanifest"));
					LOGGER.warn(CurrentLocale.get("com.tle.mets.metsimport.notvalidmanifest") + ": " + e.getMessage());
					return result;
				}
				// validator can also complain with ClassCastExceptions for
				// invalid schemas!
				catch( ClassCastException e )
				{
					result.setError(CurrentLocale.get("com.tle.mets.metsimport.notvalidmanifest"));
					return result;
				}
			}
			else
			{
				result.setError(CurrentLocale.get("com.tle.mets.metsimport.notloaded"));
			}
		}
		catch( IOException e )
		{
			result.setError(CurrentLocale.get("com.tle.mets.metsimport.notloaded"));
		}

		return result;
	}

	@Override
	public PackageInfo getInfo(SectionInfo info, FileHandle staging, String packageExtractedFolder)
	{
		PackageInfo result = new PackageInfo();
		result.setValid(false);

		boolean nonZip = packageExtractedFolder.toLowerCase().endsWith(".xml");
		try( InputStream manifest = imsService.getMetsManifestAsStream(staging, packageExtractedFolder, nonZip ? null
			: METS_FILENAME, true) )
		{
			if( manifest != null )
			{
				Mets mets = Mets.reader(new MetsReader(manifest, LOGGER.isDebugEnabled()));
				manifest.close();

				mets.validate(new MetsValidator());
				METSTreeHelper treeHelper = new METSTreeHelper(new Item(), staging, packageExtractedFolder, null,
					mets.getLABEL());
				// treeHelper.populateTree(findStructMaps(mets));

				result.setTitle(treeHelper.title);
				result.setScormVersion(null);
				result.setValid(true);
			}
		}
		catch( Exception e )
		{
			LOGGER.debug("Mets error", e);
		}
		return result;
	}

	@Override
	public boolean canHandle(SectionInfo info, FileHandle staging, String packageExtractedFolder)
	{
		boolean nonZip = packageExtractedFolder.toLowerCase().endsWith(".xml");
		if( nonZip && getInfo(info, staging, packageExtractedFolder).isValid() )
		{
			return true;
		}
		try
		{
			ArchiveEntry entry = fileSystem.findZipEntry(staging, packageExtractedFolder, METS_FILENAME, false);
			return (entry != null);
		}
		catch( Exception e )
		{
			return false;
		}
	}

	/**
	 * Assumes you have invoked canHandle with this builder.
	 */
	@Override
	public List<String> determinePackageTypes(SectionInfo info, FileHandle staging, String packageFilepath)
	{
		return Lists.<String> newArrayList("METS");
	}

	@SuppressWarnings("unchecked")
	protected List<StructMap> findStructMaps(Mets mets)
	{
		List<MetsElement> elements = mets.getContent();
		List<StructMap> structs = new ArrayList<StructMap>();
		for( MetsElement elem : elements )
		{
			if( elem instanceof StructMap )
			{
				structs.add((StructMap) elem);
			}
		}
		return structs;
	}

	protected class METSTreeHelper implements AttachmentAdder
	{
		private final Logger HELPER_LOGGER = Logger.getLogger(METSTreeHelper.class);
		protected final Item item;
		protected final Map<String, Integer> kidCount;
		protected final FileHandle staging;
		protected final String packageExtractedFolder;
		@Nullable
		protected final String targetFolder;
		protected String title;
		protected Collection<Attachment> createdAttachments;

		public METSTreeHelper(Item item, FileHandle staging, String packageExtractedFolder,
			@Nullable String targetFolder, String provisionalTitle)
		{
			this.item = item;
			this.staging = staging;
			this.packageExtractedFolder = packageExtractedFolder;
			this.targetFolder = targetFolder;
			this.title = provisionalTitle;
			this.kidCount = new HashMap<String, Integer>();
		}

		public void populateTree(List<StructMap> structs) throws MetsException
		{
			item.setTreeNodes(new ArrayList<ItemNavigationNode>());
			this.createdAttachments = new ArrayList<Attachment>();

			for( StructMap struct : structs )
			{
				populateStructMap(null, struct);
			}
		}

		@SuppressWarnings("unchecked")
		protected void populateStructMap(ItemNavigationNode parent, StructMap struct) throws MetsException
		{
			// may contain | <div>
			List<MetsElement> elements = struct.getContent();
			for( MetsElement elem : elements )
			{
				if( elem instanceof Div ) // edu.harvard.hul.ois.mets.Div
				{
					populateDiv(parent, (Div) elem);
				}
				else
				{
					throw new MetsException(CurrentLocale.get("com.tle.mets.metsimport.notvalidmanifest"));
				}
			}
		}

		@SuppressWarnings("unchecked")
		protected void populateDiv(ItemNavigationNode parent, Div div) throws MetsException
		{
			final ItemNavigationNode node = addNode(parent);
			node.setName(Check.isEmpty(div.getLABEL()) ? div.getID() : div.getLABEL());
			if( Check.isEmpty(title) )
			{
				title = node.getName();
			}

			// may contain | <mptr> | <fptr> | <div>
			List<MetsElement> elements = div.getContent();
			for( MetsElement elem : elements )
			{
				// ItemNavigationNode subnode;
				if( elem instanceof Mptr )
				{
					populateMptr(node, (Mptr) elem);
				}
				else if( elem instanceof Fptr )
				{
					populateFPtr(node, (Fptr) elem);
				}
				else if( elem instanceof Div ) // edu.harvard.hul.ois.mets.Div
				{
					populateDiv(node, (Div) elem);
				}
				else
				{
					throw new MetsException(CurrentLocale.get("com.tle.mets.metsimport.notvalidmanifest"));
				}
			}
		}

		@SuppressWarnings("unchecked")
		protected void populateFPtr(ItemNavigationNode parent, Fptr fptr) throws MetsException
		{
			// may contain | <par> | <seq> | <area>
			Map<String, File> fileMap = fptr.getFILEID();

			for( File file : fileMap.values() )
			{
				populateFile(parent, file);
			}
		}

		protected void populateMptr(ItemNavigationNode parent, Mptr mptr) throws MetsException
		{
			String mId = mptr.getXlinkTitle();
			ItemNavigationNode node = addNode(parent);
			node.setName(mId); // TODO:

			// may contain NO SUB ELEMENTS
		}

		@SuppressWarnings("unchecked")
		protected void populateFile(ItemNavigationNode parent, File file) throws MetsException
		{
			// may contain | <FLocat> | <FContent> | <stream> | <transformFile>
			// | <file>
			List<MetsElement> content = file.getContent();
			for( MetsElement elem : content )
			{
				boolean imported = false;

				final PropBagEx xml = new PropBagEx();

				final Collection adminDatas = file.getADMID().values();

				if( adminDatas.size() > 0 )
				{
					final TechMD techInfo = (TechMD) adminDatas.iterator().next();
					for( Object tech : techInfo.getContent() )
					{
						if( tech instanceof MdWrap )
						{
							for( Object xmlData : ((MdWrap) tech).getContent() )
							{
								if( xmlData instanceof XmlData )
								{
									// xmlData has a list of Any, each with a
									// list of child Any... need to rebuild the
									// XML
									rebuildXml(((XmlData) xmlData).getContent(), xml, 0);
								}
							}
						}
					}
				}

				for( MetsAttachmentImporterExporter exporter : attachmentExporters.getBeanList() )
				{
					if( exporter.canImport(file, elem, xml, parent) )
					{
						exporter.doImport(item, staging, targetFolder, file, elem, xml, parent, this);
						imported = true;
						break;
					}
				}
				if( !imported )
				{
					if( elem instanceof FLocat )
					{
						// link
						FLocat loc = (FLocat) elem;
						LinkAttachment link = new LinkAttachment();
						String nodetitle = (loc.getXlinkTitle() != null ? loc.getXlinkTitle() : loc.getXlinkHref());
						link.setDescription(nodetitle);
						link.setUrl(loc.getXlinkHref());
						addAttachment(parent, link, nodetitle);
					}
					else if( elem instanceof FContent )
					{
						populateFContent(parent, (FContent) elem, file.getMIMETYPE(), file.getOWNERID());
					}
					else if( elem instanceof Stream ) // edu.harvard.hul.ois.mets.Stream
					{
						// TODO:
						HELPER_LOGGER.warn(CurrentLocale.get("com.tle.mets.treebuilder.streamnotsupported"));
					}
					else if( elem instanceof TransformFile ) // edu.harvard.hul.ois.mets.TransformFile
					{
						// TODO:
						HELPER_LOGGER.warn(CurrentLocale.get("com.tle.mets.treebuilder.transformfilenotsupported"));
					}
					else if( elem instanceof File ) // edu.harvard.hul.ois.mets.File
					{
						populateFile(parent, (File) elem);
					}
				}
			}
		}

		private void rebuildXml(List objects, PropBagEx xmlInfo, int level)
		{
			for( Object object : objects )
			{
				if( object instanceof Any )
				{
					Any any = (Any) object;
					if( level == 0 )
					{
						xmlInfo.setNodeName(any.getLocalName());
						rebuildXml(any.getContent(), xmlInfo, level + 1);
						return;
					}

					PropBagEx subInfo = xmlInfo.newSubtree(any.getLocalName());
					rebuildXml(any.getContent(), subInfo, level + 1);
				}
				else if( object instanceof PCData )
				{
					PCData pc = (PCData) object;
					StringBuilder sb = new StringBuilder();
					for( Object d : pc.getContent() )
					{
						if( d instanceof String )
						{
							sb.append(d);
						}
					}
					xmlInfo.setNode("", sb.toString());
				}
			}
		}

		@SuppressWarnings("unchecked")
		protected void populateFContent(ItemNavigationNode parent, FContent fcontent, String mimeType, String filename)
			throws MetsException
		{
			List<MetsElement> content = fcontent.getContent();
			for( MetsElement elem : content )
			{
				if( elem instanceof BinData )
				{
					populateBinData(parent, (BinData) elem, mimeType, filename);
				}

				// what if it isn't a BinData?
			}
		}

		protected void populateBinData(ItemNavigationNode parent, BinData bin, String mimeType, String filename)
			throws MetsException
		{
			for( Object elem : bin.getContent() )
			{
				if( elem instanceof PCData )
				{
					PCData pc = (PCData) elem;
					StringBuilder base64data = new StringBuilder();
					for( Object data : pc.getContent() )
					{
						base64data.append(data);
					}
					byte[] bytes = new Base64().decode(base64data.toString());
					if( Check.isEmpty(filename) )
					{
						filename = UUID.randomUUID().toString();
					}
					try
					{
						FileInfo fileInfo = fileSystem.write(staging, filename, new ByteArrayInputStream(bytes), true);
						filename = fileInfo.getFilename();
						if( fileInfo.getLength() == 0 )
						{
							fileSystem.removeFile(staging, filename);
							throw new MetsException(CurrentLocale.get("com.tle.mets.treebuilder.notzero"));
						}

						FileAttachment attach = new FileAttachment();
						attach.setFilename(filename);
						attach.setDescription(filename);
						attach.setSize(fileInfo.getLength());
						addAttachment(parent, attach, filename);
					}
					catch( IOException io )
					{
						throw new MetsException(CurrentLocale.get("com.tle.mets.treebuilder.couldntwrite"));
					}
				}

				// What if it isn't a PCData?
			}
		}

		@Override
		public void addAttachment(ItemNavigationNode parentNode, Attachment attach, String nodeName)
		{
			item.getAttachments().add(attach);
			createdAttachments.add(attach);

			if( parentNode != null )
			{
				ItemNavigationNode node = addNode(parentNode);
				node.setName(nodeName);

				ItemNavigationTab tab = new ItemNavigationTab();
				tab.setNode(node);
				tab.setAttachment(attach);
				node.getTabs().add(tab);
			}
		}

		private ItemNavigationNode addNode(ItemNavigationNode parent)
		{
			ItemNavigationNode node = new ItemNavigationNode(item);
			node.setParent(parent);
			node.ensureTabs();

			String id = (parent == null ? "" : parent.getUuid());
			Integer count = kidCount.get(id);
			if( count == null )
			{
				count = 0;
			}
			node.setIndex(count);
			kidCount.put(id, count + 1);
			item.getTreeNodes().add(node);

			return node;
		}

		public Collection<Attachment> getCreatedAttachments()
		{
			return createdAttachments;
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		attachmentExporters = new PluginTracker<MetsAttachmentImporterExporter>(pluginService, "com.tle.mets",
			"exporterimporter", "id");
		attachmentExporters.setBeanKey("bean");
	}
}
