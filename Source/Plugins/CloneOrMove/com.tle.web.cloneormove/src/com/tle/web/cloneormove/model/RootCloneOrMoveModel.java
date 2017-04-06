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
