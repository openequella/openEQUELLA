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

package com.dytech.edge.common;

import static com.dytech.edge.common.Constants.DOUBLE_QUOTE;
import static com.dytech.edge.common.Constants.SINGLE_QUOTE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.common.Check;
import com.tle.common.Pair;

/**
 * Finds all URLs withing a piece of text (only HTTP or HTTPS URLs though) This
 * could probably be refactored into ConvertHtmlService
 * 
 * @author aholland
 */
public class UrlExtractor
{
	public static final String HTTP = "http"; //$NON-NLS-1$
	public static final String HTTPS = "https"; //$NON-NLS-1$
	private static final String[] DEFAULT_PROTOCOLS = new String[]{HTTP, HTTPS};

	private static final String NAMESPACE_START = "ns=\""; //$NON-NLS-1$	

	/**
	 * This really would depend on the context of the URL, we have to be mind
	 * readers to a certain extent.
	 */
	@SuppressWarnings("nls")
	private static final String[] URL_ENDS = new String[]{"#", " ", "\t", "\n", "\r", DOUBLE_QUOTE, "]", "<"};

	/**
	 * E.g. someone has written (http://www.google.com.au) in a text field.
	 * (Blame Yih) Since these are valid URL characters, the second of each pair
	 * should be treated as a URL terminating character IF AND ONLY IF the
	 * previous character before the URL start is the first of the pair.
	 */
	private static final List<Pair<String, String>> SURROUNDERS = new ArrayList<Pair<String, String>>();
	static
	{
		SURROUNDERS.add(new Pair<String, String>("(", ")")); //$NON-NLS-1$//$NON-NLS-2$
		SURROUNDERS.add(new Pair<String, String>(SINGLE_QUOTE, SINGLE_QUOTE));
	}

	private final String[] protocols;
	private final boolean testNamespaceAttribute;

	public UrlExtractor(boolean testNamespaceAttribute, String... protocols)
	{
		this.testNamespaceAttribute = testNamespaceAttribute;
		if( protocols.length > 0 )
		{
			this.protocols = protocols;
		}
		else
		{
			this.protocols = DEFAULT_PROTOCOLS;
		}
	}

	public Set<String> grabUrls(final String val)
	{
		final Set<String> urls = new HashSet<String>();
		int offset = 0;
		while( (offset = findUrlStart(val, offset)) >= 0 )
		{
			// initially assume the URL is the rest of the value
			int urlEnd = val.length();

			// check to make sure that index is not a namespace.
			int nsStart = offset - NAMESPACE_START.length();
			boolean namespace = (testNamespaceAttribute && nsStart >= 0 && val.substring(nsStart, offset).equals(
				NAMESPACE_START));
			if( !namespace )
			{
				// find earliest ending sequence
				for( String element : URL_ENDS )
				{
					int end = val.indexOf(element, offset);
					if( end >= 0 )
					{
						urlEnd = Math.min(urlEnd, end);
					}
				}

				// find possible surrounders that could be valid URL chars
				for( Pair<String, String> surround : SURROUNDERS )
				{
					int end = val.indexOf(surround.getSecond(), offset);

					// if this is before our already established end, then we
					// will need to check the the start of this URL and ignore
					// the character appropriately. Also look at the char before
					// URL to see if it's the other corresponding surrounder
					if( end >= 0 && end < urlEnd && offset > 0
						&& val.substring(offset - 1, offset).equals(surround.getFirst()) )
					{
						// we have a new end
						urlEnd = end;
					}
				}

				final String url = val.substring(offset, urlEnd);
				if( !Check.isEmpty(url) )
				{
					urls.add(url);
				}
			}
			else
			{
				// ignore this URL and move along to the end quote of the
				// namespace attribute
				offset = val.indexOf(DOUBLE_QUOTE, offset);
			}
			offset = urlEnd;
		}
		return urls;
	}

	private int findUrlStart(String text, int currentOffset)
	{
		if( text != null && currentOffset >= 0 )
		{
			// find earliest next protocol
			int index = Integer.MAX_VALUE;
			for( String protocol : protocols )
			{
				int protoIndex = text.indexOf(protocol + "://", currentOffset); //$NON-NLS-1$
				if( protoIndex >= 0 )
				{
					index = Math.min(index, protoIndex);
				}
			}

			// nothing was found
			if( index == Integer.MAX_VALUE )
			{
				return -1;
			}
			return index;
		}
		return -1;
	}
}
