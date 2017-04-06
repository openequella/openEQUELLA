package com.tle.core.connectors.canvas.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class CanvasErrorBean
{
	private List<CanvasErrorMessageBean> errors;

	public List<CanvasErrorMessageBean> getErrors()
	{
		return errors;
	}

	public void setErrors(List<CanvasErrorMessageBean> errors)
	{
		this.errors = errors;
	}

	public static class CanvasErrorMessageBean
	{
		private String message;

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}
	}
}
