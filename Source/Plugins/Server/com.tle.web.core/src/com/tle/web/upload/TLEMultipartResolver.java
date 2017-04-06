package com.tle.web.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;

import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
import com.tle.web.core.servlet.ProgressServlet;
import com.tle.web.upload.TLEMultipartResolver.TLEFileUpload.TLEFileItemIteratorImpl;
import com.tle.web.upload.UploadService.Upload;

@SuppressWarnings("nls")
@Bind
@Singleton
public class TLEMultipartResolver extends CommonsMultipartResolver
{
	private static final String LAZY_UPLOAD_FIELD = "lazyUpload";
	private static final String UPLOAD_ID_FIELD = "uploadId";

	@Inject
	private ProgressServlet progressServlet;
	@Inject
	private UploadService uploadService;

	@Override
	public boolean isMultipart(HttpServletRequest request)
	{
		if( request.getAttribute("ALREADY_RESOLVED!") == null )
		{
			request.setAttribute("ALREADY_RESOLVED!", true);
			return super.isMultipart(request);
		}
		return false;
	}

	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException
	{
		final String encoding = determineEncoding(request);
		final TLEFileUpload fileUpload = (TLEFileUpload) prepareFileUpload(encoding);

		try
		{
			ProgressListenerImpl listener = new ProgressListenerImpl();
			fileUpload.setProgressListener(listener);

			final List fileItems = fileUpload.parseRequest(request);
			final MultipartParsingResult parsingResult = parseFileItems(fileItems, encoding);

			return new TLEMultipartHttpServlet(request, parsingResult.getMultipartFiles(),
				parsingResult.getMultipartParameters());
		}
		catch( FileUploadBase.SizeLimitExceededException ex )
		{
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		}
		catch( FileUploadException ex )
		{
			throw new MultipartException("Could not parse multipart servlet request", ex);
		}
	}

	@Override
	protected DiskFileItemFactory newFileItemFactory()
	{
		return new TLEFileItemFactory();
	}

	@Override
	protected FileUpload newFileUpload(FileItemFactory fileItemFactory)
	{
		return new TLEFileUpload(fileItemFactory);
	}

	public class TLEFileUpload extends ServletFileUpload
	{
		public TLEFileUpload(FileItemFactory fileItemFactory)
		{
			super(fileItemFactory);
		}

		@Override
		public FileItemIterator getItemIterator(RequestContext ctx) throws FileUploadException, IOException
		{
			return new TLEFileItemIteratorImpl(ctx);
		}

		public class TLEFileItemIteratorImpl extends FileUploadBase.FileItemIteratorImpl
		{
			private boolean lazyUploadFlag;
			private String uploadId;

			public TLEFileItemIteratorImpl(RequestContext ctx) throws FileUploadException, IOException
			{
				super(ctx);
			}

			@Override
			protected MultipartStream createMultipartStream(InputStream input, byte[] boundary,
				MultipartStream.ProgressNotifier notifier)
			{
				return new TLEMultipartStream(input, boundary, notifier, this);
			}

			public String getUploadId()
			{
				return uploadId;
			}

			public void setUploadId(String uploadId)
			{
				this.uploadId = uploadId;
			}

			public boolean isLazyUploadFlag()
			{
				return lazyUploadFlag;
			}

			public void setLazyUploadFlag(boolean lazyUploadFlag)
			{
				this.lazyUploadFlag = lazyUploadFlag;
			}

			public void setEof(boolean eof)
			{
				this.eof = eof;
			}
		}

