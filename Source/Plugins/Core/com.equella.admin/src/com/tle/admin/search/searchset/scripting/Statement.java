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

package com.tle.admin.search.searchset.scripting;

public class Statement
{
	protected Block block;

	public Statement()
	{
		// Nothing to do here.
	}

	public void setBlock(Block block)
	{
		this.block = block;
		if( block != null )
		{
			block.setParent(this);
		}
	}

	public boolean isEmpty()
	{
		return block == null;
	}

	public Block getBlock()
	{
		return block;
	}

	public String toScript()
	{
		return block.toScript();
	}
}
