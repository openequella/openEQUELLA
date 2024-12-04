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

package com.tle.core.imagemagick;

import com.google.inject.name.Named;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.healthcheck.listeners.ServiceCheckRequestListener;
import com.tle.core.healthcheck.listeners.ServiceCheckResponseListener.CheckServiceResponseEvent;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.ServiceName;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.Status;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.services.FileSystemService;
import com.tle.core.zookeeper.ZookeeperService;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Bind(ImageMagickService.class)
@Singleton
@SuppressWarnings("nls")
public class ImageMagickServiceImpl implements ImageMagickService, ServiceCheckRequestListener {
  private static final Log LOGGER = LogFactory.getLog(ImageMagickServiceImpl.class);
  private static final String KEY_PFX =
      AbstractPluginService.getMyPluginId(ImageMagickServiceImpl.class) + ".";

  @Inject private FileSystemService fileSystem;
  @Inject private EventService eventService;
  @Inject private ZookeeperService zkService;

  @Inject
  @Named("thumbnailing.timeout")
  private int thumbnailingTimeout;

  private String imageMagickPath;
  private File convertExe;
  private File identifyExe;

  @Inject
  public void setImageMagickPath(@Named("imageMagick.path") String imageMagickPath) {
    this.imageMagickPath = imageMagickPath.trim();
  }

