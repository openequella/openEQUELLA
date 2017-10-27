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

package com.tle.core.institution.convert.importhandler;

import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.institution.convert.XmlHelper;

/**
 * Importer for new node format that has a single node XML per file. Files are
 * sequentially numbered, and it is important that they are read in that order
 * for correct top-down insertion of nodes.
 */
public class MultiFileImportHandler<T> extends AbstractImportHandler<T>
{
	protected final SubTemporaryFile folder;
	protected final List<String> files;

	public MultiFileImportHandler(SubTemporaryFile folder, XmlHelper xmlHelper, XStream xstream)
	{
		super(xmlHelper, xstream);
		this.folder = folder;
		files = xmlHelper.getXmlFileListOrdered(folder);
	}

	@Override
	public int getNodeCount()
	{
		return files.size();
	}

	@Override
	public Iterator<T> iterateNodes()
	{
		final XStream xs = getXStream();
		final Iterator<String> iter = files.iterator();
		return new Iterator<T>()
		{
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}

			@Override
			@SuppressWarnings("unchecked")
			public T next()
			{
				return (T) xmlHelper.readXmlFile(folder, iter.next(), xs);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
