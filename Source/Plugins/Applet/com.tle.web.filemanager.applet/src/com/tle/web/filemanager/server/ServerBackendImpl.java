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

package com.tle.web.filemanager.server;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.exceptions.BannedFileException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.SubItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;
import com.tle.web.filemanager.common.FileInfo;
import com.tle.web.filemanager.common.ServerBackend;
import com.tle.web.sections.generic.DefaultSectionInfo;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardStateInterface;

@Bind
@Singleton
public class ServerBackendImpl implements ServerBackend {
  @Inject private FileSystemService fileSystemService;
  @Inject private WizardService wizardService;
  @Inject private InstitutionService institutionService;

  @SuppressWarnings("nls")
  @Override
  public String getDownloadUrl(String wizardId, String filename) {
    WizardStateInterface state = getWizardState(wizardId);
    try {
      return new URL(
              institutionService.getInstitutionUrl(),
              "file/" + state.getStagingId() + "/$/" + URLUtils.urlEncode(filename, false))
          .toString();
    } catch (MalformedURLException ex) {
      throw new RuntimeException("This shouldn't happen: " + filename, ex);
    }
  }

  @Override
  public void delete(String wizardId, String filename) {
    WizardStateInterface wizState = getWizardState(wizardId);
    fileSystemService.removeFile(wizState.getFileHandle(), filename);
    forAttachments(wizState, new RemoveAttachment(), filename);
  }

  @Override
  public List<FileInfo> listFiles(String wizardId, final String directory) {
    WizardStateInterface wizState = getWizardState(wizardId);

    FileHandle h = wizState.getFileHandle();
    if (!Check.isEmpty(directory)) {
      if (h instanceof StagingFile) {
        h = new SubTemporaryFile((StagingFile) h, directory);
      } else if (h instanceof ItemFile) {
        h = new SubItemFile((ItemFile) h, directory);
      }
    }
    final FileHandle handle = h;

    // Need a list of current attachments, so we can mark the others.
    final Set<String> currentAtts = new HashSet<String>();
    for (IAttachment att : wizState.getAttachments().getList(AttachmentType.FILE)) {
      currentAtts.add(att.getUrl());
    }

    // A list of all the files in our directory
    List<String> files = fileSystemService.grepIncludingDirs(handle, null, "*"); // $NON-NLS-1$

    // Convert filenames to full FileInfo objects
    List<FileInfo> infos =
        Lists.newArrayList(
            Lists.transform(
                files,
                new Function<String, FileInfo>() {
                  @Override
                  @SuppressWarnings("nls")
                  public FileInfo apply(String filename) {
                    FileInfo info = new FileInfo();
                    info.setName(filename);
                    info.setPath(directory);
                    info.setLastModified(fileSystemService.lastModified(handle, filename));
                    info.setDirectory(fileSystemService.fileIsDir(handle, filename));
                    if (!info.isDirectory()) {
                      try {
                        info.setSize(fileSystemService.fileLength(handle, filename));
                      } catch (FileNotFoundException ex) {
                        throw new RuntimeException("We should never reach here", ex);
                      }

                      info.setMarkAsAttachment(currentAtts.contains(info.getFullPath()));
                    }
                    return info;
                  }
                }));

    // Remove root folders that begin with a underscore.
    if (Check.isEmpty(directory)) {
      for (Iterator<FileInfo> iter = infos.iterator(); iter.hasNext(); ) {
        FileInfo info = iter.next();
        if (info.isDirectory() && info.getName().startsWith("_")) // $NON-NLS-1$
        {
          iter.remove();
        }
      }
    }

    return infos;
  }

  @Override
  public boolean renameFile(String wizardId, String oldName, String newName) {
    final WizardStateInterface wizState = getWizardState(wizardId);
    final FileHandle handle = wizState.getFileHandle();
    final boolean isFileRename = !fileSystemService.fileIsDir(handle, oldName);

    boolean success = false;
    try {
      success = fileSystemService.rename(handle, oldName, newName);
    } catch (BannedFileException ex) {
      // Ignore for now...
    }

    if (!success) {
      return false;
    }

    FindAttachments finder = new FindAttachments();
    finder.setStopAfterFirstFind(isFileRename);
    forAttachments(wizState, finder, isFileRename ? oldName : oldName + '/', isFileRename);

    for (Attachment attachment : finder.getAttachments()) {
      attachment.setUrl(isFileRename ? newName : attachment.getUrl().replace(oldName, newName));
      attachment.setDescription(
          isFileRename ? newName : attachment.getDescription().replace(oldName, newName));
    }

    return true;
  }

