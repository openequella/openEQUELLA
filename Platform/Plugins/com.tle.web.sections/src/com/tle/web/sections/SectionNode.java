package com.tle.web.sections;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code SectionNode} is a tree node useful for inserting into a
 * {@link SectionTree}.
 * <p>
 * It contains a {@link Section}, an optional preferred id, an optional place
 * holder id, and a list of children (which can either be {@link Section}s or
 * other {@code SectionNode}s.
 * 
 * @author jmaginnis
 */
public class SectionNode
{
	private List<Object> children;
	private List<Object> innerChildren;
	private Section section;
	private String id;
	private String placeHolderId;

	public SectionNode()
	{
		// for spring
	}

	public SectionNode(String rootId)
	{
		this.id = rootId;
	}

	public SectionNode(String id, Section section)
	{
		this.id = id;
		this.section = section;
	}

	/**
	 * Gets the optional place holder id.
	 * 
	 * @return The place holder id
	 * @see SectionTree#getPlaceHolder(String)
	 */
	public String getPlaceHolderId()
	{
		return placeHolderId;
	}

	public void setPlaceHolderId(String placeHolderId)
	{
		this.placeHolderId = placeHolderId;
	}

	/**
	 * Return the list of child {@code Section}s.
	 * 
	 * @return A list of children (which can either be {@code Section}s or other
	 *         {@code SectionNode}s.
	 */
	public List<Object> getChildren()
	{
		return children;
	}

	/**
	 * Grr, must be <? extends Object> otherwise Spring tries to autowire it
	 * 
	 * @param children
	 */
	public void setChildren(List<? extends Object> children)
	{
		if( children != null )
		{
			this.children = new ArrayList<Object>(children);
		}
	}

	public Section getSection()
	{
		return section;
	}

	public void setSection(Section section)
	{
		this.section = section;
	}

	/**
	 * Get the preferred id of the <code>Section</code>.
	 * 
	 * @return The preferred id to register the {@code Section} with. If set to
	 *         <code>null</code> use the
	 *         {@link Section#getDefaultPropertyName()}
	 * @see Section#getDefaultPropertyName()
	 */
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public List<Object> getInnerChildren()
	{
		return innerChildren;
	}

	/**
	 * Grr, must be <? extends Object> otherwise Spring tries to autowire it
	 * 
	 * @param innerChildren
	 */
	public void setInnerChildren(List<? extends Object> innerChildren)
	{
		if( innerChildren != null )
		{
			this.innerChildren = new ArrayList<Object>(innerChildren);
		}
	}
}
