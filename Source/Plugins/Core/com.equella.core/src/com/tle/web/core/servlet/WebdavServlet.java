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

package com.tle.web.core.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.common.io.CharStreams;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.web.core.servlet.webdav.WebdavProps;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

/**
 * This class handles Webdav requests and allows access to Learning Edge
 * attachments. Only one attachment can be accessed at a time, through the UUID
 * value of the attachment.
 * 
 * @author Charles O'Farrell
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class WebdavServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(WebdavServlet.class);
	private static final String CONTENT_TYPE_XML = "text/xml; charset=utf-8";
	private static final int SC_MULTI_STATUS = 207;
	private static final String METHODS_ALLOWED = "OPTIONS, GET, HEAD, POST, DELETE,"
		+ " TRACE, PROPFIND, PROPPATCH, COPY, MOVE, PUT, LOCK, UNLOCK";

	public static final class HttpMethod
	{
		public static final String GET = "GET";
		public static final String HEAD = "HEAD";
		public static final String POST = "POST";
		public static final String PUT = "PUT";

		private HttpMethod()
		{
			throw new Error();
		}
	}

	// MS requested props:
	/*
	 * <a:name/> <a:parentname/> <a:href/> <a:ishidden/> <a:isreadonly/>
	 * <a:getcontenttype/> <a:contentclass/> <a:getcontentlanguage/>
	 * <a:creationdate/> <a:lastaccessed/> <a:getlastmodified/>
	 * <a:getcontentlength/> <a:iscollection/> <a:isstructureddocument/>
	 * <a:defaultdocument/> <a:displayname/> <a:isroot/> <a:resourcetype/>
	 */

	private static String ALLPROP = "allprop";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException
	{
		try
		{
			doProcessing(req, resp);
		}
		catch( Exception ex )
		{
			LOGGER.error("Error in Webdav Servlet", ex);
			throw new ServletException(ex);
		}
	}

	protected void doProcessing(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception
	{
		String requestedUrl = request.getRequestURI();

		LOGGER.info("URL:" + requestedUrl);
		LOGGER.info("METHOD:" + request.getMethod());

		String[] parts = decodeParts(request, requestedUrl);
		String filename = parts[1];
		String stagingid = parts[0];

		LOGGER.info("FILENAME:" + filename);
		LOGGER.info("STAGING:" + stagingid);

		try
		{
			handleMethod(request, response, requestedUrl, stagingid, filename);
		}
		catch( NotFoundException ex )
		{
			LOGGER.info("WebDav Resource Not Found", ex);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		catch( IllegalArgumentException ex )
		{
			LOGGER.info("WebDav Method Not Supported", ex);
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
	}

	private String[] decodeParts(HttpServletRequest request, String szURL) throws UnsupportedEncodingException
	{
		String[] parts = new String[2];

		final String wd = request.getServletPath();
		String basepath = request.getContextPath() + wd;
		if( szURL.length() <= basepath.length() )
		{
			parts[0] = Constants.BLANK;
			parts[1] = Constants.BLANK;
			return parts;
		}
		// Changed as szURL could be the Desinition header, which included the
		// institution

		String tail = szURL.substring(szURL.indexOf(wd) + wd.length() + 1);
		String filename = Constants.BLANK;
		String stagingId;
		int firstSlash = tail.indexOf('/');
		if( firstSlash != -1 )
		{
			filename = tail.substring(firstSlash + 1);
			stagingId = tail.substring(0, firstSlash);
		}
		else
		{
			stagingId = tail;
		}
		filename = URLDecoder.decode(filename, Utils.CHARSET_ENCODING);

		parts[0] = stagingId;

		// filename may be an asbolute URL...
		// parts[1] =
		// urlService.removeInstitution(urlService.institutionalise(filename));
		parts[1] = filename;

		return parts;
	}

	/**
	 * Services an HTTP request from the client, and determines how to handle
	 * it. In particular it examines the request method and decides whether the
	 * request is a WebDav or normal request.
	 * <p>
	 * The following are the supported WebDav methods:
	 * <ul>
	 * <li>GET
	 * <li>POST
	 * <li>OPTIONS
	 * <li>PROPFIND
	 * <li>HEAD
	 * <li>COPY
	 * <li>MOVE
	 * <li>DELETE
	 * <li>PUT
	 * <li>MKCOL
	 * </ul>
	 * <p>
	 * Unsupported Webdav methods:
	 * <ul>
	 * <li>PROPPATCH
	 * <li>LOCK
	 * <li>UNLOCK
	 * <li>ACLs
	 * </ul>
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 * @throws IOException
	 * @throws Exception
	 */
	public void handleMethod(HttpServletRequest req, HttpServletResponse resp, String requestedUrl, String stagingid,
		String filename) throws IOException, Exception
	{
		StagingFile stagingFile = null;

		if( stagingid.length() > 0 )
		{
			stagingFile = new StagingFile(stagingid);
			// check valid staging id
			if( !fileSystemService.fileIsDir(stagingFile, Constants.BLANK) )
			{
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}

		String methodName = req.getMethod();
		LOGGER.info("WebDav method = " + methodName);

		if( methodName.equalsIgnoreCase(HttpMethod.GET) || methodName.equalsIgnoreCase(HttpMethod.POST) )
		{
			getMethod(req, resp, stagingFile, filename, true);
		}
		else if( methodName.equalsIgnoreCase(HttpMethod.HEAD) )
		{
			getMethod(req, resp, stagingFile, filename, false);
		}
		else if( methodName.equalsIgnoreCase("OPTIONS") )
		{
			optionsMethod(req, resp);
		}
		else if( methodName.equalsIgnoreCase("PROPFIND") )
		{
			propFindMethod(req, resp, stagingFile, filename);
		}
		else if( methodName.equalsIgnoreCase(HttpMethod.PUT) )
		{
			putMethod(req, resp, stagingFile, filename);
		}
		else if( methodName.equalsIgnoreCase("LOCK") )
		{
			lockMethod(req, resp);
		}
		else if( methodName.equalsIgnoreCase("PROPPATCH") )
		{
			resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		else if( methodName.equalsIgnoreCase("UNLOCK") )
		{
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		else if( methodName.equalsIgnoreCase("DELETE") )
		{
			deleteMethod(req, resp, stagingFile, filename);
		}
		else if( methodName.equalsIgnoreCase("MKCOL") )
		{
			mkcolMethod(req, resp, stagingFile, filename);
		}
		else if( methodName.equalsIgnoreCase("MOVE") )
		{
			moveMethod(req, resp, stagingFile, filename);
		}
		else if( methodName.equalsIgnoreCase("COPY") )
		{
			copyMethod(req, resp, stagingFile, filename);
		}
		else
		{
			throw new IllegalArgumentException("Method '" + methodName + "' not implemented");
		}
	}

	/**
	 * Process a GET request
	 * 
	 * @param req
	 * @param resp
	 * @param itemurl
	 * @param fileSystem
	 * @param actualserve
	 * @throws Exception
	 */
	private void getMethod(HttpServletRequest request, HttpServletResponse response, StagingFile staging, String fname,
		boolean actualserve) throws Exception
	{
		if( fileSystemService.fileExists(staging, fname) )
		{
			if( Check.isEmpty(fname) && actualserve )
			{
				// tell n00bs not to paste the URL in the browser
				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_OK);

				if( actualserve )
				{
					try( PrintWriter out = new PrintWriter(response.getOutputStream()) )
					{
						out.write(
							"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\"><title>No</title></head><body><div><h1>Please open this URL as a web folder.  Do not paste the URL directly in the web browser.</h1></div></body></html>");
						out.flush();
					}
				}
			}

			else
			{
				response.setStatus(HttpServletResponse.SC_OK);

				// Send back data
				if( !fileSystemService.fileIsDir(staging, fname) )
				{
					final String mimeType = mimeTypeService.getMimeTypeForFilename(fname);
					FileContentStream stream = fileSystemService.getContentStream(staging, fname, mimeType);

					contentStreamWriter.outputStream(request, response, stream);
				}
			}
		}

		else
		{
			LOGGER.info("Webdav 404, File not found: " + fname);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * Handles the webdav PROPFIND method, which retrieves the list of available
	 * files, and their properties.
	 * <p>
	 * The request from a browser uses a body as shown here, which details which
	 * properties to return.
	 * <p>
	 * Microsoft Windows wants this: <code>
	 * <pre>
	 * <?xml version = '1.0' encoding = 'UTF-8'?>
	 *  <a:propfind xmlns:a="DAV:" xmlns:b="urn:schemas-microsoft-com:datatypes">
	 *     <a:prop>
	 *     	<a:name/>
			<a:parentname/>
			<a:href/>
			<a:ishidden/>
			<a:isreadonly/>
			<a:getcontenttype/>
			<a:contentclass/>
			<a:getcontentlanguage/>
			<a:creationdate/>
			<a:lastaccessed/>
			<a:getlastmodified/>
			<a:getcontentlength/>
			<a:iscollection/>
			<a:isstructureddocument/>
			<a:defaultdocument/>
			<a:displayname/>
			<a:isroot/>
			<a:resourcetype/>
	 *     </a:prop>
	 *  </a:propfind>
	 * </pre>
	 * </code>
	 * <p>
	 * Here is an example of a correctly formatted response. The
	 * "HTTP/1.1 404 Not Found" <propstat>'s are to descibe what properties
	 * haven't been listed in the response.
	 * <p>
	 * <code>
	 * <pre>
	 * <?xml version = '1.0' encoding = 'utf-8'?>
	 * <multistatus xmlns="DAV:">
	 *   <response>
	 *      <href>/Day1-Webdav-context-root/servlet/WebdavServlet/</href>
	 *      <propstat>
	 *         <prop>
	 *            <parentname/>
	 *            <ishidden/>
	 *            <isreadonly/>
	 *            <getcontenttype/>
	 *            <creationdate/>
	 *            <getlastmodified/>
	 *            <contentclass/>
	 *            <getcontentlanguage/>
	 *            <lastaccessed/>
	 *            <iscollection/>
	 *            <isstructureddocument/>
	 *            <defaultdocument/>
	 *            <isroot/>
	 *         </prop>
	 *         <status>HTTP/1.1 404 Not Found</status>
	 *      </propstat>
	 *      <propstat>
	 *         <prop>
	 *         	  <name/>
	 *            <href/>
	 *            <displayname/>
	 *            <getcontentlength>0</getcontentlength>
	 *            <resourcetype>
	 *               <collection/>
	 *            </resourcetype>
	 *         </prop>
	 *         <status>HTTP/1.1 200 OK</status>
	 *      </propstat>
	 *   </response>
	 *   <response>
	 *      <propstat>
	 *         <prop>
	 *            <ishidden/>
	 *            <isreadonly/>
	 *            <getcontenttype/>
	 *            <creationdate/>
	 *            <getlastmodified/>
	 *            <contentclass/>
	 *            <getcontentlanguage/>
	 *            <lastaccessed/>
	 *            <iscollection/>
	 *            <isstructureddocument/>
	 *            <defaultdocument/>
	 *            <isroot/>
	 *         </prop>
	 *         <status>HTTP/1.1 404 Not Found</status>
	 *      </propstat>
	 *      <href>/Day1-Webdav-context-root/servlet/WebdavServlet/Colour.zip</href>
	 *      <propstat>
	 *         <prop>
	 *            <name/>
	 *            <href/>
	 *            <parentname/>
	 *            <displayname>Colour.zip</displayname>
	 *            <getcontentlength>0</getcontentlength>
	 *            <resourcetype>
	 *               <collection/>
	 *            </resourcetype>
	 *         </prop>
	 *         <status>HTTP/1.1 200 OK</status>
	 *      </propstat>
	 *   </response>
	 * </multistatus>
	 * </pre>
	 * </code>
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 * @throws IOException
	 * @throws Exception
	 */
	public void propFindMethod(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename)
		throws IOException, Exception
	{
		// Don't want double //'s
		final String basepath = institutionService.getInstitutionUrl() + req.getServletPath().substring(1);

		if( !fileSystemService.fileExists(staging, filename) )
		{
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String uri = basepath + '/' + staging.getUuid() + '/' + filename;
		boolean isDir = fileSystemService.fileIsDir(staging, filename);
		// http://www.webdav.org/specs/rfc2518.html#collection.resources
		// folders should end with '/'
		if( isDir && !uri.endsWith("/") )
		{
			uri += '/';
		}
		resp.setHeader("Content-Location", uri);

		Set<String> requestedProperties = getRequestedProps(req);

		resp.setStatus(SC_MULTI_STATUS);
		resp.setContentType(CONTENT_TYPE_XML);

		// XML Response
		PropBagEx propBag = new PropBagEx();
		propBag.createNode("multistatus", Constants.BLANK);
		propBag.setNode("multistatus/@xmlns", "DAV:");
		propBag = propBag.getSubtree("multistatus");

		// http://www.webdav.org/specs/rfc2518.html#rfc.section.8.1.2
		// if the requested file is a folder:

		if( isDir )
		{
			final WebdavProps madProps = new WebdavProps(mimeTypeService, requestedProperties);
			madProps.addFolderProps(Constants.BLANK, filename, Check.isEmpty(filename));
			addResponse(propBag, madProps, uri);

			// Recurses the directory and lists the files within
			final FileEntry dir = fileSystemService.enumerateTree(staging, filename, null);
			recurseDirectory(requestedProperties, uri, propBag, staging, filename, dir, getRequestedDepth(req));
		}
		else
		{
			final WebdavProps madProps = new WebdavProps(mimeTypeService, requestedProperties);
			final FileEntry file = fileSystemService.enumerateTree(staging, filename, null);
			madProps.addFileProps(Constants.BLANK, file);
			addResponse(propBag, madProps, uri);
		}

		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug(propBag.toString());
		}

		// Returns the XML Response
		final String responseXml = "<?xml version = '1.0' encoding = 'utf-8'?>\n" + propBag.toString();
		resp.setContentLengthLong(responseXml.getBytes().length);

		final PrintWriter writer = resp.getWriter();
		writer.print(responseXml);

		writer.close();
	}

	private Set<String> getRequestedProps(HttpServletRequest req)
	{
		final Set<String> reqProps = new HashSet<String>();

		InputStream in = null;
		try
		{
			in = req.getInputStream();
			Reader rdr = new UnicodeReader(in, Constants.UTF8);
			StringWriter wrt = new StringWriter();
			CharStreams.copy(rdr, wrt);

			final String xml = wrt.toString();
			// http://www.webdav.org/specs/rfc2518.html#METHOD_PROPFIND
			// empty request body assumes an allprop request
			if( Check.isEmpty(xml) )
			{
				return null;
			}

			final PropBagEx inBag = new PropBagEx(xml);
			if( inBag.nodeExists(ALLPROP) )
			{
				// nothing ( which means all )
				return null;
			}
			else
			{
				for( Node node : inBag.iterateAllNodes("/prop/*") )
				{
					String nodeName = node.getNodeName();
					int colonIndex = nodeName.lastIndexOf(':');
					if( colonIndex >= 0 )
					{
						nodeName = nodeName.substring(colonIndex + 1);
					}
					reqProps.add(nodeName);
				}
			}
			return reqProps;
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	private int getRequestedDepth(HttpServletRequest req)
	{
		// Request has a depth value which describes how many levels
		// of propeties need to be returned. The default is INFINITY, but
		// usually it is only 1.
		try
		{
			return req.getIntHeader("Depth");
		}
		catch( Exception e )
		{
			// Depth = Infinity
			return Integer.MAX_VALUE;
		}
	}

	private void lockMethod(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType(CONTENT_TYPE_XML);
		PrintWriter writer = resp.getWriter();

		// XML Response
		PropBagEx propBag = new PropBagEx().newSubtree("prop");
		propBag.setNode("@xmlns", "DAV:");

		PropBagEx x = propBag.newSubtree("lockdiscovery").newSubtree("activelock");
		// Need this for IE - not infinite
		x.setNode("timeout", "Second-" + Integer.MAX_VALUE);

		// Returns the XML Response
		writer.println("<?xml version = '1.0' encoding = 'utf-8'?>");
		writer.println(propBag);
		writer.close();
	}

	/**
	 * Handles the OPTIONS method, which asks the server what WebDav version and
	 * methods are supported.
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 */
	public void optionsMethod(HttpServletRequest req, HttpServletResponse resp)
	{
		resp.addHeader("DAV", "1, 2");
		resp.addHeader("Allow", METHODS_ALLOWED);
		resp.addHeader("MS-Author-Via", "DAV");
		resp.addHeader("LearningEdge", "true");
	}

	/**
	 * Handles the PUT method, which uploads a file to the server.
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 */
	public void putMethod(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename)
		throws IOException
	{
		try( InputStream in = req.getInputStream() )
		{
			fileSystemService.write(staging, filename, in, false);
			resp.setStatus(HttpServletResponse.SC_CREATED);
		}
	}

	/**
	 * Handles the MKCOL method, which makes a new "Collection" (ie Folder) on
	 * the server. This is supported, but doesn't actually work because
	 * currently the Learning Edge doesn't support such an action. However, a
	 * CREATED status is sent to fool the client into thinking it has been
	 * created. If any files are then dragged into the folder then it WILL be
	 * created.
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 * @throws RemoteException
	 */
	public void mkcolMethod(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename)
		throws RemoteException
	{
		fileSystemService.mkdir(staging, filename);
		resp.setStatus(HttpServletResponse.SC_CREATED);
	}

	/**
	 * Handles the COPY method, which copies a file from one location to
	 * another.
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 * @throws IOException if the properties could not be found @
	 */
	public void copyMethod(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename)
		throws IOException, NotFoundException
	{
		copyOrMove(req, resp, staging, filename, true);
	}

	/**
	 * Handles the MOVE method, which moves a file from one location to another,
	 * which is also used for renaming a file.
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 * @throws UnsupportedEncodingException
	 * @throws RemoteException
	 */
	public void moveMethod(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename)
		throws IOException
	{
		copyOrMove(req, resp, staging, filename, false);
	}

	private void copyOrMove(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename,
		boolean copy) throws IOException
	{
		String dest = req.getHeader("Destination");
		try
		{
			dest = (new URL(dest)).getPath();
		}
		catch( MalformedURLException ex )
		{
			throw new NotFoundException("Malformed URL", ex, true);
		}

		final String[] parts = decodeParts(req, dest);
		if( !parts[0].equals(staging.getUuid()) )
		{
			throw new NotFoundException("Illegal copy/move", true);
		}

		final String newname = parts[1];

		// http://www.webdav.org/specs/rfc2518.html#rfc.section.8.9.4
		if( filename.equals(newname) )
		{
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		final boolean destExists = fileSystemService.fileExists(staging, newname);

		final String overwrite = req.getHeader("Overwrite");
		if( destExists )
		{
			if( overwrite != null && overwrite.equals("F") )
			{
				resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				return;
			}
			else if( overwrite != null && overwrite.equals("T") )
			{
				// http://www.webdav.org/specs/rfc2518.html#rfc.section.8.8.4
				// delete dest folder
				fileSystemService.removeFile(staging, newname);
			}
		}

		final boolean success = (copy ? fileSystemService.copy(staging, filename, newname) != null
			: fileSystemService.rename(staging, filename, newname));

		// http://www.webdav.org/specs/rfc2518.html#rfc.section.8.9.4
		// respond:
		if( success )
		{
			if( !destExists )
			{
				resp.setStatus(HttpServletResponse.SC_CREATED);
			}
			else
			{
				resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		}
		else
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Handles the DELETE method, which deletes a file from the server.
	 * 
	 * @param req HTTP Request from client
	 * @param resp HTTP Response to client
	 */
	public void deleteMethod(HttpServletRequest req, HttpServletResponse resp, StagingFile staging, String filename)
	{
		try
		{
			fileSystemService.removeFile(staging, filename);
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		catch( Exception e )
		{
			LOGGER.error("Error in deleteMethod", e);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	// ************** Private Methods **************//

	/*
	 * Recursively moves through the dir PropBag and retrieves the file listing
	 * and adds it to the (main) propBag. In many cases this will only be called
	 * once, as the depth is usually 1.
	 */
	private void recurseDirectory(Set<String> requestedProperties, String uri, PropBagEx propBag, StagingFile staging,
		String parentName, FileEntry dir, int depth) throws IOException
	{
		if( depth == 0 )
		{
			return;
		}

		if( dir.isFolder() )
		{
			for( FileEntry file : dir.getFiles() )
			{
				final String name = file.getName();

				// Word doesn't like +'s in it's url encoding
				String newuri = uri + URLEncoder.encode(name, Utils.CHARSET_ENCODING).replaceAll("\\+", "%20");

				final WebdavProps props = new WebdavProps(mimeTypeService, requestedProperties);

				if( file.isFolder() )
				{
					props.addFolderProps(parentName, file.getName(), false);
				}
				else
				{
					props.addFileProps(parentName, file);
				}

				addResponse(propBag, props, newuri);

				// Recurse directory
				recurseDirectory(requestedProperties, newuri + '/', propBag, staging, file.getName(), file, depth - 1);
			}
		}
	}

	private void addResponse(final PropBagEx multiStatusBag, final WebdavProps props, final String href)
	{
		PropBagEx responseBag = multiStatusBag.newSubtree("response");
		responseBag.setNode("href", href);
		props.createResponsePropstats(responseBag);
	}
}
