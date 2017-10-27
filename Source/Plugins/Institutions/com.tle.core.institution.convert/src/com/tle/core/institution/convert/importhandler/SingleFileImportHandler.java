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

public class SingleFileImportHandler<T> extends AbstractImportHandler<T>
{
	private final List<T> nodes;

	public SingleFileImportHandler(SubTemporaryFile folder, String path, XmlHelper xmlHelper, XStream xstream)
	{
		super(xmlHelper, xstream);
		this.nodes = xmlHelper.readXmlFile(folder, path, getXStream());
	}

	@Override
	public int getNodeCount()
	{
		return nodes.size();
	}

	@Override
	public Iterator<T> iterateNodes()
	{
		return nodes.iterator();
	}
}
