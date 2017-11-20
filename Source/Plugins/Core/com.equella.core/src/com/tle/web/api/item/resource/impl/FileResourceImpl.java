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

package com.tle.web.api.item.resource.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.util.DateUtil;

import com.dytech.edge.common.FileInfo;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.interfaces.FileResource;
import com.tle.web.api.item.interfaces.beans.FileBean;
import com.tle.web.api.item.interfaces.beans.FolderBean;
import com.tle.web.api.item.interfaces.beans.GenericFileBean;
import com.tle.web.api.item.interfaces.beans.RootFolderBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(FileResource.class)
@Singleton
public class FileResourceImpl implements FileResource
{
	private static final List<String> PRIVS_COPY_ITEM_FILES = ImmutableList.of(ItemSecurityConstants.EDIT_ITEM,
		ItemSecurityConstants.NEWVERSION_ITEM, ItemSecurityConstants.CLONE_ITEM, ItemSecurityConstants.REDRAFT_ITEM);

	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemLinkService itemLinkService;

	protected StagingFile getStagingFile(String stagingUuid)
	{
		ensureStaging(stagingUuid);
		return new StagingFile(stagingUuid);
	}

	protected RootFolderBean convertStagingFile(StagingFile stagingFile, boolean nested)
	{
		final RootFolderBean stagingBean = new RootFolderBean();
		stagingBean.setUuid(stagingFile.getUuid());
		stagingBean.setFilename("");
		stagingBean.setParent(null);
		populatedSubFileBeans(stagingFile, "", stagingBean, nested, nested);
		return itemLinkService.addLinks(stagingBean);
	}