  @Override
  public void copy(String wizardId, String sourceFile, String destFile) {
    WizardStateInterface wizState = getWizardState(wizardId);
    fileSystemService.copy(wizState.getFileHandle(), sourceFile, destFile);
  }

  @Override
  public void markAsResource(String wizardId, boolean mark, String fullPath) {
    WizardStateInterface wizState = getWizardState(wizardId);
    ModifiableAttachments attachments = wizState.getAttachments();

    if (mark) {
      FindAttachments finder = new FindAttachments();
      finder.setStopAfterFirstFind(true);
      forAttachments(wizState, finder, fullPath);
      if (!Check.isEmpty(finder.getAttachments())) {
        // Already marked
        return;
      }

      FileAttachment att = new FileAttachment();
      att.setFilename(fullPath);
      att.setDescription(fullPath);
      att.setConversion(true);
      try {
        att.setSize(fileSystemService.fileLength(wizState.getFileHandle(), fullPath));
      } catch (FileNotFoundException ex) {
        // We should never reach here
        throw new RuntimeException(ex);
      }

      attachments.addAttachment(att);
    } else {
      forAttachments(wizState, new RemoveAttachment(), fullPath);
    }
  }

  @Override
  public void newFolder(String wizardId, String name) {
    WizardStateInterface wizState = getWizardState(wizardId);
    fileSystemService.mkdir(wizState.getFileHandle(), name);
  }

  @Override
  public void write(String wizardId, String filename, boolean append, byte[] upload) {
    WizardStateInterface wizState = getWizardState(wizardId);
    try {
      fileSystemService.write(
          wizState.getFileHandle(), filename, new ByteArrayInputStream(upload), append);
    } catch (IOException ex) {
      throw new RuntimeException(
          CurrentLocale.get("com.tle.web.applet.filemanager.error.filewrite"), ex); // $NON-NLS-1$
    }
  }

  @Override
  public void extractArchive(String wizardId, String filename, String destDir) {
    WizardStateInterface wizState = getWizardState(wizardId);
    try {
      fileSystemService.unzipFile(wizState.getFileHandle(), filename, destDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void forAttachments(
      WizardStateInterface state, AttachmentProcessor processor, String filename) {
    forAttachments(state, processor, filename, true);
  }

  private void forAttachments(
      WizardStateInterface state,
      AttachmentProcessor processor,
      String filename,
      boolean fullMatch) {
    final String filenameLower = (filename == null ? null : filename.toLowerCase());
    for (Iterator<Attachment> iter = state.getAttachments().getIterator(AttachmentType.FILE);
        iter.hasNext(); ) {
      Attachment attachment = iter.next();
      final String urlLower = attachment.getUrl().toLowerCase();
      if ((filenameLower == null
              || (fullMatch ? filenameLower.equals(urlLower) : urlLower.startsWith(filenameLower)))
          && !processor.process(iter, attachment)) {
        return;
      }
    }
  }

  private WizardStateInterface getWizardState(String wizardId) {
    return wizardService.getFromSession(new DefaultSectionInfo(null), wizardId);
  }

  private interface AttachmentProcessor {
    /** @return true to continue processing more attachments */
    boolean process(Iterator<Attachment> iter, Attachment attachment);
  }

  private static class RemoveAttachment implements AttachmentProcessor {
    @Override
    public boolean process(Iterator<Attachment> iterator, Attachment attachment) {
      iterator.remove();
      return false;
    }
  }

  private static class FindAttachments implements AttachmentProcessor {
    private final List<Attachment> attachments = new ArrayList<Attachment>();
    private boolean stopAfterFirstFind = false;

    @Override
    public boolean process(Iterator<Attachment> iterator, Attachment attachment) {
      attachments.add(attachment);
      return !stopAfterFirstFind;
    }

    public void setStopAfterFirstFind(boolean stopAfterFirstFind) {
      this.stopAfterFirstFind = stopAfterFirstFind;
    }

    public List<Attachment> getAttachments() {
      return attachments;
    }
  }
}