  @Override
  public void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) {
    List<String> opts = new ArrayList<String>();
    validateAgainstTimer(srcFile);
    boolean gif = srcFile.getAbsolutePath().endsWith(".gif");
    if (gif) {
      opts.add(convertExe.getAbsolutePath());
      opts.add(srcFile.getAbsolutePath() + "[0]");
      String frame = srcFile.getParent() + "\\frame.gif";
      opts.add(frame);
      ExecUtils.exec(opts);
      srcFile = new File(frame);
      opts.clear();
    }
    try {
      Dimension imageDimensions = getImageDimensions(srcFile);
      options.setImgHeight(imageDimensions.height);
      options.setImgWidth(imageDimensions.width);

      opts.add(convertExe.getAbsolutePath());
      boolean madeDirs = dstFile.getParentFile().mkdirs();
      if (!(madeDirs || dstFile.getParentFile().exists())) {
        throw new IOException(
            "Could not create/confirm directory " + dstFile.getParentFile().getAbsolutePath());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    int thumbWidth = options.getWidth();
    int thumbHeight = options.getHeight();
    if (!options.isNoSize()) {
      int sizeX = options.getImgWidth();
      int sizeY = options.getImgHeight();
      if (sizeX == 0) {
        sizeX = thumbWidth * 2;
      }
      if (sizeY == 0) {
        sizeY = thumbHeight * 2;
      }
      opts.add("-size");
      opts.add(sizeX + "x" + sizeY);
    }

    opts.add(srcFile.getAbsolutePath());

    if (!options.isNoSize()) {
      opts.add("-thumbnail");
      String thumbOpt = "";

      if (options.isKeepAspect()
          || (options.getImgHeight() < thumbHeight && options.getImgWidth() < thumbWidth)) {
        thumbOpt = ">";
      } else {
        thumbOpt = "^";
      }

      opts.add(thumbWidth + "x" + thumbHeight + thumbOpt);
      if (options.getGravity() != null) {
        opts.add("-gravity");
        opts.add(options.getGravity());
      }

      if (!Check.isEmpty(options.getBackgroundColour())) {
        opts.add("-bordercolor");
        opts.add(options.getBackgroundColour());
        opts.add("-border");
        opts.add("50");
      }

      int cropWidth = options.getCropWidth();
      int cropHeight = options.getCropHeight();
      if (cropHeight > 0 && cropWidth > 0) {
        opts.add("-crop");
        int cropX = options.getCropX();
        int cropY = options.getCropY();
        opts.add(cropWidth + "x" + cropHeight + "+" + cropX + "+" + cropY); // $NON-NLS-2$
        opts.add("+repage");
      }
    }

    opts.add(dstFile.getAbsolutePath());
    ExecResult exec = ExecUtils.exec(opts);
    exec.ensureOk();
    if (gif) {
      boolean wasDeleted = srcFile.delete();
      if (!wasDeleted) {
        LOGGER.warn("Unable to delete generated gif frame:" + srcFile.getAbsolutePath());
      }
    }

    if (!options.isSkipBlankCheck()) {
      // Check that we have not created a blank (white) thumbnail - delete
      // if so.
      ExecResult exec2 =
          ExecUtils.exec(
              convertExe.getAbsolutePath(),
              dstFile.getAbsolutePath(),
              "-threshold",
              "99%",
              "-format",
              "\"%[fx:100*mean]\"",
              "info:"); //$NON-NLS-2$//$NON-NLS-4$
      exec2.ensureOk();
      if (exec2.getStdout().contains("100")) {
        boolean wasDeleted = dstFile.delete();
        if (!wasDeleted) {
          LOGGER.warn("Unable to delete presumed blank thumbnail: " + dstFile.getAbsolutePath());
        }
      }
    }
  }

  private void validateAgainstTimer(File image) {
    // use a timed process so that thumbnailing
    // for problem files doesn't attempt indefinitely.
    // Set in plugins/com.tle.core.imagemagick/config.properties
    // thumbnail.timeout as an integer in seconds.
    // if not set, the default is 20 seconds. If set to 0, uses a regular non-timed process.
    ExecResult result =
        ExecUtils.execWithTimeLimit(
            thumbnailingTimeout,
            new String[] {
              identifyExe.getAbsolutePath(),
              "-format",
              "%wx%h",
              new String(
                  image.getAbsolutePath().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
            });
    result.ensureOk();
  }

  @PostConstruct
  public void afterPropertiesSet() throws Exception {
    final File imageMagicDir = new File(imageMagickPath);
    convertExe = ExecUtils.findExe(imageMagicDir, "convert");
    identifyExe = ExecUtils.findExe(imageMagicDir, "identify");
    if (convertExe == null || identifyExe == null) {
      throw new RuntimeException(
          "ImageMagick was not found, specifically the convert and identify programs.  The"
              + " configured path is "
              + imageMagicDir.getCanonicalPath());
    }
  }

  @Override
  public Dimension getImageDimensions(FileHandle handle, String filename) throws IOException {
    return getImageDimensions(fileSystem.getExternalFile(handle, filename));
  }

  @Override
  public Dimension getImageDimensions(File image) throws IOException {
    ExecResult result =
        ExecUtils.exec(
            identifyExe.getAbsolutePath(),
            "-format",
            "%wx%h",
            new String(image.getAbsolutePath().getBytes("UTF-8"), "UTF-8"));
    result.ensureOk();

    Matcher m = Pattern.compile(".*?(\\d+)x(\\d+).*?", Pattern.DOTALL).matcher(result.getStdout());
    if (m.matches()) {
      return new Dimension(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }
    throw new RuntimeException(
        "Output is not in expected format: "
            + (Check.isEmpty(result.getStderr()) ? result.getStdout() : result.getStderr()));
  }

  @Override
  public void sample(File src, File dest, String width, String height, String... options)
      throws IOException {
    operation("-sample", src, dest, null, width, height, true, options);
  }

  @Override
  public void sampleNoRatio(File src, File dest, String width, String height, String... options)
      throws IOException {
    operation("-sample", src, dest, null, width, height, false, options);
  }

  private void operation(
      String op,
      File src,
      File dest,
      String opParam,
      String width,
      String height,
      boolean keepRatio,
      String[] options) {
    ArrayList<String> args = new ArrayList<String>();
    args.add(convertExe.getAbsolutePath());
    args.add(src.getAbsolutePath());
    args.add(op);
    if (opParam != null) {
      args.add(opParam);
    }
    if (width != null && height != null) {
      String dim = width + "x" + height;
      if (!keepRatio) {
        dim += "!";
      }
      args.add(dim);
    }
    if (options != null) {
      args.addAll(Arrays.asList(options));
    }
    args.add(dest.getAbsolutePath());
    ExecUtils.exec(args.toArray(new String[args.size()])).ensureOk();
  }

  @Override
  public void crop(File src, File dest, String width, String height, String... options)
      throws IOException {
    operation("-crop", src, dest, null, width, height, true, options);
  }

  @Override
  public void rotate(final File srcImage, final File destImage, int angle, String... options)
      throws IOException {
    operation("-rotate", srcImage, destImage, Integer.toString(angle), null, null, true, options);
  }

  @Override
  public void generateStandardThumbnail(File srcFile, File dstFile) {
    // int size = 64;
    int height = 66;
    int width = 88;
    ThumbnailOptions topts = new ThumbnailOptions();
    topts.setHeight(height);
    topts.setWidth(width);
    topts.setCropHeight(height);
    topts.setCropWidth(width);
    topts.setGravity("center");
    topts.setBackgroundColour("White");

    generateThumbnailAdvanced(srcFile, dstFile, topts);
  }

  @Override
  public boolean supported(String mimeType) {
    return mimeType.startsWith("image/") || mimeType.equals("windows/metafile");
  }

  @Override
  public void checkServiceRequest(CheckServiceRequestEvent request) {
    ServiceStatus status = new ServiceStatus(ServiceName.IMAGEMAGICK);
    try {
      ExecResult versionResult = ExecUtils.exec(identifyExe.getAbsolutePath(), "-version");

      if (!versionResult.getStderr().isEmpty()) {
        status.setServiceStatus(Status.BAD);
        status.setMoreInfo(
            CurrentLocale.get(
                KEY_PFX + "imagemagick.servicecheck.moreinfo.problem",
                imageMagickPath,
                versionResult.getStderr()));
      } else {
        status.setServiceStatus(Status.GOOD);
        status.setMoreInfo(
            CurrentLocale.get(
                KEY_PFX + "imagemagick.servicecheck.moreinfo",
                imageMagickPath,
                versionResult.getStdout()));
      }
    } catch (Exception e) {
      status.setServiceStatus(Status.BAD);
      status.setMoreInfo(
          CurrentLocale.get(
              KEY_PFX + "imagemagick.servicecheck.moreinfo.problem",
              imageMagickPath,
              e.getMessage()));
    }
    eventService.publishApplicationEvent(
        new CheckServiceResponseEvent(request.getRequetserNodeId(), zkService.getNodeId(), status));
  }
}