	protected void populatedSubFileBeans(StagingFile stagingFile, String filepath, FolderBean folder,
		boolean filesAndFolders, boolean nested)
	{
		try
		{
			final List<FileBean> subFiles = Lists.newArrayList();
			final List<FolderBean> subFolders = Lists.newArrayList();

			final FileEntry[] files = fileSystemService.enumerate(stagingFile, filepath, null);
			final List<FileEntry> filesSorted = Lists.newArrayList(files);
			Collections.sort(filesSorted, new Comparator<FileEntry>()
			{
				@Override
				public int compare(FileEntry o1, FileEntry o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
			for( FileEntry subFile : filesSorted )
			{
				if( subFile.isFolder() )
				{
					subFolders.add(convertFolderEntry(stagingFile, PathUtils.filePath(filepath, subFile.getName()),
						subFile, nested, nested));
				}
				else
				{
					subFiles
						.add(convertFileEntry(stagingFile, PathUtils.filePath(filepath, subFile.getName()), subFile));
				}
			}

			folder.setFiles(subFiles);
			folder.setFolders(subFolders);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	protected FileBean convertFileEntry(StagingFile staging, String filepath, FileEntry file)
	{
		final FileBean subFileBean = new FileBean();
		subFileBean.setFilename(file.getName());
		subFileBean.setSize(file.getLength());
		subFileBean.setParent(getParent(staging, filepath));
		return itemLinkService.addLinks(staging, subFileBean, filepath);
	}

	protected FolderBean convertFolderEntry(StagingFile staging, String filepath, FileEntry folder,
		boolean filesAndFolders, boolean recurse)
	{
		final FolderBean subFileBean = new FolderBean();
		subFileBean.setFilename(folder.getName());
		subFileBean.setParent(getParent(staging, filepath));
		if( filesAndFolders )
		{
			populatedSubFileBeans(staging, filepath, subFileBean, true, recurse);
		}
		return itemLinkService.addLinks(staging, subFileBean, filepath);
	}

	protected GenericFileBean convertFilepath(StagingFile stagingFile, String filepath, boolean nested)
	{
		if( fileSystemService.fileIsDir(stagingFile, filepath) )
		{
			final FolderBean folder = new FolderBean();
			folder.setFilename(PathUtils.getFilenameFromFilepath(filepath));
			folder.setParent(getParent(stagingFile, filepath));
			populatedSubFileBeans(stagingFile, filepath, folder, true, nested);
			return itemLinkService.addLinks(stagingFile, folder, filepath);
		}

		final FileEntry entry = new FileEntry(false);
		entry.setName(PathUtils.getFilenameFromFilepath(filepath));
		try
		{
			entry.setLength(fileSystemService.fileLength(stagingFile, filepath));
		}
		catch( FileNotFoundException e )
		{
			throw Throwables.propagate(e); // can't happen
		}
		return convertFileEntry(stagingFile, filepath, entry);
	}

	protected FolderBean getParent(StagingFile staging, String filepath)
	{
		final String parentPath = PathUtils.getParentFolderFromFilepath(filepath);
		if( parentPath == null )
		{
			return null;
		}

		final FolderBean folder = new FolderBean();
		folder.setFilename(PathUtils.getFilenameFromFilepath(parentPath));
		return itemLinkService.addLinks(staging, folder, parentPath);
	}

	protected void ensureStaging(String stagingUuid)
	{
		if( !stagingService.stagingExists(stagingUuid)
			|| !fileSystemService.fileExists(new StagingFile(stagingUuid), null) )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	protected void ensureFileExists(StagingFile staging, String filepath)
	{
		if( !fileSystemService.fileExists(staging, filepath) )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@Override
	public Response createStaging()
	{
		final StagingFile stagingFile = stagingService.createStagingArea();
		final RootFolderBean stagingBean = convertStagingFile(stagingFile, false);
		return Response.status(Status.CREATED).location(itemLinkService.getFileDirURI(stagingFile, null))
			.entity(stagingBean).build();
	}

	/**
	 * @param itemUuid The uuid of the item to copy files from
	 * @param version The version of the item to copy files from
	 * @return
	 */
	@Override
	public Response createStagingFromItem(String itemUuid, int itemVersion)
	{
		final StagingFile stagingFile = stagingService.createStagingArea();

		if( Check.isEmpty(itemUuid) || itemVersion == 0 )
		{
			throw new WebApplicationException(
				new IllegalArgumentException("uuid and version must be supplied to the copy endpoint"),
				Status.BAD_REQUEST);
		}

		final Item item = itemService.get(new ItemId(itemUuid, itemVersion));
		if( aclService.filterNonGrantedPrivileges(item, PRIVS_COPY_ITEM_FILES).isEmpty() )
		{
			throw new PrivilegeRequiredException(PRIVS_COPY_ITEM_FILES);
		}

		final ItemFile itemFile = itemFileService.getItemFile(item);
		if( !fileSystemService.fileExists(itemFile) )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		fileSystemService.copy(itemFile, stagingFile);

		// final RootFolderBean stagingBean = convertStagingFile(stagingFile,
		// false);
		// was: .entity(stagingBean)
		return Response.status(Status.CREATED).location(itemLinkService.getFileDirURI(stagingFile, null)).build();
	}

	/**
	 * @param stagingUuid
	 * @param filename
	 * @param deep A boolean value
	 * @return
	 */
	@Override
	public GenericFileBean getFileMetadata(String stagingUuid, String filename, Boolean deep)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);
		ensureFileExists(stagingFile, filename);

		boolean nested = (deep == null ? false : deep);

		if( Check.isEmpty(filename) )
		{
			return convertStagingFile(stagingFile, nested);
		}

		return convertFilepath(stagingFile, filename, nested);
	}

	/**
	 * @param stagingUuid
	 * @param parentFolder May or may not be present. This folder must already
	 *            exist.
	 * @param folder Mandatory
	 * @return
	 */
	@Override
	public Response createFolderPost(String stagingUuid, String parentFolder, GenericFileBean folder)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);
		ensureFileExists(stagingFile, parentFolder);

		final String filename = folder.getFilename();
		final String newPath = PathUtils.filePath(parentFolder, filename);

		boolean exists = fileSystemService.fileExists(stagingFile, newPath);
		fileSystemService.mkdir(stagingFile, newPath);

		// was: .entity(convertFile(stagingFile, newPath, false))
		ResponseBuilder resp = Response.status(exists ? Status.OK : Status.CREATED);
		if( !exists )
		{
			resp = resp.location(itemLinkService.getFileDirURI(stagingFile, URLUtils.urlEncode(newPath, false)));
		}
		return resp.build();
	}

	/**
	 * Cannot be used to create files, however it can rename them.
	 * 
	 * @param stagingUuid
	 * @param parentFolder May or may not be present. Will be created if it does
	 *            not exist.
	 * @param foldername The name of the new or existing folder
	 * @param fileOrFolder Mandatory
	 * @return
	 */
	@Override
	public Response createOrRenameFolderPut(String stagingUuid, String parentFolder, String foldername,
		GenericFileBean fileOrFolder) throws IOException
	{
		final String newFoldername = (fileOrFolder != null && !Check.isEmpty(fileOrFolder.getFilename())
			? fileOrFolder.getFilename() : foldername);

		return createOrRenameFolder(stagingUuid, parentFolder, foldername, newFoldername);
	}

	/**
	 * @param stagingUuid
	 * @param parentFolder May or may not be present. Will be created if it does
	 *            not exist.
	 * @param foldername
	 * @return
	 */
	@Override
	public Response createFolderPut(String stagingUuid, String parentFolder, String foldername)
	{
		return createOrRenameFolder(stagingUuid, parentFolder, foldername, foldername);
	}

	private Response createOrRenameFolder(String stagingUuid, String parentFolder, String oldFoldername,
		String newFoldername)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);

		final String oldPath = PathUtils.filePath(parentFolder, oldFoldername);
		final String newPath = PathUtils.filePath(parentFolder, newFoldername);
		boolean created = false;

		if( !newFoldername.equals(oldFoldername) )
		{
			// build folder structure if required
			if( !fileSystemService.fileExists(stagingFile, parentFolder) )
			{
				fileSystemService.mkdir(stagingFile, parentFolder);
			}
			fileSystemService.rename(stagingFile, oldPath, newPath);
			created = true;
		}
		else if( !fileSystemService.fileExists(stagingFile, newPath) )
		{
			fileSystemService.mkdir(stagingFile, newPath);
			created = true;
		}
		// else No-op

		// was: .entity(convertFile(stagingFile, newPath, false)
		ResponseBuilder resp = Response.status(created ? Status.CREATED : Status.OK);
		if( created )
		{
			resp = resp.location(itemLinkService.getFileDirURI(stagingFile, URLUtils.urlEncode(newPath, false)));
		}
		return resp.build();
	}

	/**
	 * Note: cannot be used to delete files. Use the file/content endpoint to
	 * delete a file.
	 * 
	 * @param stagingUuid
	 * @param folder
	 * @return
	 */
	@Override
	public Response deleteFolder(String stagingUuid, String folder)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);
		if( Check.isEmpty(folder) )
		{
			stagingService.removeStagingArea(stagingFile, true);
		}
		else
		{
			ensureFileExists(stagingFile, folder);
			fileSystemService.removeFile(stagingFile, folder);
		}

		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * Respond with the binary data
	 * 
	 * @param stagingUuid
	 * @param filename
	 * @return
	 */
	@Override
	public Response downloadFile(HttpHeaders headers, String stagingUuid, String filepath)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);
		ensureFileExists(stagingFile, filepath);

		try
		{
			final String etag = headers.getHeaderString(HttpHeaders.IF_NONE_MATCH);
			if( etag != null )
			{
				String md5Checksum = fileSystemService.getMD5Checksum(stagingFile, filepath);
				String quotedChecksum = "\"" + md5Checksum + "\"";
				if( Objects.equals(etag, quotedChecksum) )
				{
					return Response.notModified(quotedChecksum).build();
				}
			}
			final String modifiedSince = headers.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE);
			if( modifiedSince != null )
			{
				final Date lastModified = new Date(fileSystemService.lastModified(stagingFile, filepath));
				if( Objects.equals(modifiedSince, DateUtil.formatDate(lastModified)) )
				{
					return Response.notModified().build();
				}
			}

			final InputStream input = fileSystemService.read(stagingFile, filepath);

			final String mimeType = mimeService.getMimeTypeForFilename(filepath);
			return Response.ok().type(mimeType).entity(new StreamingOutput()
			{
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException
				{
					try
					{
						ByteStreams.copy(input, output);
					}
					finally
					{
						Closeables.close(input, false);
					}
				}
			}).build();
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Response uploadOrReplaceFile(String stagingUuid, String filepath, boolean append, String unzipTo, long size,
		String contentType, InputStream binaryData)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);

		boolean creating = !fileSystemService.fileExists(stagingFile, filepath);
		try( InputStream bd = binaryData )
		{
			final FileInfo fileInfo = fileSystemService.write(stagingFile, filepath, bd, append);
			final FileBean fileBean = new FileBean();
			fileBean.setFilename(fileInfo.getFilename());
			fileBean.setParent(getParent(stagingFile, filepath));
			fileBean.setSize(fileInfo.getLength());

			// unzip?
			if( !Strings.isNullOrEmpty(unzipTo) )
			{
				fileSystemService.mkdir(stagingFile, unzipTo);
				fileSystemService.unzipFile(stagingFile, filepath, unzipTo);
			}

			// Returns both the file dir entity and the location of the content
			// so that you can know both locations
			ResponseBuilder resp = Response.status(creating ? Status.CREATED : Status.OK)
				.entity(itemLinkService.addLinks(stagingFile, fileBean, filepath));
			if( creating )
			{
				resp.location(itemLinkService.getFileContentURI(stagingFile, URLUtils.urlEncode(filepath, false)));
			}
			return resp.build();
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Response deleteFile(String stagingUuid, String filepath)
	{
		final StagingFile stagingFile = getStagingFile(stagingUuid);
		ensureFileExists(stagingFile, filepath);

		boolean removed = fileSystemService.removeFile(stagingFile, filepath);
		if( !removed )
		{
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return Response.status(Status.NO_CONTENT).build();
	}

}
