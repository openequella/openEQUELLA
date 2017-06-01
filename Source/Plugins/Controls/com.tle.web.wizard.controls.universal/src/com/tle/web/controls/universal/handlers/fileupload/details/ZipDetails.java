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

package com.tle.web.controls.universal.handlers.fileupload.details;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.util.FileEntry;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ZippingConverter.ZippingProgress;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.DefaultMessageCallback;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;

@Bind
@SuppressWarnings("nls")
public class ZipDetails extends AbstractDetailsEditor<ZipDetails.Model>
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(ZipDetails.class);
	private static final String FOLDER_ZIP_WITH_SLASH = FileSystemService.ZIPS_FOLDER + '/';

	private static final String ZIP_WARNING_PLACEHOLDER = resources.key("handlers.file.zipoptions.zipwarning");
	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/file/zip.js"));
	private static final JSCallable SELECT_ALL = new ExternallyDefinedFunction("zipSelectAll", INCLUDE);
	private static final JSCallable SELECT_FUNCTION = new ExternallyDefinedFunction("zipSelect", INCLUDE);

	private static final ScriptVariable SELECTION_TREE = new ScriptVariable("zipTree");

	private static final String CSS_CLASS_FOLDER = "folder";
	private static final String CSS_CLASS_FILE = "file";

	@Inject
	private FileSystemService fileSystemService;

	@Component
	private Checkbox attachZip;
	@Component
	private MappedBooleans selections;
	@Component
	private Div fileListDiv;
	@PlugKey("handlers.file.zipdetails.link.selectall")
	@Component
	private Link selectAll;
	@PlugKey("handlers.file.zipdetails.link.selectnone")
	@Component
	private Link selectNone;

	@Override
	public Model instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		selectAll.setClickHandler(new OverrideHandler(Js.call_s(SELECT_ALL, true)));
		selectNone.setClickHandler(new OverrideHandler(Js.call_s(SELECT_ALL, false)));
	}

	@Override
	public void initialiseFromUpload(SectionInfo info, UploadedFile uploadedFile, boolean resolved)
	{
		ZipAttachment zipAttachment = new ZipAttachment();
		uploadedFile.setAttachment(zipAttachment);
		String filepath = uploadedFile.getFilepath();
		zipAttachment.setUrl(filepath);
		zipAttachment.setMd5sum(uploadedFile.getMd5());
		zipAttachment.setDescription(uploadedFile.getDescription());
		if( resolved )
		{
			String outpath = FileUploadHandler.getUploadFilepath("_unzipped/");
			String sectionId = getSectionId();
			Model model = (Model) info.getModelForId(sectionId);
			try
			{
				fileSystemService.unzipFile(getFileUploadHandler().getStagingFile(), filepath, outpath,
					model.getZippingProgress());
				uploadedFile.setExtractedPath(outpath);
			}
			catch( IOException e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public SectionRenderable renderDetailsEditor(RenderContext context, DialogRenderOptions renderOptions,
		UploadedFile uploadedFile)
	{
		final Model model = getModel(context);
		model.setShowZipPreview(getFileUploadHandler().getFileSettings().isAllowPreviews());
		setupTreeIds(context, uploadedFile);
		final TreeIds treeIds = model.getTreeIds();
		final ObjectExpression map = new ObjectExpression();
		for( String selectionId : treeIds.childMap.keySet() )
		{
			ObjectExpression nodeInfo = new ObjectExpression();
			nodeInfo.put("folder", treeIds.folderMap.get(selectionId));
			nodeInfo.put("children", treeIds.childMap.get(selectionId));
			map.put(selectionId, nodeInfo);
		}
		fileListDiv.addReadyStatements(context, new AssignStatement(SELECTION_TREE, map));
		model.setEditTitle(new TextLabel(displayName.getValue(context)));

		return viewFactory.createResult("file/file-zipedit.ftl", this);
	}

	private void setupTreeIds(SectionInfo info, UploadedFile file)
	{
		// turn the tree into a flat list of files for display

		final StagingFile staging = getFileUploadHandler().getStagingFile();

		String extractedPath = file.getExtractedPath();
		FileEntry root;
		try
		{
			root = fileSystemService.enumerateTree(staging, extractedPath, null);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
		final List<EntryDisplay> rawDisplays = new ArrayList<EntryDisplay>();
		final TreeIds treeIds = new TreeIds();

		List<FileEntry> zipEntries = root.getFiles();
		List<EntryDisplay> fileDisplays = createDisplaysForFiles(info, treeIds, zipEntries, "", "", 1);
		for( EntryDisplay entryDisplay : fileDisplays )
		{
			rawDisplays.add(entryDisplay);
			treeIds.putChild("_unzipped", entryDisplay.getCheck().getId());
		}
		createDisplaysForFolders(info, treeIds, zipEntries, "", "", 1, rawDisplays);

		final Model model = getModel(info);
		model.setFiles(rawDisplays);
		model.setTreeIds(treeIds);
	}

	private List<EntryDisplay> createDisplaysForFiles(SectionInfo info, TreeIds treeIds, List<FileEntry> files,
		String parentPath, String parentDisplay, int level)
	{
		List<EntryDisplay> displays = Lists.newArrayList();
		for( FileEntry fileEntry : files )
		{
			if( !fileEntry.isFolder() )
			{
				final String selectId = "s" + treeIds.nextId++;
				final EntryDisplay display = new EntryDisplay();
				display.setLevel(level);
				String name = fileEntry.getName();
				display.setName(name);
				display.setFileClass(CSS_CLASS_FILE);
				final String path;
				final String displayPath;
				if( Check.isEmpty(parentPath) )
				{
					path = name;
					displayPath = name;
				}
				else
				{
					path = parentPath + "/" + name;
					displayPath = parentDisplay + " / " + name;
				}
				display.setPath(path);
				display.setDisplayPath(displayPath);
				final HtmlBooleanState state = selections.getBooleanState(info, path);
				state.setId(selectId);
				display.setCheck(state);
				displays.add(display);
			}
		}
		return displays;
	}

	private void createDisplaysForFolders(SectionInfo info, TreeIds treeIds, List<FileEntry> files, String parentPath,
		String parentDisplay, int level, List<EntryDisplay> displays)
	{
		for( FileEntry entry : files )
		{
			if( entry.isFolder() )
			{
				String name = entry.getName();
				final String path;
				final String displayPath;
				if( Check.isEmpty(parentPath) )
				{
					path = name;
					displayPath = name;
				}
				else
				{
					path = parentPath + "/" + name;
					displayPath = parentDisplay + " / " + name;
				}
				List<FileEntry> childFiles = entry.getFiles();
				List<EntryDisplay> childDisplays = createDisplaysForFiles(info, treeIds, childFiles, path, displayPath,
					level + 1);
				if( !childDisplays.isEmpty() )
				{
					final String selectId = "s" + treeIds.nextId++;
					final EntryDisplay display = new EntryDisplay();
					display.setLevel(level);
					display.setName(name);
					display.setFileClass(CSS_CLASS_FOLDER);
					display.setPath(path);
					display.setFolder(true);
					display.setDisplayPath(displayPath);
					final HtmlBooleanState state = new HtmlBooleanState();
					state.setId(selectId);
					state.setClickHandler(Js.handler(Js.call_s(SELECT_FUNCTION, selectId)));
					display.setCheck(state);
					displays.add(display);
					for( EntryDisplay entryDisplay : childDisplays )
					{
						displays.add(entryDisplay);
						treeIds.putChild(selectId, entryDisplay.getCheck().getId());
					}
				}
				createDisplaysForFolders(info, treeIds, childFiles, path, displayPath, level + 1, displays);
			}
		}
	}

	@Override
	public void prepareForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_ZIP);
		ZipAttachment zipAttach = (ZipAttachment) uploadedFile.getAttachment();
		String zipFolder = getZipFolder(zipAttach.getUrl());
		uploadedFile.setExtractedPath(zipFolder);
		List<FileAttachment> existingFiles = getSelectedAttachments(zipAttach.getUuid());
		Set<String> paths = Sets.newHashSet();
		for( FileAttachment file : existingFiles )
		{
			// double check
			if( file.getFilename().startsWith(zipFolder) )
			{
				paths.add(file.getFilename().substring(zipFolder.length()));
			}
		}
		selections.setCheckedSet(info, paths);
	}

	@Override
	public void cleanup(SectionInfo info, UploadedFile uploadedFile)
	{
		// _uploads will be cleaned
	}

	@Override
	public void setupDetailsForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		super.setupDetailsForEdit(info, uploadedFile);
		ZipAttachment attachment = (ZipAttachment) uploadedFile.getAttachment();
		attachZip.setChecked(info, attachment.isAttachZip());
	}

	@Override
	public void saveDetailsToAttachment(SectionInfo info, UploadedFile uploadedFile, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, uploadedFile, attachment);
		ZipAttachment zipAttach = (ZipAttachment) uploadedFile.getAttachment();
		zipAttach.setAttachZip(attachZip.isChecked(info));
	}

	@Override
	public void removeAttachment(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		super.removeAttachment(info, attachment, willBeReplaced);
		if( attachment.getAttachmentType() == AttachmentType.ZIP )
		{
			FileUploadHandler hdlr = getFileUploadHandler();
			StagingFile stagingFile = hdlr.getStagingFile();
			UniversalControlState dialogState = hdlr.getDialogState();
			List<FileAttachment> selectedAttachments = getSelectedAttachments(attachment.getUuid());
			for( FileAttachment fileAttachment : selectedAttachments )
			{
				dialogState.removeAttachment(info, fileAttachment);
				dialogState.removeMetadataUuid(info, fileAttachment.getUuid());
			}
			String zipFilename = attachment.getUrl();
			fileSystemService.removeFile(stagingFile, getZipFolder(zipFilename));
			fileSystemService.removeFile(stagingFile, zipFilename);
		}
	}

	private String getZipFolder(String filename)
	{
		if( filename.startsWith(FOLDER_ZIP_WITH_SLASH) )
		{
			return filename.substring(FOLDER_ZIP_WITH_SLASH.length()) + '/';
		}
		throw new Error("Invalid zip filename:" + filename);
	}

	private List<FileAttachment> getSelectedAttachments(String uuid)
	{
		List<FileAttachment> existingAttachment = Lists.newArrayList();
		Collection<Attachment> attachments = getFileUploadHandler().getDialogState().getAttachments();
		for( Attachment attachment : attachments )
		{
			if( attachment instanceof FileAttachment )
			{
				FileAttachment file = (FileAttachment) attachment;
				if( uuid.equals(attachment.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID)) )
				{
					existingAttachment.add(file);
				}
			}
		}
		return existingAttachment;
	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile ua, Attachment attachment)
	{
		ua.setAttachment(attachment);
		saveDetailsToAttachment(info, ua, attachment);
		String extractedPath = ua.getExtractedPath();
		String zipUuid = attachment.getUuid();
		List<FileAttachment> currentSelections = getSelectedAttachments(zipUuid);
		Map<String, FileAttachment> pathMap = UnmodifiableAttachments.convertToUrlMap(currentSelections);
		Set<String> checkedSet = selections.getCheckedSet(info);
		FileUploadHandler hdlr = getFileUploadHandler();
		StagingFile stagingFile = hdlr.getStagingFile();
		UniversalControlState dialogState = hdlr.getDialogState();
		for( String filepath : checkedSet )
		{
			String fullPath = extractedPath + filepath;
			FileAttachment existing = pathMap.remove(fullPath);
			if( existing == null )
			{
				FileAttachment newAttach = createChildAttachment(stagingFile, fullPath, zipUuid);
				dialogState.addAttachment(info, newAttach);
				dialogState.addMetadataUuid(info, newAttach.getUuid());
			}
		}
		for( FileAttachment orphaned : pathMap.values() )
		{
			dialogState.removeAttachment(info, orphaned);
			dialogState.removeMetadataUuid(info, orphaned.getUuid());
		}
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, String replacementUuid)
	{
		super.commitNew(info, uploadedFile, replacementUuid);

		ZipAttachment zipAttach = (ZipAttachment) uploadedFile.getAttachment();
		if( uploadedFile.isDetailEditing() )
		{
			saveDetailsToAttachment(info, uploadedFile, zipAttach);
		}
		FileUploadHandler hdlr = getFileUploadHandler();
		StagingFile staging = hdlr.getStagingFile();
		String intendedFolder = uploadedFile.getIntendedFilepath();
		String zipFilename = FOLDER_ZIP_WITH_SLASH + intendedFolder;
		zipAttach.setUrl(zipFilename);
		move(staging, uploadedFile.getFilepath(), zipFilename);
		move(staging, uploadedFile.getExtractedPath(), intendedFolder);

		UniversalControlState dialogState = hdlr.getDialogState();

		Set<String> selectedFiles = selections.getCheckedSet(info);
		String zipUuid = uploadedFile.getAttachment().getUuid();
		for( String file : selectedFiles )
		{
			FileAttachment childFile = createChildAttachment(staging, intendedFolder + '/' + file, zipUuid);
			dialogState.addAttachment(info, childFile);
			dialogState.addMetadataUuid(info, childFile.getUuid());
		}
	}

	public FileAttachment createChildAttachment(StagingFile stagingFile, String filepath, String zipUuid)
	{
		FileAttachment file = new FileAttachment();
		file.setFilename(filepath);
		file.setDescription(PathUtils.getFilenameFromFilepath(filepath));
		try
		{
			file.setSize(fileSystemService.fileLength(stagingFile, filepath));
		}
		catch( FileNotFoundException e )
		{
			throw Throwables.propagate(e);
		}
		file.setData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID, zipUuid);
		return file;
	}

	public static class EntryDisplay
	{
		private int level;
		private String name;
		private String path;
		private String displayPath;
		private String fileClass;
		private boolean folder;
		private HtmlBooleanState check;

		public int getLevel()
		{
			return level;
		}

		public void setLevel(int level)
		{
			this.level = level;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getPath()
		{
			return path;
		}

		public void setPath(String path)
		{
			this.path = path;
		}

		public String getDisplayPath()
		{
			return displayPath;
		}

		public void setDisplayPath(String displayPath)
		{
			this.displayPath = displayPath;
		}

		public boolean isFolder()
		{
			return folder;
		}

		public void setFolder(boolean folder)
		{
			this.folder = folder;
		}

		public String getFileClass()
		{
			return fileClass;
		}

		public void setFileClass(String fileClass)
		{
			this.fileClass = fileClass;
		}

		public HtmlBooleanState getCheck()
		{
			return check;
		}

		public void setCheck(HtmlBooleanState check)
		{
			this.check = check;
		}
	}

	/**
	 * Used for the selection checkboxes tree
	 */
	public static class TreeIds
	{
		protected int nextId;
		protected final Map<String, List<String>> childMap;
		protected final Map<String, Boolean> folderMap;

		protected TreeIds()
		{
			childMap = new HashMap<String, List<String>>();
			folderMap = new HashMap<String, Boolean>();
		}

		public List<String> ensureId(String parent, boolean folder)
		{
			List<String> children = childMap.get(parent);
			if( children == null )
			{
				children = new ArrayList<String>();
				childMap.put(parent, children);
			}
			Boolean folderRec = folderMap.get(parent);
			if( folderRec == null )
			{
				folderMap.put(parent, folder);
			}
			return children;
		}

		public void putChild(String parent, String child)
		{
			ensureId(parent, false).add(child);
		}
	}

	public static class Model extends AbstractDetailsEditor.Model
	{
		private ZippingProgress zippingProgress = new ZippingProgress(new DefaultMessageCallback(
			ZIP_WARNING_PLACEHOLDER));
		private boolean showZipPreview;
		private List<EntryDisplay> files;
		private TreeIds treeIds;

		public List<EntryDisplay> getFiles()
		{
			return files;
		}

		public void setFiles(List<EntryDisplay> files)
		{
			this.files = files;
		}

		public TreeIds getTreeIds()
		{
			return treeIds;
		}

		public void setTreeIds(TreeIds treeIds)
		{
			this.treeIds = treeIds;
		}

		public boolean isShowZipPreview()
		{
			return showZipPreview;
		}

		public void setShowZipPreview(boolean showZipPreview)
		{
			this.showZipPreview = showZipPreview;
		}

		public boolean isWarning()
		{
			// sanity check on array length
			if( zippingProgress.getProgress().getValues().length >= 2 )
			{
				Object[] values = zippingProgress.getProgress().getValues();
				return values[2] != null && (!values[2].equals(Constants.BLANK));
			}
			return false;

		}

		public ZippingProgress getZippingProgress()
		{
			return zippingProgress;
		}

		public String getWarningMsg()
		{
			return zippingProgress.getProgress().getMessage();
		}
	}

	public Div getFileListDiv()
	{
		return fileListDiv;
	}

	public Checkbox getAttachZip()
	{
		return attachZip;
	}

	public MappedBooleans getSelections()
	{
		return selections;
	}

	public Link getSelectAll()
	{
		return selectAll;
	}

	public Link getSelectNone()
	{
		return selectNone;
	}
}
