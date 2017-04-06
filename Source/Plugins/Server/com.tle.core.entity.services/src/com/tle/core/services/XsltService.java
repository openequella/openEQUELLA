/*
 * Created on Oct 25, 2005
 */
package com.tle.core.services;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.URIResolver;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.filesystem.FileHandle;

/**
 * @author Nicholas Read
 */
public interface XsltService
{
	/**
	 * Transforms the input with the XSLT. The service may cache the compiled
	 * form of the XSLT.
	 */
	String transform(FileHandle handle, String xslt, Reader input);

	/**
	 * Transforms the input with the XSLT. The service may cache the compiled
	 * form of the XSLT.
	 */
	String transform(FileHandle handle, String xslt, Reader input, URIResolver resolver);

	/**
	 * Transforms the input with the XSLT. The service may cache the compiled
	 * form of the XSLT.
	 */
	String transform(FileHandle handle, String xslt, PropBagEx input, boolean omitXmlDeclaration);

	/**
	 * Transforms the input with the XSLT. The service may cache the compiled
	 * form of the XSLT.
	 */
	String transform(FileHandle handle, String xslt, PropBagEx input, URIResolver resolver, boolean omitXmlDeclaration);

	/**
	 * Transforms the input with the XSLT. No caching
	 */
	String transformFromXsltString(String xslt, PropBagEx input);

	/**
	 * Performs a once off tranformation with no caching. Do not use this for
	 * common XSLT operations, especially where the XSLT is stored in the
	 * filestore.
	 */
	String onceOffTransform(InputStream xslt, InputStream input);

	void cacheXslt(String xslt);
}
