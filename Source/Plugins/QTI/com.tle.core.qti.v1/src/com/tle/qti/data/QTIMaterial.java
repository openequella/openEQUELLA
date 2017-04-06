package com.tle.qti.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a QTI material node, which can contain a number of different
 * elements
 * 
 * @author will
 */
public class QTIMaterial implements Serializable
{
	private static final long serialVersionUID = 1L;
	private List<QTIMaterialElement> elements = new ArrayList<QTIMaterialElement>();

	public QTIMaterial()
	{

	}

	public QTIMaterial(List<QTIMaterialElement> elements)
	{
		this.elements = elements;
	}

	public QTIMaterial(QTIMaterialElement element)
	{
		this.elements.add(element);
	}

	public void setElements(List<QTIMaterialElement> elements)
	{
		this.elements = elements;
	}

	public List<QTIMaterialElement> getElements()
	{
		return elements;
	}

	public void add(QTIMaterialElement element)
	{
		elements.add(element);
	}

	public void addAll(List<QTIMaterialElement> elements)
	{
		this.elements.addAll(elements);
	}

	public String getHtml()
	{
		StringBuilder html = new StringBuilder();

		for( QTIMaterialElement ele : elements )
		{
			html.append(ele.getHtml());
		}
		return html.toString();
	}
}
