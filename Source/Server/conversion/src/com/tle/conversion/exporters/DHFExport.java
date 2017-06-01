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

package com.tle.conversion.exporters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Base64;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParseContext;

import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import com.tle.conversion.Converter;

/**
 * A wrapper for the DHF exporter. Exports to HTML from word DOC, XLS, PPT.
 *
 * @author ddelblanco
 */
public class DHFExport implements Export
{
	private final Converter exporter;
    final AutoDetectParser tikaParser = new AutoDetectParser();
    private Map<String,String> embedded;

	public DHFExport(Converter exporter)
	{
		this.exporter = exporter;
	}

	@Override
    public void exportFile(String in, String out) throws IOException
    {
        try
        {
            embedded = new HashMap<>();
            ParseContext context = new ParseContext();
            context.set(Parser.class, new ExtractParser());
            Metadata metadata = new Metadata();
            Path path = Paths.get(in);
            InputStream stream = TikaInputStream.get(path);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            TransformerHandler handler = factory.newTransformerHandler();
            handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
            handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
            handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            handler.setResult(new StreamResult(outputStream));
            ContentHandler contentHandler = new BodyContentHandler(handler);

            tikaParser.parse(stream,contentHandler,metadata,context);

            OutputStream outputStreamFile = new FileOutputStream (out);
            outputStreamFile.write(outputStream.toByteArray());

            convertImagesInFile(out);

        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            throw new RuntimeException("Error converting document", ex);
        }
    }

    /**
     * Extracts the images to a hashmap in base64 code
     */
    public class ExtractParser extends AbstractParser {

        public Set<MediaType> getSupportedTypes(ParseContext context) {
            // Everything AutoDetect parser does
            return tikaParser.getSupportedTypes(context);
        }
        public void parse(
                InputStream stream, ContentHandler handler,
                Metadata metadata, ParseContext context)
                throws IOException, SAXException, TikaException {

            byte[] bytes = IOUtils.toByteArray(stream);
            String encoded = Base64.getEncoder().encodeToString(bytes);
            embedded.put(metadata.get("resourceName"),encoded);


        }
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

    /**
     * Reads an html file converts image src references with the base 64 code and
     * writes the file back to the same place.
     *
     * @param file
     * @throws IOException
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
        String htmlContent = html.toString();

        String strToFind = "<img src=\"embedded:";
        int lastIndex = 0;

        while(lastIndex != -1){

            lastIndex = htmlContent.indexOf(strToFind,lastIndex);

            if(lastIndex != -1){
                int auxIndex=lastIndex + strToFind.length();
                int auxIndex2=htmlContent.indexOf("\"",auxIndex);
                String embeddedIndex = htmlContent.substring(auxIndex, auxIndex2);
                if (embedded.containsKey(embeddedIndex)) {
                    String base64Image = embedded.get(embeddedIndex);
                    String srcText = "data:image/png;base64, " + base64Image;
                    int postIndex = lastIndex + 10 + srcText.length() + 1;
                    htmlContent = htmlContent.substring(0, lastIndex + 10) + srcText + htmlContent.substring(auxIndex2);
                    lastIndex = postIndex;
                }else {
                    lastIndex += strToFind.length();
                }
            }
        }
        // Write the file:
        FileOutputStream out = new FileOutputStream(file);
        out.write(htmlContent.getBytes("UTF-8"));
        out.close();
    }

}