		/**
		 * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC
		 * 1867</a> compliant <code>multipart/form-data</code> stream.
		 * 
		 * @param ctx The context for the request to be parsed.
		 * @return A list of <code>FileItem</code> instances parsed from the
		 *         request, in the order that they were transmitted.
		 * @throws FileUploadException if there are problems reading/parsing the
		 *             request or storing files.
		 */
		@Override
		public List<FileItem> parseRequest(RequestContext ctx) throws FileUploadException
		{
			final List<FileItem> items = new ArrayList<FileItem>();
			boolean successful = false;
			boolean allowLazyUpload = true;

			final TLEFileItemFactory fac = (TLEFileItemFactory) getFileItemFactory();
			try
			{
				final TLEFileItemIteratorImpl iter = (TLEFileItemIteratorImpl) getItemIterator(ctx);
				while( iter.hasNext() )
				{
					final FileItemStream item = iter.next();
					final String fileName = item.getName();
					final FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(),
						item.isFormField(), fileName, iter.isLazyUploadFlag());
					items.add(fileItem);

					try
					{
						if( !item.isFormField() )
						{
							// disable lazyUpload if the lazyUpload flag has not
							// yet been read
							allowLazyUpload = false;

							if( iter.isLazyUploadFlag() )
							{
								final String uploadId = iter.getUploadId();
								@SuppressWarnings("resource")
								final Upload input = uploadService.addUpload(
									uploadId != null ? uploadId : CurrentUser.getSessionID(), item);
								((TLEDiskFileItem) fileItem).setStreamToUseForInputStream(input);
								progressServlet.addListener(input.getUuid(), listener);

								// don't try to read any more items
								iter.setEof(true);
							}
							else
							{
								// dodgy: this is the same listener for all
								// files on this control
								// but this is how it's always been. Oh well,
								// use the new ajax upload! :)
								progressServlet.addListener(item.getFieldName(), listener);
								Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
							}
						}
						else
						{
							Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
							String fieldName = item.getFieldName();
							if( fieldName.equals(LAZY_UPLOAD_FIELD) )
							{
								iter.setLazyUploadFlag(allowLazyUpload);
							}
							else if( fieldName.equals(UPLOAD_ID_FIELD) )
							{
								iter.setUploadId(fileItem.getString("UTF-8"));
							}
						}
					}
					catch( FileUploadIOException e )
					{
						throw new RuntimeException(e.getCause());
					}
					catch( IOException e )
					{
						throw new RuntimeException("Processing of " + MULTIPART_FORM_DATA + " request failed. "
							+ e.getMessage(), e);
					}

					if( fileItem instanceof FileItemHeadersSupport )
					{
						final FileItemHeaders fih = item.getHeaders();
						((FileItemHeadersSupport) fileItem).setHeaders(fih);
					}
				}
				successful = true;
				return items;
			}
			catch( FileUploadIOException e )
			{
				throw (FileUploadException) e.getCause();
			}
			catch( IOException e )
			{
				throw new FileUploadException(e.getMessage(), e);
			}
			finally
			{
				if( !successful )
				{
					for( FileItem fileItem : items )
					{
						try
						{
							fileItem.delete();
						}
						catch( Exception e )
						{
							// ignore it
						}
					}
				}
			}
		}
	}

	public static class TLEFileItemFactory extends DiskFileItemFactory
	{
		public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName,
			boolean lazyUpload)
		{
			if( lazyUpload && !isFormField )
			{
				return new TLEDiskFileItem(fieldName, contentType, isFormField, fileName, 0, null);
			}
			return super.createItem(fieldName, contentType, isFormField, fileName);
		}
	}

	public static class TLEDiskFileItem extends DiskFileItem
	{
		private Upload inputStream;

		public TLEDiskFileItem(String fieldName, String contentType, boolean isFormField, String fileName,
			int sizeThreshold, File repository)
		{
			super(fieldName, contentType, isFormField, fileName, sizeThreshold, repository);
		}

		public void setStreamToUseForInputStream(Upload inputStream)
		{
			this.inputStream = inputStream;
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			if( inputStream != null )
			{
				return inputStream;
			}
			return super.getInputStream();
		}

		@Override
		protected void finalize()
		{
			try
			{
				getOutputStream();
			}
			catch( IOException e )
			{
				// nohting
			}
			super.finalize();
		}

		@Override
		public boolean isInMemory()
		{
			return true;
		}

		@Override
		public long getSize()
		{
			return 1;
		}
	}

	public static class TLEMultipartStream extends MultipartStream
	{
		private final TLEFileItemIteratorImpl iter;

		public TLEMultipartStream(InputStream input, byte[] boundary, ProgressNotifier pNotifier,
			TLEFileItemIteratorImpl iter)
		{
			super(input, boundary, DEFAULT_BUFSIZE, pNotifier);
			this.iter = iter;
		}

		@Override
		protected ItemInputStream newInputStream()
		{
			if( iter.isLazyUploadFlag() )
			{
				return new TLEItemInputStream();
			}
			return super.newInputStream();
		}

		public class TLEItemInputStream extends ItemInputStream
		{
			/**
			 * Returns the next byte in the stream.
			 * 
			 * @return The next byte in the stream, as a non-negative integer,
			 *         or -1 for EOF.
			 * @throws IOException An I/O error occurred.
			 */
			@Override
			public int read() throws IOException
			{
				if( available() == 0 && makeAvailable() == 0 )
				{
					return -1;
				}
				++total;
				int b = buffer[head++];
				if( b >= 0 )
				{
					return b;
				}
				return b + BYTE_POSITIVE_OFFSET;
			}

			/**
			 * Reads bytes into the given buffer.
			 * 
			 * @param b The destination buffer, where to write to.
			 * @param off Offset of the first byte in the buffer.
			 * @param len Maximum number of bytes to read.
			 * @return Number of bytes, which have been actually read, or -1 for
			 *         EOF.
			 * @throws IOException An I/O error occurred.
			 */
			@Override
			public int read(byte[] b, int off, int len) throws IOException
			{
				if( len == 0 )
				{
					return 0;
				}
				int res = available();
				if( res == 0 )
				{
					res = makeAvailable();
					if( res == 0 )
					{
						return -1;
					}
				}
				res = Math.min(res, len);
				System.arraycopy(buffer, head, b, off, res);
				head += res;
				total += res;
				return res;
			}
		}
	}

	public static class TLEMultipartHttpServlet extends AbstractMultipartHttpServletRequest
	{
		private final Map multipartParameters;

		/**
		 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
		 * 
		 * @param request the servlet request to wrap
		 * @param multipartFiles a map of the multipart files
		 * @param multipartParameters a map of the parameters to expose, with
		 *            Strings as keys and String arrays as values
		 */
		public TLEMultipartHttpServlet(HttpServletRequest request, Map multipartFiles, Map multipartParameters)
		{

			super(request);
			setMultipartFiles(multipartFiles);
			this.multipartParameters = multipartParameters;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Enumeration<String> getParameterNames()
		{
			Set<String> paramNames = new HashSet<String>();
			Enumeration<String> paramEnum = super.getParameterNames();
			while( paramEnum.hasMoreElements() )
			{
				paramNames.add(paramEnum.nextElement());
			}
			paramNames.addAll(this.multipartParameters.keySet());
			return Collections.enumeration(paramNames);
		}

		@Override
		public String getParameter(String name)
		{
			String value = super.getParameter(name);
			if( value != null )
			{
				return value;
			}
			String[] values = (String[]) this.multipartParameters.get(name);
			if( values != null )
			{
				return (values.length > 0 ? values[0] : null);
			}
			return null;
		}

		@Override
		public String[] getParameterValues(String name)
		{
			String[] values = super.getParameterValues(name);
			if( values != null )
			{
				return values;
			}
			return (String[]) this.multipartParameters.get(name);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map getParameterMap()
		{
			Map paramMap = new HashMap();
			paramMap.putAll(this.multipartParameters);
			paramMap.putAll(super.getParameterMap());
			return paramMap;
		}
	}
}
