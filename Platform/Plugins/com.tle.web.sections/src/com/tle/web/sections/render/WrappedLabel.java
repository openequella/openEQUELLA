package com.tle.web.sections.render;

public class WrappedLabel implements ProcessedLabel
{
	private final Label label;
	private int maxBodyLength;
	private boolean inline;
	private boolean showAltText;

	private String truncText;

	public WrappedLabel(Label label, int maxBodyLength)
	{
		this(label, maxBodyLength, false, true);
	}

	public WrappedLabel(Label label, int maxBodyLength, boolean showAltText)
	{
		this(label, maxBodyLength, showAltText, true);
	}

	public WrappedLabel(Label label, int maxBodyLength, boolean showAltText, boolean inline)
	{
		this.label = label;
		this.maxBodyLength = maxBodyLength;
		this.showAltText = showAltText;
		this.inline = inline;
	}

	public WrappedLabel setMaxBodyLength(int maxBodyLength)
	{
		this.maxBodyLength = maxBodyLength;
		return this;
	}

	public int getMaxBodyLength()
	{
		return maxBodyLength;
	}

	public WrappedLabel setShowAltText(boolean showAltText)
	{
		this.showAltText = showAltText;
		return this;
	}

	public boolean isShowAltText()
	{
		return showAltText;
	}

	public WrappedLabel setInline(boolean inline)
	{
		this.inline = inline;
		return this;
	}

	public boolean isInline()
	{
		return inline;
	}

	private void build()
	{
		String fullText = label.getText();
		if( maxBodyLength < 0 || fullText.length() < maxBodyLength )
		{
			truncText = fullText;
			return;
		}
		truncText = TextUtils.INSTANCE.ensureWrap(fullText, maxBodyLength, -1, label.isHtml());
	}

	@Override
	public String getText()
	{
		if( truncText == null )
		{
			build();
		}
		return truncText;
	}

	@Override
	public Label getUnprocessedLabel()
	{
		return label;
	}

	@Override
	public boolean isHtml()
	{
		return label.isHtml();
	}
}
