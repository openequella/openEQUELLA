package com.tle.web.sections.standard.renderers.fancybox;

import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.standard.dialog.renderer.DialogRenderer;

@SuppressWarnings("nls")
public class FancyBoxOptions
{
	private Bookmark href;
	private String type;
	private String title;
	private Object width = DialogRenderer.AUTO_SIZE;
	private Object height = DialogRenderer.AUTO_SIZE;
	private int padding = 0;
	private int margin = 0;
	private ElementId inlineElement;
	private boolean modal;
	private JSFunction dialogOpenedCallback;
	private JSFunction dialogClosedCallback;

	public Object[] getParameters()
	{
		ObjectExpression options = new ObjectExpression();
		if( href != null )
		{
			options.put("href", href.getHref());
		}
		boolean autoWidth = width.equals(DialogRenderer.AUTO_SIZE);
		boolean autoHeight = height.equals(DialogRenderer.AUTO_SIZE);
		boolean bothAuto = autoWidth && autoHeight;
		if( type.equals("iframe") && (autoWidth || autoHeight) )
		{
			throw new SectionsRuntimeException("You must supply height and width for iframe's");
		}
		if( !bothAuto )
		{
			options.put("width", width);
			options.put("height", height);
		}
		options.put("autoDimensions", bothAuto);
		options.put("autoScale", false);
		if( title != null )
		{
			options.put("title", title);
		}
		options.put("type", type);
		if( modal )
		{
			options.put("modal", true);
		}
		options.put("scrolling", false);

		options.put("padding", padding);
		options.put("margin", margin);

		if( dialogOpenedCallback != null )
		{
			options.put("onComplete", dialogOpenedCallback);
		}
		if( dialogClosedCallback != null )
		{
			options.put("onClosed", dialogClosedCallback);
		}

		if( inlineElement != null )
		{
			return new Object[]{inlineElement, options};
		}
		return new Object[]{options};
	}

	public void setHref(Bookmark href)
	{
		this.href = href;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setHeight(String height)
	{
		if( !Check.isEmpty(height) )
		{
			if( height.equals(DialogRenderer.AUTO_SIZE) || height.contains("%") )
			{
				this.height = height;
			}
			else
			{
				this.height = JSUtils.cssToPixels(height);
			}
		}

	}

	public void setWidth(String width)
	{
		if( !Check.isEmpty(width) )
		{
			if( width.equals(DialogRenderer.AUTO_SIZE) || width.contains("%") )
			{
				this.width = width;
			}
			else
			{
				this.width = JSUtils.cssToPixels(width);
			}
		}
	}

	public void setTitle(String title)
	{
		if( !Check.isEmpty(title) )
		{
			this.title = title;
		}
	}

	public void setModal(boolean modal)
	{
		this.modal = modal;
	}

	public void setInlineElement(ElementId inlineElement)
	{
		this.inlineElement = inlineElement;
	}

	public ElementId getInlineElement()
	{
		return inlineElement;
	}

	public void setPadding(int padding)
	{
		this.padding = padding;
	}

	public void setMargin(int margin)
	{
		this.margin = margin;
	}

	public JSFunction getDialogOpenedCallback()
	{
		return dialogOpenedCallback;
	}

	public void setDialogOpenedCallback(JSFunction dialogOpenedCallback)
	{
		this.dialogOpenedCallback = dialogOpenedCallback;
	}

	public JSFunction getDialogClosedCallback()
	{
		return dialogClosedCallback;
	}

	public void setDialogClosedCallback(JSFunction dialogClosedCallback)
	{
		this.dialogClosedCallback = dialogClosedCallback;
	}
}
