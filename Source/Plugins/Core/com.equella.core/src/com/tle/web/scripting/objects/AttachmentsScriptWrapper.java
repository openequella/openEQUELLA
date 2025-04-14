/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.scripting.objects;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.PropBagWrapper;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.*;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.scripting.objects.AttachmentsScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.BinaryDataScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.scripting.types.XmlScriptType;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.objects.UtilsScriptWrapper.BinaryDataScriptTypeImpl;
import com.tle.web.scripting.types.AttachmentScriptTypeImpl;
import com.tle.web.scripting.types.ItemScriptTypeImpl;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@SuppressWarnings("nls")
public class AttachmentsScriptWrapper extends AbstractScriptWrapper
    implements AttachmentsScriptObject {
  private static final long serialVersionUID = 1L;

  @Inject private FileSystemService fileSystem;

  private final ModifiableAttachments attachments;
  private final FileHandle staging;

  @Inject
  protected AttachmentsScriptWrapper(
      @Assisted("attachments") ModifiableAttachments attachments,
      @Assisted("staging") FileHandle staging) {
    this.attachments = attachments;
    this.staging = staging;
  }

  @Override
  public AttachmentScriptType add(AttachmentScriptType attachment) {
    if (attachment instanceof AttachmentScriptTypeImpl) {
      attachments.addAttachment(((AttachmentScriptTypeImpl) attachment).getWrapped());
      return attachment;
    }
    return null;
  }

  @Override
  public List<AttachmentScriptType> list() {
    List<AttachmentScriptType> all = new ArrayList<>();
    for (IAttachment attach : attachments) {
      all.add(scriptTypeFactory.createAttachment((Attachment) attach, staging));
    }
    return all;
  }

  @Override
  public List<AttachmentScriptType> listForItem(ItemScriptType item) {
    Item realItem = ((ItemScriptTypeImpl) item).getItem();
    return Lists.newArrayList(
        Lists.transform(
            realItem.getAttachments(), o -> scriptTypeFactory.createAttachment(o, staging)));
  }

  @Override
  public void remove(AttachmentScriptType attachment) {
    if (attachment instanceof AttachmentScriptTypeImpl) {
      attachments.removeAttachment(((AttachmentScriptTypeImpl) attachment).getWrapped());
    }
  }

  @Override
  public void clear() {
    for (AttachmentScriptType attachment : list()) {
      remove(attachment);
    }
  }

  @Override
  public AttachmentScriptType getByUuid(String uuid) {
    Attachment attach = (Attachment) attachments.getAttachmentByUuid(uuid);
    if (attach != null) {
      return scriptTypeFactory.createAttachment(attach, staging);
    }
    return null;
  }

  @Override
  public AttachmentScriptType getByFilename(String filename) {
    Attachment attach = (Attachment) attachments.getAttachmentByFilename(filename);
    if (attach != null) {
      return scriptTypeFactory.createAttachment(attach, staging);
    }
    return null;
  }

  @Override
  public AttachmentScriptType createLinkAttachment(String url, String description) {
    LinkAttachment link = new LinkAttachment();
    link.setUrl(url);
    link.setDescription(description);
    return scriptTypeFactory.createAttachment(link, staging);
  }

  @Override
  public AttachmentScriptType createCustomAttachment(String customType, String description) {
    CustomAttachment custom = new CustomAttachment();
    custom.setType(customType);
    custom.setDescription(description);
    return scriptTypeFactory.createAttachment(custom, staging);
  }

  @Override
  public AttachmentScriptType createResourceAttachment(
      String itemUuid, int itemVersion, String attachmentUuid, String description) {
    if (Check.isEmpty(itemUuid)) {
      throw new RuntimeException(
          "Error creating resource attachment, you must supply an item UUID");
    }

    CustomAttachment custom = new CustomAttachment();
    custom.setType("resource");
    custom.setData("uuid", itemUuid);
    custom.setData("version", itemVersion);
    if (!Check.isEmpty(attachmentUuid)) {
      custom.setData("type", "a");
      custom.setUrl(attachmentUuid);
    } else {
      custom.setData("type", "p");
    }
    if (!Check.isEmpty(description)) {
      custom.setDescription(description);
    }
    return scriptTypeFactory.createAttachment(custom, staging);
  }

  @Override
  public AttachmentScriptType createTextFileAttachment(
      String filename, String description, String contents) {
    try {
      StringReader reader = new StringReader(contents);
      FileInfo file = fileSystem.write(staging, filename, reader, false);
      if (file != null) {
        FileAttachment attach = new FileAttachment();
        attach.setFilename(file.getFilename());
        attach.setDescription(description);
        attach.setSize(file.getLength());
        return scriptTypeFactory.createAttachment(attach, staging);
      }
      return null;
    } catch (IOException io) {
      throw new RuntimeException("Error creating text file attachment " + filename, io);
    }
  }

  @Override
  public AttachmentScriptType createHtmlAttachment(
      String filename, String description, String contents) {
    try {
      StringReader reader = new StringReader(contents);
      FileInfo file = fileSystem.write(staging, filename, reader, false);
      if (file != null) {
        HtmlAttachment attach = new HtmlAttachment();
        attach.setFilename(file.getFilename());
        attach.setDescription(description);
        attach.setSize(file.getLength());
        return scriptTypeFactory.createAttachment(attach, staging);
      }
      return null;
    } catch (IOException io) {
      throw new RuntimeException("Error creating HTML attachment " + filename, io);
    }
  }

  @Override
  public AttachmentScriptType createBinaryFileAttachment(
      String filename, String description, BinaryDataScriptType contents) {
    try {
      ByteArrayInputStream is =
          new ByteArrayInputStream(((BinaryDataScriptTypeImpl) contents).getData());
      FileInfo file = fileSystem.write(staging, filename, is, false);
      if (file != null) {
        FileAttachment attach = new FileAttachment();
        attach.setFilename(file.getFilename());
        attach.setDescription(description);
        attach.setSize(file.getLength());
        return scriptTypeFactory.createAttachment(attach, staging);
      }
      return null;
    } catch (IOException io) {
      throw new RuntimeException("Error creating binary file attachment " + filename, io);
    }
  }

  @Override
  public AttachmentScriptType addExistingFileAsAttachment(String filename, String description) {
    if (!fileSystem.fileExists(staging, filename)) {
      throw new RuntimeException("File " + filename + " not found");
    }

    FileInfo file = fileSystem.getFileInfo(staging, filename);
    FileAttachment attach = new FileAttachment();
    attach.setFilename(filename);
    attach.setDescription(description);
    attach.setSize(file.getLength());

    attachments.addAttachment(attach);
    return scriptTypeFactory.createAttachment(attach, staging);
  }

  @Override
  public AttachmentScriptType editTextFileAttachment(
      AttachmentScriptType attachment, String newContents) {
    if (attachment != null) {
      String type = attachment.getType();
      if (type.equals(AttachmentType.FILE.toString())
          || type.equals(AttachmentType.IMSRES.toString())
          || type.equals(AttachmentType.HTML.toString())) {
        try {
          StringReader reader = new StringReader(newContents);
          FileInfo file = fileSystem.write(staging, attachment.getFilename(), reader, false);

          if (attachment instanceof AttachmentScriptTypeImpl) {
            Attachment wrapped = ((AttachmentScriptTypeImpl) attachment).getWrapped();
            if (wrapped instanceof FileAttachment) {
              ((FileAttachment) wrapped).setSize(file.getLength());
            }
          }
          return attachment;
        } catch (IOException io) {
          throw new RuntimeException(
              "Error editing text file attachment " + attachment.getFilename(), io);
        }
      }
    }
    return null;
  }

  @Override
  public String readTextFileAttachment(AttachmentScriptType attachment) {
    return readTextFileAttachmentWithEncoding(attachment, Constants.UTF8);
  }

  @Override
  public String readTextFileAttachmentWithEncoding(
      AttachmentScriptType attachment, String encoding) {
    if (attachment != null) {
      String type = attachment.getType();
      if (type.equals(AttachmentType.FILE.toString())
          || type.equals(AttachmentType.IMSRES.toString())
          || type.equals(AttachmentType.HTML.toString())) {
        final String filename = attachment.getFilename();
        try (Reader reader = new UnicodeReader(fileSystem.read(staging, filename), encoding)) {
          StringWriter writer = new StringWriter();
          CharStreams.copy(reader, writer);
          return writer.toString();
        } catch (IOException io) {
          throw new RuntimeException("Error reading text file attachment " + filename, io);
        }
      }

      throw new RuntimeException("Cannot read an attachment of this type as text");
    }
    return null;
  }

  @Override
  public XmlScriptType readXmlFileAttachment(AttachmentScriptType attachment) {
    return new PropBagWrapper(new PropBagEx(readTextFileAttachment(attachment)));
  }

  @Override
  public List<ItemScriptType> getAttachedItemResources() {
    List<ItemScriptType> items = new ArrayList<ItemScriptType>();

    List<CustomAttachment> customList = attachments.getCustomList("resource");
    for (CustomAttachment attachment : customList) {
      ItemId itemId =
          new ItemId((String) attachment.getData("uuid"), (Integer) attachment.getData("version"));

      items.add(scriptTypeFactory.createItem(itemId));
    }
    return items;
  }
}
