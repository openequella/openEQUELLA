/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.scripting.objects;

import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.BinaryDataScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.scripting.types.XmlScriptType;

/**
 * Referenced by the 'attachments' variable in script
 * 
 * @author aholland
 */
public interface AttachmentsScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "attachments"; //$NON-NLS-1$

	/**
	 * Adds an attachment object to the item. Obtain this object using
	 * createTextFileAttachment or createLinkAttachment
	 * 
	 * @param attachment The attachment to add to the item.
	 * @return The attachment you added.
	 */
	AttachmentScriptType add(AttachmentScriptType attachment);

	/**
	 * Links a file already in the staging area to the item as an attachment. If
	 * the filename specified cannot be found an exception will be thrown. Use
	 * FileScriptingObject.exists(String) to determine if the file can be found.
	 * 
	 * @param filename The path to the file, relative to the item
	 * @param description The text to display as a link to this attachment
	 * @return The new attachment
	 */
	AttachmentScriptType addExistingFileAsAttachment(String filename, String description);

	/**
	 * Removes an attachment from the item. If the attachment is a physical
	 * file, it does not delete the file from the file system. Use
	 * FileScriptingObject.deleteFile(String) to delete the actual file.
	 * 
	 * @param attachment The attachment to remove from the item.
	 */
	void remove(AttachmentScriptType attachment);

	/**
	 * Remove all attachments from the item. If any of the attachments are a
	 * physical file, it does not delete the file from the file system. Use
	 * FileScriptingObject.deleteFile(String) to delete the actual files.
	 */
	void clear();

	/**
	 * Lists all attachments on the item, this will include any attachments you
	 * have added.
	 * 
	 * @return A list of attachments
	 */
	List<AttachmentScriptType> list();

	/**
	 * Lists all attachments on the given item
	 * 
	 * @param item The item to list the attachments of
	 * @return A list of attachments
	 */
	List<AttachmentScriptType> listForItem(ItemScriptType item);

	/**
	 * Gets an attachment based on the uuid of the attachment.
	 * 
	 * @param uuid The system generated UUID for the attachment.
	 * @return The attachment with the specified UUID, or null if not found.
	 */
	AttachmentScriptType getByUuid(String uuid);

	/**
	 * Gets an attachment based on the filename of the attachment. Does not look
	 * in the staging directory, only on attachments that are added to the item.
	 * 
	 * @param filename The filename of the attachment
	 *            (AttachmentScriptType.getFilename() or
	 *            AttachmentScriptType.getUrl())
	 * @return The attachment with the specified filename or URL, or null if not
	 *         found.
	 */
	AttachmentScriptType getByFilename(String filename);

	/**
	 * Creates an attachment for the given text contents Does not add this
	 * attachment to the item, you must explicitly call attachments.add(XX) to
	 * do so.
	 * 
	 * @param filename The filename of the attachment, relative to the item.
	 * @param description The text to display as a link to this attachment.
	 * @param contents The text contents of the file.
	 * @return An attachment of type FILE.
	 */
	AttachmentScriptType createTextFileAttachment(String filename, String description, String contents);

	/**
	 * Creates an HTML attachment for the given text contents. Much the same as
	 * createTextFileAttachment(String,String,String) but the attachments
	 * created by this method can be used in conjuction with a My Pages control.
	 * Does not add this attachment to the item, you must explicitly call
	 * attachments.add(XX) to do so.
	 * 
	 * @param filename The filename of the attachment, relative to the item.
	 * @param description The text to display as a link to this attachment.
	 * @param contents The text contents of the file.
	 * @return An attachment of type FILE.
	 */
	AttachmentScriptType createHtmlAttachment(String filename, String description, String contents);

	/**
	 * Creates an attachment for the given binary data contents Does not add
	 * this attachment to the item, you must explicitly call attachments.add(XX)
	 * to do so.
	 * 
	 * @param filename The filename of the attachment, relative to the item.
	 * @param description The text to display as a link to this attachment.
	 * @param contents The binary contents of the file. At this stage, a
	 *            BinaryDataScriptType may only be retrieved from a
	 *            ResponseScriptType.
	 * @return An attachment of type FILE.
	 */
	AttachmentScriptType createBinaryFileAttachment(String filename, String description, BinaryDataScriptType contents);

	/**
	 * Sets the text contents of the supplied attachment. If you attempt to edit
	 * an attachment that is not of type FILE, HTML or IMSRES then an exception
	 * will be thrown.
	 * 
	 * @param attachment An attachment of type FILE, HTML or IMSRES with text
	 *            content.
	 * @param newContents The new text contents of the file associated with this
	 *            attachment.
	 * @return The attachment that was edited
	 */
	AttachmentScriptType editTextFileAttachment(AttachmentScriptType attachment, String newContents);

	/**
	 * Creates a link to an external URL Does not add this attachment to the
	 * item, you must explicitly call attachments.add(XX) to do so.
	 * 
	 * @param url The URL of the link
	 * @param description A display name for the link
	 * @return An attachment of type LINK
	 */
	AttachmentScriptType createLinkAttachment(String url, String description);

	/**
	 * Creates an attachment of type CUSTOM.
	 * <p>
	 * Based on the customType specified you can set certain custom properties
	 * on the returned attachment. For instance: <br>
	 * googlebook has the following properties: "thumbWidth", "thumbHeight",
	 * "thumbUrl", "url", "bookId" <br>
	 * youtube has the following properties: "thumbWidth", "thumbHeight",
	 * "thumbUrl", "playUrl" <br>
	 * itunesu has the following properties: "trackUrl" <br>
	 * resource has the following properties: "uuid", "version", "type" <br>
	 * <br>
	 * These properties are not officially supported.
	 * <p>
	 * Does not add this attachment to the item, you must explicitly call
	 * attachments.add(XX) to do so.
	 * 
	 * @param customType The custom type name. Recognised types are
	 *            "googlebook", "youtube", "itunesu", "resource".
	 * @param description A display name for the custom attachment
	 * @return A new CUSTOM type attachment.
	 */
	AttachmentScriptType createCustomAttachment(String customType, String description);

	/**
	 * Creates a resource attachment (as created by the EQUELLA Resource option
	 * on the Attachments control)
	 * 
	 * @param itemUuid The UUID of the item to link to
	 * @param itemVersion The version of the item to link to
	 * @param attachmentUuid Optional. If omitted the returned resource
	 *            attachment will link to the item summary.
	 * @param description A display name for the resource attachment
	 * @return A new CUSTOM type attachment.
	 */
	AttachmentScriptType createResourceAttachment(String itemUuid, int itemVersion, String attachmentUuid,
		String description);

	/**
	 * Returns the content of the attachment in plain text. Don't read large or
	 * binary files with this method!! If you attempt to read an attachment that
	 * is not of type FILE, HTML or IMSRES then an exception will be thrown. The
	 * file is assumed to be UTF-8 encoded, if this is not the case then you
	 * should use readTextFileAttachmentWithEncoding(AttachmentScriptType,
	 * String)
	 * 
	 * @param attachment An attachment of type FILE, HTML or IMSRES with text
	 *            content.
	 * @return The text content of the attachment.
	 */
	String readTextFileAttachment(AttachmentScriptType attachment);

	/**
	 * Same as readTextFileAttachment but you can specify the encoding, e.g.
	 * UTF-8.
	 * 
	 * @param attachment An attachment of type FILE, HTML or IMSRES with text
	 *            content.
	 * @param encoding An encoding type
	 * @return The text content of the attachment.
	 */
	String readTextFileAttachmentWithEncoding(AttachmentScriptType attachment, String encoding);

	/**
	 * Returns the content of the attachment as an XML document. Don't read
	 * large or binary files with this method!! If you attempt to read an
	 * attachment that is not of type FILE, HTML or IMSRES then an exception
	 * will be thrown. The file is assumed to be UTF-8 encoded.
	 * 
	 * @param attachment An attachment of type FILE, HTML or IMSRES with XML
	 *            content.
	 * @return An XML document with the attachment content.
	 */
	XmlScriptType readXmlFileAttachment(AttachmentScriptType attachment);

	/**
	 * Gets a list of any linked items (e.g. with Resource Selector). See
	 * com.tle.common.scripting.ItemScriptType for the type of object returned
	 * in this list.
	 * 
	 * @return A list of ItemScriptType objects
	 */
	List<ItemScriptType> getAttachedItemResources();
}