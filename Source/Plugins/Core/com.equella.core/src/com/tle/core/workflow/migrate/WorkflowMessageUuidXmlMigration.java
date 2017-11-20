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

package com.tle.core.workflow.migrate;

import java.util.Iterator;
import java.util.UUID;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
@SuppressWarnings("nls")
public class WorkflowMessageUuidXmlMigration extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		String xpath = "com.tle.common.workflow.WorkflowItemStatus/comments/com.tle.common.workflow.WorkflowMessage";
		Iterator<PropBagEx> iter = xml.iterateAll(xpath);
		boolean modified = false;

		while( iter.hasNext() )
		{
			PropBagEx comment = iter.next();
			if( Check.isEmpty(comment.getNode("uuid")) )
			{
				comment.setNode("uuid", UUID.randomUUID().toString());
				modified = true;
			}
		}

		return modified;
	}
}
