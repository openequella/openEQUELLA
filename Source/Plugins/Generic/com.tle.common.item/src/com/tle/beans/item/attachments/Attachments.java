package com.tle.beans.item.attachments;

import java.util.Iterator;
import java.util.List;

/*
 * @author aholland
 */
public interface Attachments extends Iterable<IAttachment>
{
	/**
	 * @param <T>
	 * @param attachmentType
	 * @return An UNMODIFIABLE list
	 */
	<T extends IAttachment> List<T> getList(AttachmentType attachmentType);

	ImsAttachment getIms();

	<T extends IAttachment> Iterator<T> getIterator(AttachmentType attachmentType);

	List<CustomAttachment> getCustomList(String type);

	CustomAttachment getFirstCustomOfType(String type);

	IAttachment getAttachmentByUuid(String uuid);

	IAttachment getAttachmentByFilename(String filename);

	/**
	 * Does a UUID comparison.
	 * 
	 * @param attachment
	 * @return
	 */
	boolean contains(IAttachment attachment);
}