package com.tle.web.api.payment.store.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.beans.filesystem.FileHandle;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.services.FileSystemService;
import com.tle.web.api.payment.store.CurrentStoreFront;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.WrappedContentStream;

/**
 * @author Aaron
 */
public abstract class AbstractStoreResource
{
	@Inject
	private CatalogueService catService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	protected static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(AbstractStoreResource.class);

	protected StoreFront getStoreFront()
	{
		return CurrentStoreFront.get();
	}

	/**
	 * For a given StroreFront, ensure that that it has country visibility for a
	 * specified Catalogue Uuid
	 * 
	 * @param sf StoreFront
	 * @param catalogueUuid
	 * @return Catalogue where identified by its UUID, and is visible, otherwise
	 *         null
	 */
	protected Catalogue verifyCatalogueForStore(StoreFront sf, String catalogueUuid)
	{
		Catalogue verifiedCatalogue = null;
		Catalogue catalogue = catService.getByUuid(catalogueUuid);
		if( catalogue != null )
		{
			// This is the country of the store front making the request
			String sfCountry = sf.getCountry();
			List<Catalogue> allCatalogues = catService.enumerateForCountry(sfCountry);

			// on establishing that the catalogue the caller is asking for is in
			// fact visible for the caller's country, return it
			for( Catalogue aCatalogue : allCatalogues )
			{
				if( aCatalogue.equals(catalogue) )
				{
					verifiedCatalogue = catalogue;
					break;
				}
			}
		}

		if( verifiedCatalogue == null )
		{
			// throw a NotFound, don't give them any clues as to it's actual
			// existence
			throw new NotFoundException(resources.getString("error.notfound.catalogue", catalogueUuid)); //$NON-NLS-1$
		}
		return verifiedCatalogue;
	}

	protected Response serveContentStreamResponse(final FileHandle handle, final String filepath,
		final HttpServletRequest request, final HttpServletResponse response)
	{
		ensureFileExists(handle, filepath);
		final String mimeType = mimeService.getMimeTypeForFilename(filepath);
		final StoreAttachmentStream contentStream = new StoreAttachmentStream(handle, filepath, mimeType);

		if( !contentStreamWriter.checkModifiedSince(request, response, contentStream.getLastModified()) )
		{
			return Response.notModified().build();
		}
		response.addHeader("Content-Disposition", "inline; filename=\"" + filepath + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return Response.ok().type(mimeType).entity(new StreamingOutput()
		{
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException
			{
				contentStreamWriter.outputStream(request, response, contentStream, output);
			}
		}).build();
	}

	protected void ensureFileExists(FileHandle handle, String filepath)
	{
		if( !fileSystemService.fileExists(handle, filepath) )
		{
			throw new NotFoundException(filepath);
		}
	}

	public class StoreAttachmentStream extends WrappedContentStream
	{
		public StoreAttachmentStream(FileHandle handle, String filepath, String mimetype)
		{
			super(fileSystemService.getContentStream(handle, filepath, mimetype));
		}

		// Copied from ItemFilestoreServlet
		@SuppressWarnings("nls")
		@Override
		public String getCacheControl()
		{
			return "max-age=86400, s-maxage=0, must-revalidate";
		}
	}
}
