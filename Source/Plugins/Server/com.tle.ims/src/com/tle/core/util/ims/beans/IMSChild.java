/*
 * Created on Jun 14, 2005
 */
package com.tle.core.util.ims.beans;

import com.dytech.common.xml.XMLDataChild;

/**
 * @author jmaginnis
 */
public abstract class IMSChild extends IMSWrapper implements XMLDataChild
{
	private static final long serialVersionUID = 1L;

	protected IMSChild parent;

	@Override
	public void setParentObject(Object parent)
	{
		this.parent = (IMSChild) parent;
	}

	@Override
	protected String getFullBase()
	{
		if( parent == null )
		{
			return super.getFullBase();
		}
		else
		{
			return parent.getFullBase() + super.getFullBase();
		}
	}

	public IMSChild getParent()
	{
		return parent;
	}

	public IMSManifest getRootManifest()
	{
		if( parent == null )
		{
			return (IMSManifest) this;
		}
		else
		{
			return parent.getRootManifest();
		}
	}
}
