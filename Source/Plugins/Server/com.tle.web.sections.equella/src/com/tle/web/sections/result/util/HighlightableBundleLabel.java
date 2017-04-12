package com.tle.web.sections.result.util;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringEscapeUtils;

import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextUtils;

public class HighlightableBundleLabel extends BundleLabel
{
	private Collection<String> words;
	private boolean truncate;

	public HighlightableBundleLabel(Object bundle, Label defaultLabel, BundleCache bundleCache,
		Collection<String> words, boolean truncate)
	{
		super(bundle, defaultLabel, bundleCache);
		this.words = words;
		this.truncate = truncate;
	}

	public HighlightableBundleLabel(Object bundle, String defaultString, BundleCache bundleCache,
		Collection<String> words, boolean truncate)
	{
		super(bundle, defaultString, bundleCache);
		this.words = words;
		this.truncate = truncate;
	}

	@Override
	public boolean isHtml()
	{
		return true;
	}

	@Override
	public String getText()
	{
		if( words == null )
		{
			words = Collections.emptyList();
		}
		String text = super.getText();
		if( truncate )
		{
			text = TextUtils.INSTANCE.mostOccurences(text, TextUtils.DESCRIPTION_LENGTH, words);
		}
		return TextUtils.INSTANCE.highlight(StringEscapeUtils.escapeHtml(text), words);
	}

	public void setWords(Collection<String> words)
	{
		this.words = words;
	}

	public void setTruncate(boolean truncate)
	{
		this.truncate = truncate;
	}
}
