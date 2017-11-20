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

package com.tle.web.cloneormove.model;

import java.util.List;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
public class RootCloneOrMoveModel
{
	@Bookmarked(name = "u")
	private String uuid;
	@Bookmarked(name = "v")
	private int version;
	@Bookmarked(name = "m")
	private boolean isMove;

	private List<SectionRenderable> sections;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public boolean getIsMove()
	{
		return isMove;
	}

	public void setIsMove(boolean isMove)
	{
		this.isMove = isMove;
	}

	public List<SectionRenderable> getSections()
	{
		return sections;
	}

	public void setSections(List<SectionRenderable> sections)
	{
		this.sections = sections;
	}
}
