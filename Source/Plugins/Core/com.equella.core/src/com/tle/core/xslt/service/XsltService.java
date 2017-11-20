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

package com.tle.core.xslt.service;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.URIResolver;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.FileHandle;

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
