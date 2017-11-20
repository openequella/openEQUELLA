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

package com.tle.ims.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.naming.TimeLimitExceededException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.core.util.ims.IMSNavigationHelper;
import com.tle.core.util.ims.beans.IMSItem;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSOrganisation;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.core.util.ims.extension.IMSAttachmentExporter;
import com.tle.core.util.ims.extension.IMSFileExporter;
import com.tle.core.util.ims.extension.IMSManifestExporter;
import com.tle.core.xstream.TLEXStream;
import com.tle.ims.export.IMSExporter.IMSExporterModel;
import com.tle.ims.service.IMSService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.ExportContentSection;
import com.tle.web.viewitem.treeviewer.DownloadPackageViewer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind
public class IMSExporter extends AbstractPrototypeSection<IMSExporterModel> implements HtmlRenderer
{
	private static final Log LOGGER = LogFactory.getLog(IMSExporter.class);

	@PlugKey(value = "export.export")
	private static Label DOWNLOAD_LABEL;
	@PlugKey(value = "export.original")
	private static Label ORIGINAL_LABEL;

	@Inject
	private SchemaService schemaService;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private IMSService imsService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private DownloadPackageViewer viewer;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	private PluginTracker<IMSFileExporter> fileExporters;
	private PluginTracker<IMSManifestExporter> manifestExporters;
	private PluginTracker<IMSAttachmentExporter> attachmentExporters;

	private JSBookmarkModifier exportFunc;

	private TLEXStream getXstream()
	{
		return TLEXStream.instance();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		exportFunc = events.getNamedModifier("doExport");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final IMSExporterModel model = getModel(context);
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);

		ExportContentSection.assertCanExport(itemInfo);

		final HtmlLinkState exportLink = new HtmlLinkState(new BookmarkAndModify(context, exportFunc));
		exportLink.setLabel(DOWNLOAD_LABEL);
		model.setExport(exportLink);

		final ImsAttachment ims = new UnmodifiableAttachments(itemInfo.getItem()).getIms();
		if( ims != null )
		{
			ViewableResource resource = attachmentResourceService.getViewableResource(context,
				itemInfo.getViewableItem(), ims);
			final ViewItemUrl viu = resource.createDefaultViewerUrl();
			viu.add(viewer.getDownloadFor(resource.getMimeType()));

			final HtmlLinkState original = new HtmlLinkState(viu);
			original.setLabel(ORIGINAL_LABEL);
			model.setOriginal(original);
		}

