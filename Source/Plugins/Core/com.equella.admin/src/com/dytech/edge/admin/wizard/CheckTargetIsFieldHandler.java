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

package com.dytech.edge.admin.wizard;

import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.TargetListener;

public class CheckTargetIsFieldHandler implements TargetListener
{
	private final SchemaModel schema;

	public CheckTargetIsFieldHandler(SchemaModel schema)
	{
		this.schema = schema;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetAdded(java.lang.String)
	 */
	@Override
	public void targetAdded(String target)
	{
		SchemaNode node = schema.getNode(target);
		if( node == null )
		{
			System.err.println("Could not find schema node for target '" + target + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.admin.schema.TargetListener#targetRemoved(java.lang.String
	 * )
	 */
	@Override
	public void targetRemoved(String target)
	{
		// We don't care about this event.
	}
}
