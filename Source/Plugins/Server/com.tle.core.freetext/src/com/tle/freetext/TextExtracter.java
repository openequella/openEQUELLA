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

package com.tle.freetext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Fieldable;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.URLUtils;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.TextExtracterExtension;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.ims.service.IMSService;

/**
 * @author jmaginnis
 */
@Bind
@Singleton
public class TextExtracter
{
	private static final Logger LOGGER = Logger.getLogger(TextExtracter.class);

	// Max size supported by Tika
	private static final int SUMMARY_SIZE = 100000;
	private static final int URL_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(2);

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private IMSService imsService;

	@Inject(optional = true)
	@Named("textExtracter.indexAttachments")
	private boolean indexAttachments = true;
	@Inject(optional = true)
	@Named("textExtracter.indexImsPackages")
	private boolean indexImsPackages = true;

	@SuppressWarnings("nls")
	public List<Fieldable> indexAttachments(IndexedItem indexedItem, SearchSettings searchSettings)
	{
		final List<Fieldable> fields = new ArrayList<Fieldable>();
		final Item item = indexedItem.getItem();
		final int urlLevel = searchSettings.getUrlLevel();

		boolean didMime = false;
		boolean hasAttachments = false;

		// file and html
		for( Attachment attach : item.getAttachmentsUnmodifiable() )
		{
			hasAttachments = true;
			try
			{
				final StringBuilder sbuf = new StringBuilder();
				if( indexAttachments )
				{
					switch( attach.getAttachmentType() )
					{
						case FILE: {
							final String filename = attach.getUrl();
							final MimeEntry mimeEntry = mimeService.getEntryForFilename(filename);

							// Allow for searching by the filename
							sbuf.append(filename);
							sbuf.append(' ');

							if( mimeEntry != null )
							{
								final List<TextExtracterExtension> extractors = getExtractors(mimeEntry);
								if( !extractors.isEmpty() )
								{
									try( InputStream input = fileSystemService.read(itemFileService.getItemFile(item),
										filename) )
									{
										extractTextFromStream(extractors, input, mimeEntry, sbuf);
									}
								}
							}
							break;
						}

						case HTML: {
							final HtmlAttachment htmlAttach = (HtmlAttachment) attach;
							final String filename = htmlAttach.getFilename();
							final MimeEntry mimeEntry = mimeService.getEntryForFilename(filename);

							final List<TextExtracterExtension> extractors = getExtractors(mimeEntry);
							if( !extractors.isEmpty() )
							{
								try( InputStream input = fileSystemService.read(itemFileService.getItemFile(item),
									filename) )
								{
									extractTextFromStream(extractors, input, mimeEntry, sbuf);
								}
							}
							break;
						}

						case LINK: {
							if( urlLevel == SearchSettings.URL_DEPTH_LEVEL_NONE )
							{
								break;
							}

							URL url = new URL(attach.getUrl());
							Robots robots = new Robots(url);

							if( robots.isAllowed(url.getPath()) )
							{
								boolean needsGet = false;
								URLConnection urlcon = url.openConnection();
								if( urlcon instanceof HttpURLConnection )
								{
									((HttpURLConnection) urlcon).setRequestMethod("HEAD");
									needsGet = true;
								}
								urlcon.setConnectTimeout(URL_TIMEOUT);
								urlcon.setReadTimeout(URL_TIMEOUT);
								InputStream input = urlcon.getInputStream();

								try
								{
									MimeEntry mimeEntry = getMimeEntryFromContentType(urlcon.getContentType());
									List<TextExtracterExtension> extractors = getExtractors(mimeEntry);

									boolean isHtmlAndNeedsParsing = (mimeEntry != null
										&& mimeEntry.getType().indexOf("html") != -1
										&& urlLevel >= SearchSettings.URL_DEPTH_LEVEL_REFERENCED_AND_LINKED);
									boolean isIndexable = extractors.size() > 0;
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									if( isIndexable || isHtmlAndNeedsParsing )
									{
										// Now do a GET if required
										if( needsGet )
										{
											Closeables.close(input, true);
											urlcon = url.openConnection();
											urlcon.setConnectTimeout(URL_TIMEOUT);
											urlcon.setReadTimeout(URL_TIMEOUT);
											input = urlcon.getInputStream();
										}

										byte[] buf = new byte[8192];
										while( true )
										{
											int amount = input.read(buf);
											if( amount <= 0 )
											{
												break;
											}
											baos.write(buf, 0, amount);
										}
									}

									if( isHtmlAndNeedsParsing )
									{
										try
										{
											new URLDownloader(new ByteArrayInputStream(baos.toByteArray()), url, sbuf,
												robots).download();
										}
										catch( Exception e )
										{
											if( LOGGER.isDebugEnabled() )
											{
												LOGGER.debug("Error download referenced links in:" + url, e);
											}
										}
									}

									if( isIndexable )
									{
										ByteArrayInputStream binput = new ByteArrayInputStream(baos.toByteArray());
										extractTextFromStream(extractors, binput, mimeEntry, sbuf);
									}
								}
								finally
								{
									Closeables.close(input, true);
								}
							}
							break;
						}

						case CUSTOM:
						case IMS: {

							if( attach instanceof CustomAttachment
								&& !((CustomAttachment) attach).getType().equals("scorm") )
							{
								break;
							}
							else if( indexImsPackages )
							{
								Attachment imsAttach = attach;
								String imsFolder = imsAttach.getUrl();
								ItemFile file = itemFileService.getItemFile(item);
								IMSManifest imsManifest = imsService.getImsManifest(file, imsFolder, true);
								if( imsManifest != null )
								{
									List<IMSResource> allResources = imsManifest.getAllResources();
									for( IMSResource res : allResources )
									{
										String fullHref = res.getFullHref();
										final MimeEntry entry = mimeService.getEntryForFilename(fullHref);
										final List<TextExtracterExtension> extractors = getExtractors(entry);
										if( !extractors.isEmpty() )
										{
											try( InputStream input = fileSystemService.read(file,
												imsFolder + '/' + fullHref) )
											{
												extractTextFromStream(extractors, input, entry, sbuf);
											}
										}
									}
								}
							}
							break;
						}
						case IMSRES:
							break;
						case ZIP:
							break;
						default:
							LOGGER.info("Unhandled attachment type: " + attach.getAttachmentType());
							break;
					}
				}

				if( sbuf.length() > 0 )
				{
					String attachmentText = sbuf.toString();
					fields.add(AbstractIndexingExtension.unstoredAndVectored(FreeTextQuery.FIELD_ATTACHMENT_VECTORED,
						attachmentText));
					fields.add(AbstractIndexingExtension
						.unstoredAndVectored(FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM, attachmentText));
				}
			}
			catch( FileNotFoundException ex )
			{
				LOGGER.warn("Attachment could not be found: " + ex.getMessage()); //$NON-NLS-1$
			}
			catch( Exception t )
			{
				LOGGER.error("Error indexing attachment: ", t);
			}
			catch( Throwable tt )
			{
				LOGGER.error("Error indexing attachment (throwable): ", tt);
			}

			try
			{
				final String mimeEntry = mimeService.getMimeEntryForAttachment(attach);
				if( mimeEntry != null )
				{
					didMime = true;
					indexMimeEntry(mimeEntry, fields);
				}
				else
				{
					LOGGER.trace(
						"No mimeEntry for attachment " + attach.getDescription() + " on item " + item.getIdString());
				}
			}
			catch( Throwable t )
			{
				LOGGER.error("Blew up indexing MIME type for item " + item.getIdString(), t);
			}
		}

		if( hasAttachments && !didMime )
		{
			LOGGER.warn("Didn't index MIME type for item " + item.getIdString());
		}

		return fields;
	}

