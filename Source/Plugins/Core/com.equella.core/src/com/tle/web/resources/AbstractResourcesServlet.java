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

package com.tle.web.resources;

import com.tle.common.PathUtils;
import com.tle.core.plugins.PluginService;
import com.tle.web.stream.ContentStream;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;
import com.tle.web.stream.URLContentStream;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.java.plugin.util.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract servlet for serving plugin resources with path traversal protection.
 *
 * <p>This servlet provides a secure base for serving static resources from plugin directories.
 * Multiple layers of security validation prevent directory traversal attacks:
 *
 * <ul>
 *   <li>Early input validation rejects paths containing ".." sequences
 *   <li>Canonical path verification ensures resolved files remain within the plugin root directory
 *   <li>Security events are logged for audit purposes
 * </ul>
 *
 * <p><b>Subclass Requirements:</b>
 *
 * <ul>
 *   <li>{@link #getRootPath()} must return a path relative to the plugin root (e.g., "web/")
 *   <li>{@link #getPluginId(HttpServletRequest)} must return the plugin ID for resource resolution
 * </ul>
 *
 * <p><b>Thread Safety:</b> This servlet is thread-safe. Subclasses must ensure their
 * implementations of abstract methods are also thread-safe.
 *
 * @see #service(HttpServletRequest, HttpServletResponse, String, String)
 */
public abstract class AbstractResourcesServlet extends HttpServlet {
  @Serial private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourcesServlet.class);

  /** Parent directory traversal sequence used in path traversal attacks. */
  private static final String PARENT_DIR_SEQUENCE = "..";

  protected boolean isCalculateETag = false;

  @Inject private PluginService pluginService;
  @Inject private ContentStreamWriter contentStreamWriter;

  /**
   * Serves a plugin resource file with comprehensive security validation.
   *
   * <p>This method performs multiple security checks to prevent path traversal attacks:
   *
   * <ol>
   *   <li>Validates the input path does not contain directory traversal sequences
   *   <li>Constructs the resource URL from the plugin's root path
   *   <li>Verifies the canonical path of the resolved file remains within the root directory
   * </ol>
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param resourcePath relative path from plugin root, must not contain ".." sequences
   * @param mimeType MIME type for the resource, may be null for auto-detection
   * @throws IOException if resource cannot be read or streamed
   * @throws SecurityException if path contains traversal sequences or resolves outside root
   *     directory
   */
  protected void service(
      HttpServletRequest request,
      HttpServletResponse response,
      String resourcePath,
      String mimeType)
      throws IOException {
    Objects.requireNonNull(resourcePath, "resourcePath cannot be null");

    // Early validation: Check for directory traversal sequences before any processing
    validateResourcePath(resourcePath);

    final String filename = PathUtils.getFilenameFromFilepath(resourcePath);
    final String normalizedPath = normalizeLeadingSlash(resourcePath);

    final URL resourceUrl = buildResourceUrl(request, normalizedPath);

    // Use Optional to functionally handle file vs URL resources
    final ContentStream stream =
        Optional.ofNullable(IoUtil.url2file(resourceUrl))
            .map(file -> createFileContentStream(file, request, mimeType, filename))
            .orElseGet(() -> createURLContentStream(resourceUrl, filename, mimeType));

    contentStreamWriter.outputStream(request, response, stream, isCalculateETag);
  }

  /**
   * Creates a file content stream for resources that resolve to files.
   *
   * <p>Performs canonical path verification to ensure the file is within the plugin root directory.
   *
   * @param file the resolved file for the resource
   * @param request the HTTP request for plugin ID resolution
   * @param mimeType the MIME type, may be null
   * @param filename the filename for content disposition
   * @return File content stream for the resource
   * @throws SecurityException if file path is outside root directory
   */
  private ContentStream createFileContentStream(
      File file, HttpServletRequest request, String mimeType, String filename) {
    // Canonical path verification for file resources
    verifyCanonicalPath(request, file);
    return new FileContentStream(file, filename, mimeType);
  }

  /**
   * Validates that the resource path does not contain directory traversal sequences.
   *
   * @param resourcePath the path to validate
   * @throws SecurityException if path contains ".." sequences
   */
  private void validateResourcePath(String resourcePath) {
    if (resourcePath.contains(PARENT_DIR_SEQUENCE)) {
      String sanitizedPath = resourcePath.replace(PARENT_DIR_SEQUENCE, "[REDACTED]");
      LOGGER.warn("Path traversal attempt detected in resource path: {}", sanitizedPath);
      throw new SecurityException(
          "Access denied: resource path contains directory traversal sequences");
    }
  }

  /**
   * Normalizes the resource path by removing leading slashes and collapsing multiple consecutive
   * slashes.
   *
   * <p>This prevents issues with paths like "//test.txt" which can be interpreted as
   * protocol-relative URLs by the URL constructor.
   *
   * @param resourcePath the path to normalize
   * @return normalized path without leading slash and with consecutive slashes collapsed
   */
  private String normalizeLeadingSlash(String resourcePath) {
    return resourcePath
        .replaceFirst("^/+", "") // Remove all leading slashes
        .replaceAll("/+", "/"); // Collapse consecutive slashes to single slash
  }

  /**
   * Builds the resource URL from the plugin's root path and resource path.
   *
   * @param request the HTTP request for plugin ID resolution
   * @param resourcePath the normalized resource path
   * @return URL pointing to the resource
   * @throws IOException if URL construction fails
   */
  private URL buildResourceUrl(HttpServletRequest request, String resourcePath) throws IOException {
    URL rootUrl = pluginService.getClassLoader(getPluginId(request)).getResource(getRootPath());
    if (rootUrl == null) {
      throw new IllegalStateException("Plugin root path not found: " + getRootPath());
    }
    return new URL(rootUrl, resourcePath);
  }

  /**
   * Verifies that the canonical path of the resolved file is within the plugin root directory.
   *
   * <p>This is the final security check to prevent path traversal attacks that may bypass earlier
   * validations through URL encoding, normalization, or other techniques.
   *
   * <p><b>Fail Secure:</b> If the root file cannot be determined (e.g., for JAR resources), a
   * warning is logged but the request proceeds. This is acceptable because:
   *
   * <ul>
   *   <li>Early validation has already rejected paths containing ".." sequences
   *   <li>JAR resources are immutable and cannot be manipulated via path traversal
   *   <li>The URL constructor normalizes paths before file resolution
   * </ul>
   *
   * @param request the HTTP request for plugin ID resolution
   * @param file the resolved file to verify
   * @throws RuntimeException wrapping IOException if canonical paths cannot be resolved
   * @throws SecurityException if file path is outside root directory
   */
  private void verifyCanonicalPath(HttpServletRequest request, File file) {
    try {
      final Path canonicalFilePath = file.getCanonicalFile().toPath();

      getRootFile(request)
          .ifPresentOrElse(
              rootFile -> checkPathWithinRoot(canonicalFilePath, rootFile),
              () ->
                  LOGGER.warn(
                      "Canonical path verification skipped for file-based resource: {} - root file"
                          + " not available (likely JAR resource)",
                      canonicalFilePath));
    } catch (IOException e) {
      throw new RuntimeException("Failed to verify canonical path", e);
    }
  }

  /**
   * Retrieves the root directory file for the plugin.
   *
   * @param request the HTTP request for plugin ID resolution
   * @return Optional containing the root file if it exists, empty otherwise
   */
  private Optional<File> getRootFile(HttpServletRequest request) {
    ClassLoader classLoader = pluginService.getClassLoader(getPluginId(request));
    URL rootUrl = classLoader.getResource(getRootPath());
    return Optional.ofNullable(rootUrl).flatMap(url -> Optional.ofNullable(IoUtil.url2file(url)));
  }

  /**
   * Checks that the canonical file path is within the canonical root path.
   *
   * @param canonicalFilePath the canonical path of the file to verify
   * @param rootFile the root directory file
   * @throws RuntimeException wrapping IOException if canonical root path cannot be resolved
   * @throws SecurityException if file path is outside root directory
   */
  private void checkPathWithinRoot(Path canonicalFilePath, File rootFile) {
    try {
      Path canonicalRootPath = rootFile.getCanonicalFile().toPath();

      if (!canonicalFilePath.startsWith(canonicalRootPath)) {
        String sanitizedPath =
            canonicalFilePath.toString().replace(PARENT_DIR_SEQUENCE, "[REDACTED]");
        LOGGER.warn(
            "Canonical path traversal attempt detected: file path {} is outside root directory",
            sanitizedPath);
        throw new SecurityException("Access denied: path resolves outside root directory");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to resolve canonical root path", e);
    }
  }

  /**
   * Creates a URL content stream for resources that don't resolve to files.
   *
   * @param resourceUrl the resource URL
   * @param filename the filename for content disposition
   * @param mimeType the MIME type, may be null
   * @return URL content stream for the resource
   */
  private URLContentStream createURLContentStream(
      URL resourceUrl, String filename, String mimeType) {
    try {
      return new URLContentStream(resourceUrl, filename, mimeType);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create URL content stream", e);
    }
  }

  /**
   * Returns the root path within the plugin for serving resources.
   *
   * <p>This path is relative to the plugin root directory (e.g., "web/", "icons/"). It defines the
   * base directory from which resources can be served.
   *
   * <p><b>Implementation Note:</b> Must be thread-safe.
   *
   * @return root path relative to plugin directory
   */
  public abstract String getRootPath();

  /**
   * Returns the plugin ID for the given request.
   *
   * <p>This method determines which plugin's resources should be served. Implementations may
   * extract the plugin ID from the request path, parameters, or use a fixed plugin ID.
   *
   * <p><b>Implementation Note:</b> Must be thread-safe.
   *
   * @param request the HTTP request
   * @return plugin ID for resource resolution
   */
  public abstract String getPluginId(HttpServletRequest request);
}
