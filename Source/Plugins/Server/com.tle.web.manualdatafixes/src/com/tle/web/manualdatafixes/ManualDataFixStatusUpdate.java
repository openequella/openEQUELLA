package com.tle.web.manualdatafixes;

import java.util.Map;

import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;

public class ManualDataFixStatusUpdate extends AbstractDOMResult
{
	private Map<String, FullAjaxCaptureResult> updates;
	private boolean finished;

	public ManualDataFixStatusUpdate(AbstractDOMResult result)
	{
		super(result);
	}

	public boolean isFinished()
	{
		return finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	public Map<String, FullAjaxCaptureResult> getUpdates()
	{
		return updates;
	}

	public void setUpdates(Map<String, FullAjaxCaptureResult> updates)
	{
		this.updates = updates;
	}
}
