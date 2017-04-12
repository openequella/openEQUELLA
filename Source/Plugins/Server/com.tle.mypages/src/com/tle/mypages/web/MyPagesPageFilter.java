package com.tle.mypages.web;

import java.util.List;

import com.tle.beans.item.attachments.HtmlAttachment;

/**
 * @author Aaron
 */
public interface MyPagesPageFilter
{
	List<HtmlAttachment> filterPages(List<HtmlAttachment> pages);
}
