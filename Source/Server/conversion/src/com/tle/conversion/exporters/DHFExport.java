package com.tle.conversion.exporters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import jp.co.antenna.dhf.v1.DHFException;
import jp.co.antenna.dhf.v1.DHFhtmlInfo;
import jp.co.antenna.dhf.v1.DHFnativeV1;

import com.tle.conversion.Converter;

/**
 * A wrapper for the DHF exporter. Exports to HTML from word DOC, XLS, PPT.
 * 
 * @author bmillar
 */
public class DHFExport implements Export
{
	private final Converter exporter;

	public DHFExport(Converter exporter)
	{
		this.exporter = exporter;
	}

	@Override
	public void exportFile(String in, String out) throws IOException
	{
		try
		{
			// Convert using DHF:
			DHFhtmlInfo info = new DHFhtmlInfo();
			info.setGroupName(DHFhtmlInfo.UTF8);
			info.setDefLangName(DHFhtmlInfo.ENGLISH);
			info.setBigEndian(true);
			info.setOption(0);
			info.setSheetID(0);
			DHFnativeV1.getDHFhtml(new File(in), new File(out), info);

			// Convert the generated html to have jpegs instead of emf:
			convertImagesInFile(out);
		}
		catch( DHFException ex )
		{
			throw new RuntimeException("Error converting document", ex);
		}
	}

	/**
	 * Converts all src attributes in an html document that end in .emf or .wmf
	 * to point to converted jpegs.
	 * 
	 * @param parent The html File - needed for relative paths.
	 * @param html The html to scan for the scr attributes.
	 * @return The converted html.
	 */
	public String convertImages(File parent, String html)
	{
		StringBuilder buf = new StringBuilder();

		// Scan the string for < and then src:
		char c = ' ';
		int index = 0;
		int length = html.length();

		while( index < length )
		{
			// Find tag:
			while( c != '<' && index < length )
			{
				c = html.charAt(index);
				buf.append(c);
				++index;
			}

			// Finished?
			if( index >= length )
			{
				break;
			}

			// Found a tag:
			int close = html.indexOf('>', index);
			if( close != -1 ) // valid?
			{
				String tag = html.substring(index, close);
				int nSrc = tag.indexOf("src");
				if( nSrc != -1 ) // got src?
				{
					nSrc = tag.indexOf('\"', nSrc);
					if( nSrc != -1 ) // need a value in quotes
					{
						int nEndSrc = tag.indexOf('\"', nSrc + 1);
						if( nEndSrc != -1 ) // need closing quotes
						{
							String src = tag.substring(nSrc + 1, nEndSrc);
							String srcModified = src.trim().toLowerCase();
							String newSrc = src;
							if( srcModified.endsWith(".emf") || srcModified.endsWith(".wmf") )
							{
								newSrc = convertToJpeg(parent, src, exporter);
							}
							try
							{
								newSrc = URLEncoder.encode(newSrc, "UTF-8").replaceAll("%2F", "/");
							}
							catch( UnsupportedEncodingException e )
							{
								// IGNORE
							}

							// Jira TLE-4785
							// Linux DHF conversion seems to output extraneous
							// characters in the filename at XXX
							// eg. doc_Test.doc.filesXXX/image0.jpg
							int filesIndex = newSrc.indexOf(".files");
							if( filesIndex > 0 )
							{
								newSrc = newSrc.substring(0, filesIndex) + ".files"
									+ newSrc.substring(newSrc.indexOf('/', filesIndex));
							}

							// Write the tag with the new src:
							String tagBegin = tag.substring(0, nSrc);
							buf.append(tagBegin);
							buf.append('\"');
							buf.append(newSrc);
							String tagEnd = tag.substring(nEndSrc);
							buf.append(tagEnd);

							// Increment index:
							index += tagBegin.length();
							index += 1;
							index += src.length();
							index += tagEnd.length();
						}
					}
				}
				else
				{
					buf.append(tag); // just write the tag as is
					index += tag.length();
				}
			}
			else
			{
				buf.append(c);
				++index;
			}

			c = ' ';
		}

		return buf.toString();
	}

	private String convertToJpeg(File parent, String src, Converter exporter)
	{
		// Only convert relative files:
		if( src.indexOf("://") > 0 )
		{
			return src;
		}

		// Strip the extension:
		int dot = src.lastIndexOf(".");
		if( dot == -1 )
		{
			return src;
		}
		String outFile = src.substring(0, dot) + ".jpeg";

		src = parent.getParent() + File.separator + src;
		String out = parent.getParent() + File.separator + outFile;

		try
		{
			exporter.exportFile(src, out);
		}
		catch( Exception ex )
		{
			return src;
		}
		return outFile;
	}

	/**
	 * Reads an html file converts image src references and writes the file back
	 * to the same place.
	 * 
	 * @param file
	 * @throws IOException
	 * @see #convertImages(File, String)
	 */
	public void convertImagesInFile(String file) throws IOException
	{
		StringBuffer html = new StringBuffer();
		File htmlFile = new File(file);
		try( FileInputStream in = new FileInputStream(htmlFile) )
		{
			byte[] buf = new byte[1024];
			int read = in.read(buf);
			while( read > 0 )
			{
				html.append(new String(buf, 0, read, "UTF-8"));
				read = in.read(buf);
			}
		}

		// Get rid of crap that DHF spits out for first three chars:
		int firstLT = html.indexOf("<");
		if( firstLT > 0 )
		{
			html = html.delete(0, firstLT);
		}

		// Convert the file:
		String conv = convertImages(htmlFile, html.toString());

		// Write the file:
		FileOutputStream out = new FileOutputStream(file);
		out.write(conv.getBytes("UTF-8"));
		out.close();
	}

	@Override
	public Collection<String> getInputTypes()
	{
		return Arrays.asList("doc", "xls", "ppt", "pps");
	}

	@Override
	public Collection<String> getOutputTypes()
	{
		return Collections.singleton("html");
	}
}