	private void indexMimeEntry(String mimeEntry, List<Fieldable> fields)
	{
		fields.add(AbstractIndexingExtension.indexed(FreeTextQuery.FIELD_ATTACHMENT_MIME_TYPES, mimeEntry));
	}

	private List<TextExtracterExtension> getExtractors(MimeEntry mimeEntry)
	{
		if( mimeEntry == null )
		{
			return Collections.emptyList();
		}

		return mimeService.getTextExtractersForMimeEntry(mimeEntry);
	}

	private void extractTextFromStream(List<TextExtracterExtension> extracters, InputStream inp, MimeEntry mimeEntry,
		StringBuilder outputText) throws Exception
	{
		String mimeType = mimeEntry != null ? mimeEntry.getType() : null;
		if( extracters.size() > 0 )
		{
			extracters.get(0).extractText(mimeType, inp, outputText, SUMMARY_SIZE);
			outputText.append(' ');
		}
		else
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Did not extract text for attachment with mimeType: " + mimeType); //$NON-NLS-1$
			}
		}
	}

	protected MimeEntry getMimeEntryFromContentType(String contentType)
	{
		String mimeType = contentType;
		if( contentType != null )
		{
			int semiColon = contentType.indexOf(';');
			if( semiColon >= 0 )
			{
				mimeType = contentType.substring(0, semiColon);
			}
		}
		return mimeService.getEntryForMimeType(mimeType);
	}

	public class URLDownloader extends DefaultHandler
	{
		private final URL url;
		private final StringBuilder buf;
		private final Robots robot;

		private final InputStream stream;

		public URLDownloader(InputStream stream, URL orig, StringBuilder sbuf, Robots robot)
		{
			this.stream = stream;
			url = orig;
			buf = sbuf;
			this.robot = robot;
		}

		public void download()
		{
			UnicodeReader reader = new UnicodeReader(stream, "UTF-8"); //$NON-NLS-1$
			XMLReader r = new Parser();
			InputSource s = new InputSource();
			s.setCharacterStream(reader);
			try
			{
				r.setContentHandler(this);
				r.parse(s);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("nls")
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if( localName.equalsIgnoreCase("a") ) //$NON-NLS-1$
			{
				String szURL = attributes.getValue("href"); //$NON-NLS-1$
				URL relurl = null;
				relurl = URLUtils.newURL(url, szURL);

				if( !relurl.getProtocol().startsWith("http") || !robot.isAllowed(relurl) )
				{
					return;
				}
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Indexing:" + relurl); //$NON-NLS-1$
				}

				try
				{
					URLConnection urlcon = relurl.openConnection();
					urlcon.setConnectTimeout(URL_TIMEOUT);
					urlcon.setReadTimeout(URL_TIMEOUT);

					try( InputStream inp = urlcon.getInputStream() )
					{
						MimeEntry mimeType = getMimeEntryFromContentType(urlcon.getContentType());
						extractTextFromStream(getExtractors(mimeType), inp, mimeType, buf);
					}
				}
				catch( Exception e )
				{
					if( LOGGER.isDebugEnabled() )
					{
						LOGGER.debug("Error indexing second level url:" + relurl, e);
					}
				}
			}
		}
	}
}