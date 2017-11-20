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

package com.tle.web.wizard.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.inject.Inject;

import com.google.common.collect.BiMap;
import com.tle.web.sections.render.TextLabel;
import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.FileNode;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Language;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.filesystem.handle.ConversionFile;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.service.MetadataMappingService;
import com.tle.core.office2html.service.Office2HtmlConversionService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.services.FileSystemService;
import com.tle.core.wizard.LERepository;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.wizard.PackageTreeBuilder;
import com.tle.web.wizard.PackageInfo;
import com.tle.web.wizard.WizardMetadataMapper;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.scripting.WizardScriptConstants;
import com.tle.web.wizard.scripting.objects.impl.ControlScriptWrapper;
import com.tle.web.wizard.scripting.objects.impl.PageScriptWrapper;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class WebRepository implements LERepository
{
	private static final Logger LOGGER = Logger.getLogger(WebRepository.class);

	private WizardState state;
	private StagingFile stagingHandle;

	@Inject
	private Office2HtmlConversionService conversionService;
	@Inject
	private FileSystemService fsys;
	@Inject
	private ItemService itemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private WizardService wizardService;
	@Inject
	private QuotaService quotaService;
	@Inject
	private LanguageService languageService;
	@Inject
	private MetadataMappingService mappingService;

	@Inject
	private PluginTracker<PackageTreeBuilder> builderTracker;

	protected Collection<Attachment> packageAttachments = new ArrayList<Attachment>();

	protected List<PackageTreeBuilder> getTreeBuilders()
	{
		return builderTracker.getBeanList();
	}

	/**
	 * @param info
	 * @param packageExtractedFolder This is where the files are currently
	 *            residing. It is NOT (necessarily) a final extracted path. e.g.
	 *            _uploads/_IMS/package.zip NOTE: this will be null in the case
	 *            of non-zip packages
	 * @param originalPackagePath This where the original file was uploaded to
	 *            e.g. _uploads/package.zip or _uploads/mets.xml
	 * @param packageName The name of the package zip folder e.g. package.zip It
	 *            is the folder name of where the files when end up when
	 *            committed (or the filename of a non zip manifest e.g.
	 *            mets.xml)
	 * @return
	 * @throws IOException
	 */
	public PackageInfo createPackageNavigation(SectionInfo info, String packageExtractedFolder,
		String originalPackagePath, String packageName, boolean expand) throws IOException
	{
		clearArtifactAttachments();

		StringBuilder failures = new StringBuilder();
		for( PackageTreeBuilder builder : getTreeBuilders() )
		{
			PackageInfo pinfo = builder.createTree(getItem(), stagingHandle, packageExtractedFolder,
				originalPackagePath, packageName, expand);
			if( pinfo.isValid() )
			{
				addArtifactAttachments(pinfo.getCreatedAttachments());
				return pinfo;
			}

			if( pinfo.getError() != null )
			{
				failures.append(pinfo.getError());
				failures.append(", ");
			}
		}
		throw new RuntimeException(failures.toString());
	}

	/**
	 * Removes all navigation nodes/tabs that point to the attachments
	 * 
	 * @param attachments
	 */
	public void removeNavigationNodes(Collection<Attachment> attachments)
	{
		final Item item = getItem();
		final List<ItemNavigationNode> nodes = item.getTreeNodes();
		if( nodes != null )
		{
			final ItemNavigationTree tree = new ItemNavigationTree(nodes);
			final Map<ItemNavigationNode, List<ItemNavigationNode>> childMap = tree.getChildMap();

			final List<ItemNavigationNode> nodeRemovals = new ArrayList<ItemNavigationNode>();

			dfsNodeTraversal(tree.getRootNodes(), childMap, attachments, nodeRemovals);

			for( ItemNavigationNode removal : nodeRemovals )
			{
				nodes.remove(removal);
			}

			new ItemNavigationTree(nodes).fixNodeIndices();
		}
	}

	private void dfsNodeTraversal(@Nullable List<ItemNavigationNode> nodes,
		Map<ItemNavigationNode, List<ItemNavigationNode>> childMap, Collection<Attachment> attachments,
		List<ItemNavigationNode> nodeRemovals)
	{
		if( nodes == null )
		{
			return;
		}
		final Iterator<ItemNavigationNode> nodeIter = nodes.iterator();
		while( nodeIter.hasNext() )
		{
			final ItemNavigationNode node = nodeIter.next();

			dfsNodeTraversal(childMap.get(node), childMap, attachments, nodeRemovals);

			final List<ItemNavigationTab> tabs = node.getTabs();
			if( tabs != null )
			{
				final Iterator<ItemNavigationTab> tabIter = tabs.iterator();
				while( tabIter.hasNext() )
				{
					final ItemNavigationTab tab = tabIter.next();
					if( attachments.contains(tab.getAttachment()) )
					{
						tabIter.remove();
					}
				}
			}
			// if no tabs left and no children then remove this node
			if( tabs == null || tabs.size() == 0 )
			{
				final List<ItemNavigationNode> childNodes = childMap.get(node);
				if( childNodes == null || childNodes.size() == 0 )
				{
					nodeRemovals.add(node);
					nodeIter.remove();
				}
			}
		}
	}

	@Nullable
	public PackageInfo readPackageInfo(SectionInfo info, String packageExtractedFolder)
	{
		for( PackageTreeBuilder builder : getTreeBuilders() )
		{
			PackageInfo pinfo = builder.getInfo(info, stagingHandle, packageExtractedFolder);
			if( pinfo.isValid() )
			{
				return pinfo;
			}
		}
		return null;
	}

	public List<String> determinePackageTypes(SectionInfo info, String packageFilepath)
	{
		final List<String> types = Lists.newArrayList();
		for( PackageTreeBuilder builder : getTreeBuilders() )
		{
			if( builder.canHandle(info, stagingHandle, packageFilepath) )
			{
				final List<String> packageTypes = builder.determinePackageTypes(info, stagingHandle, packageFilepath);
				if( packageTypes != null )
				{
					types.addAll(packageTypes);
				}
			}
		}
		return types;
	}

	public void clearArtifactAttachments()
	{
		for( Attachment attach : packageAttachments )
		{
			state.getAttachments().removeAttachment(attach);
		}
		packageAttachments.clear(); // TODO: what about multiple IMS uploaders??
	}

	public void addArtifactAttachments(Attachment... attachments)
	{
		addArtifactAttachments(Arrays.asList(attachments));
	}

	public void addArtifactAttachments(Collection<Attachment> attachments)
	{
		final ModifiableAttachments itemAttachments = state.getAttachments();
		for( Attachment attachment : attachments )
		{
			if( !itemAttachments.contains(attachment) )
			{
				itemAttachments.addAttachment(attachment);
			}
		}
		packageAttachments.addAll(attachments);
	}

	public void setState(WizardState state)
	{
		this.state = state;
		String stagingid = state.getStagingId();
		if( stagingid != null )
		{
			stagingHandle = new StagingFile(stagingid);
		}
	}

	@Override
	public FileInfo uploadStream(String filename, InputStream inp, boolean calcMd5) throws IOException
	{
		try
		{
			return fsys.write(stagingHandle, filename, inp, false);
		}
		catch( IOException io )
		{
			throw io;
		}
		catch( Exception ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	@Override
	public void delete(String filename)
	{
		fsys.removeFile(stagingHandle, filename);

		// if there is a thumbnail, delete it as well!
		String thumb = FileSystemService.THUMBS_FOLDER + '/' + filename + FileSystemService.THUMBNAIL_EXTENSION;
		if( fsys.fileExists(stagingHandle, thumb) )
		{
			fsys.removeFile(stagingHandle, thumb);
		}

		// if there is a conversion file, delete THAT as well!
		// TODO: should not be here, this is the converter responsibility
		ConversionFile conv = new ConversionFile(stagingHandle);
		if( fsys.fileExists(conv, filename + ".jpeg") )
		{
			fsys.removeFile(conv, filename);
		}

		String tile = FileSystemService.TILES_FOLDER + '/' + filename;
		if( fsys.fileExists(stagingHandle, tile) )
		{
			fsys.removeFile(stagingHandle, tile);
		}
	}

	@Override
	public InputStream read(String filename)
	{
		try
		{
			return fsys.read(stagingHandle, filename);
		}
		catch( IOException ex )
		{
			throw new RuntimeApplicationException("Error reading file", ex);
		}
	}

	@Override
	public void copy(String filename, String targetFilename)
	{
		fsys.copy(stagingHandle, filename, targetFilename);
	}

	@Override
	public ScriptContext getScriptContext(WizardPage page, HTMLControl control, Map<String, Object> attributes)
	{
		final ScriptContext context = wizardService.createScriptContext(state, page, control, attributes);

		// set the page and control
		context.addScriptObject(WizardScriptConstants.PAGE, new PageScriptWrapper(page));
		context.addScriptObject(WizardScriptConstants.CONTROL, new ControlScriptWrapper(control, page));

		PropBagWrapper xml = context.getXml();
		xml.clearOverrides();
		for( Pair<String, Integer> override : state.getPathOverrides() )
		{
			xml.pushOverride(override.getFirst(), override.getSecond());
		}

		return context;
	}

	@Override
	public void pushPathOverride(String path, int index)
	{
		state.pushPathOverride(new Pair<String, Integer>(path, index));
	}

	@Override
	public void popPathOverride()
	{
		state.popPathOveride();
	}

	@Override
	public boolean isEditable()
	{
		return state.isEditable();
	}

	@Override
	public boolean isExpert()
	{
		return false;
	}

	@Override
	public String getWebUrl()
	{
		return institutionService.getInstitutionUrl().toString();
	}

	@Override
	public String getWizid()
	{
		return state.getWizid();
	}

	@Override
	public String getStagingid()
	{
		return state.getStagingId();
	}

	@Override
	public boolean checkDataUniqueness(String xpath, ImmutableCollection<String> values, boolean canAccept)
	{
		return wizardService.checkDuplicateXpathValue(state, xpath, values, canAccept);
	}

	@Override
	public void checkLinkAttachmentUniqueness(String[] urls)
	{
		wizardService.checkDuplicateUrls(state, urls);
	}

	@Override
	public FileInfo unzipFile(String zipfile, String targetFolder, boolean ignoreZipError)
	{
		try
		{
			return fsys.unzipFile(stagingHandle, zipfile, targetFolder);
		}
		catch( IOException ex )
		{
			throw new RuntimeApplicationException("Error unzipping file", ex);
		}
	}

	@Override
	public ModifiableAttachments getAttachments()
	{
		return state.getAttachments();
	}

	@Override
	public String getUserUUID()
	{
		return CurrentUser.getUserID();
	}

	@Override
	public boolean isConvertibleToHtml(String filename)
	{
		try
		{
			return conversionService.isConvertibleToHtml(filename);
		}
		catch( Exception ex )
		{
			LOGGER.error("Error", ex);
			return false;
		}
	}

	@Override
	public PropBagEx getItemBag()
	{
		return state.getItemxml();
	}

	@Override
	public String getWizardName()
	{
		return state.getWizard().getName();
	}

	@Override
	public void spawnBrowser(String link)
	{
		// nothing
	}

	@Override
	public FileNode getFileTree(String path)
	{
		return wizardService.getFileTree(state, path);
	}

	public PropBagEx getItemXML(ItemId key)
	{
		return itemService.getItemPack(key).getXml();
	}

	public void selectTopLevelFilesAsAttachments()
	{
		try
		{
			wizardService.selectTopLevelFilesAsAttachments(state);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public void checkQuota(AbstractWebControl<?> action, boolean recheck)
	{
		QuotaExceededException e = state.getQuotaExceededException();
		if( recheck )
		{
			try
			{
				quotaService.checkQuotaAndReturnNewItemSize(state.getItem(), stagingHandle);
				e = null;
			}
			catch( QuotaExceededException ex )
			{
				e = ex;
			}
		}
		state.setQuotaExceededException(e);
		if( e != null )
		{
			action.setInvalid(true, new TextLabel(e.getMessage()));
		}
	}

	public List<FileNode> linearizeFileTree(List<FileNode> files, String prefix)
	{
		return wizardService.linearizeFileTree(files, prefix);
	}

	public ItemDefinition getItemdefinition()
	{
		return state.getItemDefinition();
	}

	public Schema getSchema()
	{
		return state.getSchema();
	}

	public ItemPack getItemPack()
	{
		return state.getItemPack();
	}

	public String getItemName(ItemId itemId)
	{
		return CurrentLocale.get(itemService.getItemNames(Collections.singleton(itemId)).get(itemId));
	}

	@Override
	public Item getItem()
	{
		return state.getItem();
	}

	@Override
	public Locale getLocale()
	{
		return CurrentLocale.getLocale();
	}

	@Override
	public List<Language> getLanguages()
	{
		return languageService.getLanguages();
	}

	public WizardState getState()
	{
		return state;
	}

	public Collection<Attachment> getPackageAttachments()
	{
		return packageAttachments;
	}

	@Override
	public boolean fileExists(String file)
	{
		return fsys.fileExists(stagingHandle, file);
	}

	public long fileLength(String filename) throws FileNotFoundException
	{
		return fsys.fileLength(stagingHandle, filename);
	}

	public Bookmark getFileURL(String path)
	{
		return getViewableItem().createStableResourceUrl(path);
	}

	public ViewableItem getViewableItem()
	{
		return wizardService.createViewableItem(state);
	}

	@Override
	public void updateMetadataMapping()
	{
		WizardMetadataMapper mapper = state.getWizardMetadataMapper();
		if( mapper.isMapNow() )
		{
			state.setWizardMetadataMapper(new WizardMetadataMapper());
			String packageExtractedFolder = mapper.getPackageExtractedFolder();
			PropBagEx itemxml = getItemBag();
			ItemDefinition collection = getItemdefinition();
			if( packageExtractedFolder != null )
			{
				mappingService.mapPackage(collection, stagingHandle, packageExtractedFolder, itemxml);
			}
			List<String> files = mapper.getHtmlMappedFiles();
			if( !Check.isEmpty(files) )
			{
				mappingService.mapHtmlTags(collection, stagingHandle, files, itemxml);
			}
		}
	}

	public boolean isArchive(String filename)
	{
		return fsys.isArchive(stagingHandle, filename);
	}

	@Override
	public Object getThreadLock()
	{
		return wizardService.getThreadLock();
	}

	@Override
	public synchronized boolean registerFilename(UUID id, String filename)
	{
		BiMap<String, UUID> inverse = state.getRegisteredFilenames().inverse();
		if (inverse.containsKey(filename))
		{
			return inverse.get(filename).equals(id);
		}
		inverse.put(filename, id);
		return true;
	}

	@Override
	public synchronized void unregisterFilename(UUID id)
	{
		state.getRegisteredFilenames().remove(id);
	}
}
