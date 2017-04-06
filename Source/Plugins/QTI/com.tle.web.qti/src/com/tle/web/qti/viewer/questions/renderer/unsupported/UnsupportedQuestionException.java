package com.tle.web.qti.viewer.questions.renderer.unsupported;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Aaron
 */
public class UnsupportedQuestionException extends RuntimeException
{
	private final String nodeName;
	private final boolean element;

	public UnsupportedQuestionException(String nodeName)
	{
		this(nodeName, false);
	}

	/**
	 * @param element True if it is a sub-part of the question that can't be
	 *            rendered and not the interaction itself
	 */
	public UnsupportedQuestionException(String nodeName, boolean element)
	{
		this.nodeName = nodeName;
		this.element = element;
	}

	@SuppressWarnings("nls")
	@Override
	public String getMessage()
	{
		return CurrentLocale.get("com.tle.web.qti.notsupported." + (element ? "element" : "question"), nodeName);
	}
}
