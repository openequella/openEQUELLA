package com.tle.web.upload;

import java.io.Serializable;

import org.apache.commons.fileupload.ProgressListener;

import com.tle.core.progress.PercentageProgressCallback;
import com.tle.core.progress.ProgressCallback;

public class ProgressListenerImpl implements ProgressListener, Serializable
{
	private static final long serialVersionUID = 1L;

	private final PercentageProgressCallback progressCallback;
	private boolean contentLengthKnown = false;
	private int percentDone = 0;

	public ProgressListenerImpl()
	{
		progressCallback = new PercentageProgressCallback();
	}

	public ProgressCallback getProgressCallback()
	{
		return progressCallback;
	}

	@Override
	public void update(long bytesRead, long contentLength, int items)
	{
		if( -1 != contentLength )
		{
			contentLengthKnown = true;
		}
		if( progressCallback.getTotalSize() != contentLength )
		{
			progressCallback.setTotalSize(contentLength);
		}
		progressCallback.setBytesRead(bytesRead);

		if( contentLengthKnown )
		{
			percentDone = (int) Math.round(100.00 * ((float) bytesRead / (float) contentLength));
			// total size can be -1 when it's unknown
			if( percentDone > 100 )
			{
				percentDone = 100;
			}
			if( percentDone < 0 )
			{
				percentDone = -1;
			}
		}
		else
		{
			percentDone = -1;
		}
		progressCallback.setPercent(percentDone);

		if( bytesRead == contentLength )
		{
			progressCallback.setFinished();
		}
	}

	public boolean isContentLengthKnown()
	{
		return contentLengthKnown;
	}

	public void setContentLengthKnown(boolean contentLengthKnown)
	{
		this.contentLengthKnown = contentLengthKnown;
	}

	public int getPercentDone()
	{
		return percentDone;
	}

	public void setPercentDone(int percentDone)
	{
		this.percentDone = percentDone;
	}
}
