package com.tle.core.mimetypes;

import com.tle.beans.item.attachments.Attachment;

public interface RegisterMimeTypeExtension<T extends Attachment>
{
	String getMimeType(T attachment);
}
