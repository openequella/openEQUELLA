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

package com.tle.core.settings.loginnotice.impl;

import com.tle.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.BadRequestException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides whether a file may be stored as a login notice image, and what a stored one actually is.
 */
public class LoginNoticeImageValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginNoticeImageValidator.class);

  private static final String LOG_PREFIX = "Rejecting login notice image upload: {}";
  private static final String UNSUPPORTED_MESSAGE = "Not a supported image type";
  private static final String INVALID_MESSAGE = "Not a valid image";

  /**
   * The extensions a login notice image may be uploaded under.
   *
   * <p>Deliberately a fixed list rather than {@link ImageIO#getReaderFileSuffixes()}. That method
   * reports whatever readers happen to be registered on the classpath, so the set of formats we
   * accept would silently follow our dependencies. Adding an SVG reader such as TwelveMonkeys'
   * imageio-batik - directly or transitively - would reopen SVG uploads, and with them scriptable
   * content in a page served before login, with no change to this class for a reviewer to notice.
   */
  private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif");

  /**
   * The formats the uploaded bytes may actually decode as, keyed by the MIME types the reader's
   * provider advertises.
   *
   * <p>The counterpart to {@link #ALLOWED_IMAGE_EXTENSIONS} on the content side, and pinned for the
   * same reason: without it any format a classpath plugin can read would be stored, so long as it
   * arrived under an allowed extension.
   */
  private static final Set<String> ALLOWED_IMAGE_MIME_TYPES =
      Set.of("image/png", "image/jpeg", "image/gif");

  /** Rejects a filename that does not end in one of {@link #ALLOWED_IMAGE_EXTENSIONS}. */
  public void validateImageExtension(String name) {
    String extension = FilenameUtils.getExtension(name);
    // Locale.ROOT because a Turkish default locale lowercases the I in GIF to a dotless i.
    if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
      throw buildImageException(UNSUPPORTED_MESSAGE, "extension outside the allowlist");
    }
  }

  /**
   * Confirms that the uploaded bytes decode as one of the image formats we accept, rather than
   * relying on the client-supplied filename or extension. HTML or SVG renamed to .png finds no
   * reader at all; a TIFF renamed to .png finds one but is not a format we allow.
   */
  public void validateImageContent(byte[] imageBytes) {
    try (DisposableImageSource image =
        new DisposableImageSource(new ByteArrayInputStream(imageBytes))) {
      ImageReader reader = requireImageReader(image);
      validateImageMimeType(reader);
      validateImageDecodes(reader, image.imageInputStream());
    } catch (IOException e) {
      throw buildImageException(INVALID_MESSAGE, "the uploaded bytes could not be read", e);
    }
  }

  /**
   * The reader found for the content. Throws if none is found, since this method backs upload
   * validation, where "nothing can decode this" means the upload is invalid. Contrast {@link
   * #validMimeTypeOf(InputStream)}, which meets the same absence on the serving path with an empty
   * {@link Optional} instead of an exception.
   */
  private ImageReader requireImageReader(DisposableImageSource image) {
    return image
        .reader()
        .orElseThrow(
            () -> buildImageException(INVALID_MESSAGE, "no reader recognises the content"));
  }

  /** The MIME types the reader's provider advertises, empty when it advertises none. */
  private Stream<String> mimeTypesOf(ImageReader reader) {
    return Optional.ofNullable(reader.getOriginatingProvider())
        .map(ImageReaderSpi::getMIMETypes)
        .stream()
        .flatMap(Arrays::stream);
  }

  /**
   * The first type the reader's provider advertises that is in {@link #ALLOWED_IMAGE_MIME_TYPES},
   * empty when it advertises none we accept.
   *
   * <p>Considering every advertised type rather than only the first because a provider may
   * advertise several - the JDK PNG reader reports both image/png and the legacy image/x-png, in an
   * order the provider chooses. Storing and serving both ask this one question here, so a format
   * cannot be accepted on the way in and refused on the way out because an alias came first.
   */
  private Optional<String> validMimeTypeOf(ImageReader reader) {
    return mimeTypesOf(reader).filter(ALLOWED_IMAGE_MIME_TYPES::contains).findFirst();
  }

  /**
   * The type the content may be served as, empty when no reader recognises it or the format is one
   * we do not accept.
   *
   * <p>Closes the {@link ImageInputStream} it wraps around {@code imageFile}, but not {@code
   * imageFile} itself - that stays with the caller that opened it.
   */
  public Optional<String> validMimeTypeOf(InputStream imageFile) throws IOException {
    try (DisposableImageSource image = new DisposableImageSource(imageFile)) {
      return image.reader().flatMap(this::validMimeTypeOf);
    }
  }

  /** Rejects a reader whose format is outside {@link #ALLOWED_IMAGE_MIME_TYPES}. */
  private void validateImageMimeType(ImageReader reader) {
    validMimeTypeOf(reader)
        .orElseThrow(
            () -> buildImageException(UNSUPPORTED_MESSAGE, "MIME type outside the allowlist"));
  }

  /**
   * Confirms that the file contains at least one decodable image by fully decoding image 0.
   *
   * <p>Neither check before this one looks past the header - {@link #validateImageExtension} reads
   * only the filename, {@link #validateImageMimeType(ImageReader)} only the types the chosen reader
   * advertises - so eight bytes of genuine PNG magic number in front of anything at all would
   * otherwise be enough to have an arbitrary blob stored and served as image/png. It does not make
   * the stored file exclusively an image - a reader stops at the format's end marker, so appended
   * data survives - which is why the serving side adds `X-Content-Type-Options: nosniff` and
   * `Content-Disposition: attachment`.
   *
   * <p>{@link RuntimeException} is caught alongside {@link IOException} because readers disagree on
   * how to report malformed data. Only image 0 is decoded, so a multi-image GIF is validated by its
   * first frame alone.
   */
  private void validateImageDecodes(ImageReader reader, ImageInputStream imageInputStream) {
    try {
      reader.setInput(imageInputStream);
      reader.read(0);
    } catch (IOException | RuntimeException e) {
      throw buildImageException(INVALID_MESSAGE, "the content failed to decode", e);
    }
  }

  private BadRequestException buildImageException(String userMessage, String logDetail) {
    LOGGER.debug(LOG_PREFIX, logDetail);
    return new BadRequestException(userMessage);
  }

  private BadRequestException buildImageException(
      String userMessage, String logDetail, Throwable cause) {
    LOGGER.debug(LOG_PREFIX, logDetail, cause);
    return new BadRequestException(userMessage, cause);
  }

  /**
   * Everything javax.imageio needs opened to answer a question about some content - the {@link
   * ImageInputStream} over it and the reader for its format - acquired together and released
   * together by try-with-resources.
   *
   * <p>A reader holds native decoder resources until {@link ImageReader#dispose()} is called, but
   * the class predates {@link AutoCloseable} and so cannot be a resource itself. Left to the
   * caller, releasing it means a try/finally nested inside the one already holding the stream open,
   * at every call site, with the check it surrounds buried in the middle. Owning both here makes
   * disposal a property of the resource rather than something each caller orchestrates, and leaves
   * every use a single flat try-with-resources.
   *
   * <p>Neither part is guaranteed: javax.imageio may produce no stream for the content, and no
   * registered reader may recognise its format. Both absences surface as an empty {@link #reader()}
   * for the caller to answer for - refusing an upload, or reporting nothing to serve.
   */
  private static final class DisposableImageSource implements AutoCloseable {

    /**
     * Null when no {@link javax.imageio.spi.ImageInputStreamSpi} accepts the content, which in
     * practice does not happen - the JDK registers one for any {@link InputStream}. Declared
     * nullable because {@link ImageIO#createImageInputStream} is documented to return null, not
     * because a null is expected here.
     */
    @Nullable private final ImageInputStream imageInputStream;

    /** Null when no reader recognises the format. */
    @Nullable private final ImageReader reader;

    DisposableImageSource(InputStream imageFile) throws IOException {
      this.imageInputStream = ImageIO.createImageInputStream(imageFile);
      this.reader = findImageReader(imageInputStream).orElse(null);
    }

    /** The reader for the content's format, empty when nothing registered recognises it. */
    Optional<ImageReader> reader() {
      return Optional.ofNullable(reader);
    }

    /**
     * The stream the reader reads from, for a caller that needs to decode rather than just ask what
     * the format is. Only meaningful once {@link #reader()} has answered with one - a reader having
     * been found implies a stream it was found for.
     */
    ImageInputStream imageInputStream() {
      return imageInputStream;
    }

    /**
     * Releases the reader and the stream opened over the content, but not the content stream itself
     * - that stays with the caller that opened it.
     */
    @Override
    public void close() throws IOException {
      if (reader != null) {
        reader.dispose();
      }
      if (imageInputStream != null) {
        imageInputStream.close();
      }
    }

    private static Optional<ImageReader> findImageReader(
        @Nullable ImageInputStream imageInputStream) {
      return Optional.ofNullable(imageInputStream)
          .map(ImageIO::getImageReaders)
          .filter(Iterator::hasNext)
          .map(Iterator::next);
    }
  }
}
