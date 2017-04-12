package com.tle.web.viewitem;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;

public interface FilestoreContentFilter
{
	FilestoreContentStream filter(FilestoreContentStream contentStream, HttpServletRequest request,
		HttpServletResponse response) throws IOException;

	boolean canView(Item item, IAttachment attachment);
}