		return viewFactory.createResult("imsexport.ftl", context);
	}

	@EventHandlerMethod
	public void doExport(SectionInfo info) throws IOException
	{
		final ItemSectionInfo itemSectionInfo = ParentViewItemSectionUtils.getItemInfo(info);
		ExportContentSection.assertCanExport(itemSectionInfo);

		final HttpServletResponse response = info.getResponse();
		final Item item = itemSectionInfo.getItem();
		final StagingFile stagingFile = stagingService.createStagingArea();
		final ItemFile itemFile = itemFileService.getItemFile(item);

		if( fileSystemService.fileExists(itemFile) )
		{
			fileSystemService.copyToStaging(itemFile, "", stagingFile, "", true);

			// if this item was originally created from an IMS package the
			// contents need to copied into the root of the export (and delete
			// the already copied expanded zip folder)
			final ImsAttachment ims = new UnmodifiableAttachments(item).getIms();
			if( ims != null )
			{
				fileSystemService.copyToStaging(itemFile, ims.getUrl(), stagingFile, "", true);
				fileSystemService.removeFile(stagingFile, ims.getUrl());
			}
		}

		createManifest(itemSectionInfo, itemFile, stagingFile);
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "inline; filename=export.zip");
		for( IMSFileExporter fileExporter : fileExporters.getBeanList() )
		{
			fileExporter.exportFiles(info, item, stagingFile);
		}

		try
		{
			fileSystemService.zipFile(stagingFile, response.getOutputStream(), ArchiveType.ZIP);
		}
		catch( Exception e )
		{
			String itemUuid = (!Check.isEmpty(item.getUuid())) ? item.getUuid() : " no UUID!";
			LOGGER.error("Failed to zip tiem with UUID: " + itemUuid, e);
		}
		finally
		{
			fileSystemService.removeFile(stagingFile, "");
		}
		info.setRendered();
	}

	private void createManifest(ItemSectionInfo itemSectionInfo, ItemFile itemFile, StagingFile stagingFile)
		throws IOException
	{
		final Item item = itemSectionInfo.getItem();

		PropBagEx metaxml = null;
		ItemDefinition itemdef = item.getItemDefinition();
		String transform = itemdef.getScormPackagingTransformation();
		if( transform != null )
		{
			long schemaId = itemdef.getSchema().getId();
			String transformed = schemaService.transformForExport(schemaId, transform, itemSectionInfo.getItemxml(),
				false);
			metaxml = new PropBagEx(transformed);
		}
		else
		{
			String name = CurrentLocale.get(item.getName(), item.getUuid());
			String description = CurrentLocale.get(item.getDescription(), item.getUuid());
			PropBagEx propBagEx = new PropBagEx();
			propBagEx.createNode("lom/general/title/langstring", name);
			propBagEx.createNode("lom/general/description/langstring", description);
			metaxml = propBagEx.aquireSubtree("/lom");
		}

		PropBagEx manbag = null;
		Attachments attachments = new UnmodifiableAttachments(item);

		ImsAttachment ims = attachments.getIms();
		if( ims != null )
		{
			String packageZip = ims.getUrl();
			InputStream manstream = imsService.getImsManifestAsStream(itemFile, packageZip, true);
			if( manstream != null )
			{
				manbag = new PropBagEx(manstream);
			}
		}

		if( manbag == null )
		{
			IMSManifest man = new IMSManifest();
			List<IMSResource> resources = new ArrayList<IMSResource>();
			List<IMSItem> items = new ArrayList<IMSItem>();
			List<RootItemInfo> rootItemInfos = new ArrayList<RootItemInfo>();
			List<ItemNavigationNode> nodes = item.getTreeNodes();
			if( nodes.isEmpty() )
			{
				noNavNodes(item, attachments, resources, items, rootItemInfos, stagingFile);
			}
			else
			{
				navTreeManifest(item, nodes, resources, items, rootItemInfos, stagingFile);
			}
			man.setResources(resources);

			List<IMSOrganisation> orgs = new ArrayList<IMSOrganisation>();
			boolean anyRootAttachments = false;
			for( RootItemInfo root : rootItemInfos )
			{
				anyRootAttachments |= root.hasAttachments();
			}

			if( !anyRootAttachments )
			{
				for( IMSItem rootItem : items )
				{
					final IMSOrganisation org = new IMSOrganisation();
					org.setTitle(rootItem.getTitle());
					org.setItems(rootItem.getItems());
					orgs.add(org);
				}
			}
			else
			{
				final IMSOrganisation org = new IMSOrganisation();
				org.setTitle(CurrentLocale.get(item.getName()));
				org.setItems(items);
				orgs.add(org);
			}
			man.setOrganisations(orgs);

			for( IMSManifestExporter manifestExporter : manifestExporters.getBeanList() )
			{
				manifestExporter.exportManifest(item, stagingFile, man);
			}

			manbag = getXstream().toPropBag(man, "manifest");
		}

		if( metaxml != null )
		{
			manbag.deleteNode("metadata");
			PropBagEx fullmeta = new PropBagEx("<metadata/>");
			fullmeta.append("", metaxml);
			manbag.insertAt("*", fullmeta);
		}

		OutputStream outputStream = fileSystemService.getOutputStream(stagingFile, "imsmanifest.xml", false);
		outputStream.write(manbag.toString().getBytes("UTF-8"));
		outputStream.close();
	}

	private void noNavNodes(Item item, Attachments attachments, List<IMSResource> resources, List<IMSItem> items,
		List<RootItemInfo> rootItemInfos, StagingFile stagingFile)
	{
		for( IAttachment attachment : attachments )
		{
			final String ident = getIMSResource(item, attachment, resources, null, stagingFile);
			final IMSItem tabNode = new IMSItem();
			tabNode.setTitle(attachment.getDescription());
			tabNode.setIdentifier(ident);
			tabNode.setIdentifierRef(ident);
			items.add(tabNode);

			final RootItemInfo rootInfo = new RootItemInfo();
			rootInfo.setHasAttachments(true);
			rootItemInfos.add(rootInfo);
		}
	}

	private void navTreeManifest(Item item, List<ItemNavigationNode> nodes, List<IMSResource> resources,
		List<IMSItem> items, List<RootItemInfo> rootItemInfos, StagingFile stagingFile)
	{
		ItemNavigationTree itemTree = new ItemNavigationTree(nodes);
		Map<ItemNavigationNode, List<ItemNavigationNode>> childMap = itemTree.getChildMap();
		List<ItemNavigationNode> rootNodes = itemTree.getRootNodes();
		Set<String> attachmentSet = new HashSet<String>();
		recurseTree(item, rootNodes, childMap, items, rootItemInfos, resources, attachmentSet, stagingFile, 0);
	}

	private void recurseTree(Item item, List<ItemNavigationNode> rootNodes,
		Map<ItemNavigationNode, List<ItemNavigationNode>> childMap, List<IMSItem> items,
		List<RootItemInfo> rootItemInfos, List<IMSResource> resources, Set<String> attachmentSet,
		StagingFile stagingFile, int level)
	{
		if( rootNodes == null )
		{
			return;
		}
		for( ItemNavigationNode node : rootNodes )
		{
			String name = node.getName();
			IMSItem itemNode = new IMSItem();
			itemNode.setTitle(name);
			List<ItemNavigationTab> tabs = node.getTabs();
			List<IMSItem> imsTabs = new ArrayList<IMSItem>();
			for( ItemNavigationTab tab : tabs )
			{
				Attachment attachment = tab.getAttachment();
				if( attachment != null )
				{
					IMSItem tabNode = new IMSItem();
					String tabName = tab.getName();
					tabNode.setTitle(tabName);
					String ident = getIMSResource(item, tab.getAttachment(), resources, attachmentSet, stagingFile);
					tabNode.setIdentifier(ident);
					tabNode.setIdentifierRef(ident);
					imsTabs.add(tabNode);
				}
			}

			if( level == 0 )
			{
				final RootItemInfo rootInfo = new RootItemInfo();
				rootInfo.setHasAttachments(imsTabs.size() == 1);
				rootItemInfos.add(rootInfo);
			}
			if( imsTabs.size() == 1 )
			{
				String ident = imsTabs.get(0).getIdentifier();
				itemNode.setIdentifier(ident);
				itemNode.setIdentifierRef(ident);
				imsTabs.clear();
			}

			itemNode.setItems(imsTabs);
			items.add(itemNode);

			recurseTree(item, childMap.get(node), childMap, imsTabs, rootItemInfos, resources, attachmentSet,
				stagingFile, level + 1);
		}
	}

	private String getIMSResource(Item item, IAttachment attachment, List<IMSResource> resources,
		Set<String> attachmentSet, StagingFile imsRoot)
	{
		String attachUuid = attachment.getUuid();
		if( attachmentSet == null || !attachmentSet.contains(attachUuid) )
		{
			boolean handled = false;
			for( IMSAttachmentExporter attachmentExporter : attachmentExporters.getBeanList() )
			{
				if( attachmentExporter.exportAttachment(item, attachment, resources, imsRoot) )
				{
					handled = true;
					break;
				}
			}
			if( !handled )
			{
				IMSResource res = new IMSResource();
				res.setIdentifier(attachUuid);
				res.setHref(URLUtils.urlEncode(attachment.getUrl()));
				resources.add(res);
			}
			if( attachmentSet != null )
			{
				attachmentSet.add(attachUuid);
			}
		}
		return attachUuid;
	}

	@Override
	public Class<IMSExporterModel> getModelClass()
	{
		return IMSExporterModel.class;
	}

	public static class IMSExporterModel
	{
		private HtmlLinkState export;
		private HtmlLinkState original;

		public HtmlLinkState getExport()
		{
			return export;
		}

		public void setExport(HtmlLinkState export)
		{
			this.export = export;
		}

		public HtmlLinkState getOriginal()
		{
			return original;
		}

		public void setOriginal(HtmlLinkState original)
		{
			this.original = original;
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		fileExporters = new PluginTracker<IMSFileExporter>(pluginService, "com.tle.web.ims", "imsFileExporter", "id");
		fileExporters.setBeanKey("class");

		manifestExporters = new PluginTracker<IMSManifestExporter>(pluginService, "com.tle.ims",
			"imsManifestExporter", "id");
		manifestExporters.setBeanKey("class");

		attachmentExporters = new PluginTracker<IMSAttachmentExporter>(pluginService, "com.tle.ims",
			"imsAttachmentExporter", "id");
		attachmentExporters.setBeanKey("class");
	}

	private static class RootItemInfo
	{
		private boolean hasAttachments;

		public boolean hasAttachments()
		{
			return hasAttachments;
		}

		public void setHasAttachments(boolean hasAttachments)
		{
			this.hasAttachments = hasAttachments;
		}
	}
}
