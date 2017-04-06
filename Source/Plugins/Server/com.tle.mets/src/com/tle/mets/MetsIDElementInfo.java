package com.tle.mets;

import com.dytech.devlib.PropBagEx;

import edu.harvard.hul.ois.mets.helper.MetsIDElement;

/**
 * @author Aaron
 */
public class MetsIDElementInfo<T extends MetsIDElement>
{
	private final T elem;
	private final String mimeType;
	private final PropBagEx xml;

	public MetsIDElementInfo(T elem, String mimeType, PropBagEx xml)
	{
		this.elem = elem;
		this.mimeType = mimeType;
		this.xml = xml;
	}

	public T getElem()
	{
		return elem;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public PropBagEx getXml()
	{
		return xml;
	}
}