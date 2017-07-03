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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.jboss.resteasy.util.DateUtil;

import com.dytech.edge.common.FileInfo;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.web.api.interfaces.beans.BlobBean;
import com.tle.web.api.staging.interfaces.StagingResource;
import com.tle.web.api.staging.interfaces.beans.MultipartBean;
import com.tle.web.api.staging.interfaces.beans.MultipartCompleteBean;
import com.tle.web.api.staging.interfaces.beans.PartBean;
import com.tle.web.api.staging.interfaces.beans.StagingBean;
import com.tle.web.remoting.rest.service.UrlLinkService;

@SuppressWarnings("nls")
@Bind(StagingResource.class)
@Singleton
public class StagingResourceImpl implements StagingResource
{
	private static final Logger LOGGER = Logger.getLogger(StagingResourceImpl.class);

	@Inject
	private MimeTypeService mimeService;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private UrlLinkService urlLinkService;

	@Override
	public Response createStaging()
	{
		final StagingFile stagingFile = stagingService.createStagingArea();
		// Need compatibility with EPS endpoint :(
		return Response.created(stagingUri(stagingFile.getUuid())).header("x-eps-stagingid", stagingFile.getUuid())
			.build();
	}

	@Override
	public StagingBean getStaging(UriInfo uriInfo, String stagingUuid)
	{
		StagingFile stagingFile = getStagingFile(stagingUuid);

		try
		{
			FileEntry base = fileSystemService.enumerateTree(stagingFile, null, null);
			List<BlobBean> blobs = Lists.newArrayList();
			for( FileEntry fileEntry : base.getFiles() )
			{
				buildBlobBeans(stagingFile, stagingUuid, blobs, fileEntry, "");
			}
			Collections.sort(blobs, new Comparator<BlobBean>()
			{
				@Override
				public int compare(BlobBean o1, BlobBean o2)
				{
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
			URI directUrl = stagingUri(stagingUuid);

			StagingBean stagingBean = new StagingBean();
			stagingBean.setFiles(blobs);
			stagingBean.setUuid(stagingUuid);
			stagingBean.setDirectUrl(directUrl.toString());
			Map<String, URI> links = Maps.newLinkedHashMap();
			links.put("self", directUrl);
			stagingBean.set("links", links);
			return stagingBean;
		}
		catch( IOException e )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	private void buildBlobBeans(FileHandle fileHandle, String stagingUuid, List<BlobBean> blobs, FileEntry entry,
		String currentPath)
	{
		// Folders are not listed
		if( entry.isFolder() )
		{
			for( FileEntry subEntry : entry.getFiles() )
			{
				buildBlobBeans(fileHandle, stagingUuid, blobs, subEntry,
					PathUtils.filePath(currentPath, entry.getName()));
			}
		}
		else
		{
			final BlobBean blobBean = new BlobBean();
			final String filename = entry.getName();
			final String filePath = PathUtils.filePath(currentPath, filename);
			try
			{
				String md5CheckSum = fileSystemService.getMD5Checksum(fileHandle, filePath);
				blobBean.setEtag("\"" + md5CheckSum + "\"");
			}
			catch( IOException e )
			{
				// Whatever
			}
			blobBean.setName(filePath);
			blobBean.setSize(entry.getLength());
			blobBean.setContentType(mimeService.getMimeTypeForFilename(filename));
			final Map<String, URI> links = new HashMap<>();
			links.put("self",
				urlLinkService.getMethodUriBuilder(StagingResource.class, "getFile").build(stagingUuid, filePath));
			blobBean.set("links", links);
			blobs.add(blobBean);
		}
	}

	@Override
	public Response headFile(String uuid, String filepath)
	{
		try
		{
			ensureFileExists(getStagingFile(uuid), filepath);
			FileInfo fileInfo = fileSystemService.getFileInfo(new StagingFile(uuid), filepath);
			if( fileInfo == null )
			{
				return Response.status(Status.NOT_FOUND).build();
			}
			return makeResponseHeaders(uuid, filepath).build();
		}
		catch( IOException io )
		{
			LOGGER.error("Error getting HEAD for file", io);
			return Response.serverError().build();
		}
	}

	@Override
	public Response getFile(HttpHeaders headers, String uuid, String filepath)
	{
		final StagingFile stagingFile = getStagingFile(uuid);
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
			final ResponseBuilder responseBuilder = makeResponseHeaders(uuid, filepath);
			return responseBuilder.entity(new StreamingOutput()
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
	public Response deleteFile(String stagingUuid, String filepath, String uploadId) throws IOException
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

	@Override
	public Response deleteStaging(String uuid) throws IOException
	{
		StagingFile stagingFile = getStagingFile(uuid);
		stagingService.removeStagingArea(stagingFile, true);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Override
	public Response completeMultipart(String uuid, String filepath, String uploadId, MultipartCompleteBean completion)
		throws IOException
	{
		StagingFile stagingFile = getStagingFile(uuid);
		List<PartBean> parts = completion.getParts();
		int[] partNumbers = new int[parts.size()];
		String[] etags = new String[parts.size()];
		int i = 0;
		for( PartBean partBean : parts )
		{
			partNumbers[i] = partBean.getPartNumber();
			etags[i++] = partBean.getEtag();
		}
		String folderPath = "multipart/" + uploadId;

		if( !fileSystemService.fileExists(stagingFile, folderPath) )
		{
			throw new BadRequestException("Multipart upload doesn't exist: " + uploadId);
		}

		File folder = fileSystemService.getExternalFile(stagingFile, folderPath);
		for( int partNumber : partNumbers )
		{
			fileSystemService.write(stagingFile, filepath,
				fileSystemService.read(stagingFile, folder + "/" + Integer.toString(partNumber)), true);
		}
		fileSystemService.removeFile(stagingFile, folderPath);
		ResponseBuilder resp = Response.ok();
		return resp.build();
	}

	@Override
	public MultipartBean startMultipart(String uuid, String filepath, Boolean uploads)
	{
		if( uploads == null )
		{
			throw new BadRequestException("Must use PUT for uploading files");
		}
		StagingFile stagingFile = getStagingFile(uuid);
		String uploadId = UUID.randomUUID().toString();
		String folderPath = "multipart/" + uploadId;
		ensureMultipartDir(stagingFile);
		try
		{
			fileSystemService.mkdir(stagingFile, folderPath);
			return new MultipartBean(uploadId);
		}
		catch( Exception e )
		{
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	private void ensureMultipartDir(StagingFile handle)
	{
		try
		{
			if( !fileSystemService.fileExists(handle, "multipart") )
			{
				fileSystemService.mkdir(handle, "multipart");
			}
		}
		catch( Exception e )
		{
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response putFile(String uuid, String filepath, InputStream data, String unzipTo, String copySource,
		int partNumber, String uploadId, long size, String contentType) throws IOException
	{
		final StagingFile stagingFile = getStagingFile(uuid);
		if( fileSystemService.fileExists(stagingFile, filepath) )
		{
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if( !Strings.isNullOrEmpty(copySource) )
		{
			fileSystemService.copy(stagingFile, copySource, stagingFile, filepath);
			String md5 = fileSystemService.getMD5Checksum(stagingFile, filepath);
			return Response.ok().header(HttpHeaders.ETAG, "\"" + md5 + "\"").location(stagingUri(uuid, filepath))
				.build();
		}

		try( InputStream bd = data )
		{
			checkValidContentType(contentType);
			FileInfo info = fileSystemService.write(stagingFile, filepath, bd, false, true);

			if( !Check.isEmpty(unzipTo) )
			{
				fileSystemService.mkdir(stagingFile, unzipTo);
				info = fileSystemService.unzipFile(stagingFile, filepath, unzipTo);
			}

			return Response.ok().header(HttpHeaders.ETAG, "\"" + info.getMd5CheckSum() + "\"")
				.location(stagingUri(uuid, filepath)).build();
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private void ensureFileExists(StagingFile staging, String filepath)
	{
		if( !fileSystemService.fileExists(staging, filepath) )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	private void checkValidContentType(String contentType)
	{
		if( contentType != null && contentType.startsWith("multipart/form-data") )
		{
			throw new BadRequestException("Don't use multipart encoding to upload files, upload the file directly");
		}
	}

	private StagingFile getStagingFile(String stagingUuid)
	{
		ensureStagingExists(stagingUuid);
		return new StagingFile(stagingUuid);
	}

	private ResponseBuilder makeResponseHeaders(String uuid, String filepath) throws IOException
	{
		ResponseBuilder builder = Response.ok();
		StagingFile handle = new StagingFile(uuid);
		FileInfo fileInfo = fileSystemService.getFileInfo(handle, filepath);

		builder.lastModified(new Date(fileSystemService.lastModified(handle, filepath)));
		builder.header(HttpHeaders.CONTENT_LENGTH, fileInfo.getLength());
		builder.header(HttpHeaders.CONTENT_TYPE, mimeService.getMimeTypeForFilename(fileInfo.getFilename()));
		builder.header(HttpHeaders.ETAG, "\"" + fileSystemService.getMD5Checksum(handle, filepath) + "\"");
		return builder;
	}

	private void ensureStagingExists(String stagingUuid)
	{
		if( !stagingService.stagingExists(stagingUuid)
			|| !fileSystemService.fileExists(new StagingFile(stagingUuid), null) )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	private URI stagingUri(String stagingUuid)
	{
		return urlLinkService.getMethodUriBuilder(StagingResource.class, "getStaging").build(stagingUuid);
	}

	private URI stagingUri(String stagingUuid, String filepath)
	{
		return urlLinkService.getMethodUriBuilder(StagingResource.class, "getFile").build(stagingUuid, filepath);
	}
}
