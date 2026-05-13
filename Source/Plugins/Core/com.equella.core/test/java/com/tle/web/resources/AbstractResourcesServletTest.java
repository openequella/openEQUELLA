package com.tle.web.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tle.core.plugins.PluginService;
import com.tle.web.stream.ContentStream;
import com.tle.web.stream.ContentStreamWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

public class AbstractResourcesServletTest {

  @TempDir Path tempDir;

  private TestResourcesServlet servlet;
  private PluginService pluginService;
  private ContentStreamWriter contentStreamWriter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private ClassLoader classLoader;

  private File rootDir;
  private File testFile;

  /** Concrete test implementation of AbstractResourcesServlet for testing. */
  private static class TestResourcesServlet extends AbstractResourcesServlet {
    private final String pluginId;
    private final String rootPath;

    public TestResourcesServlet(
        String pluginId,
        String rootPath,
        PluginService pluginService,
        ContentStreamWriter contentStreamWriter) {
      super(pluginService, contentStreamWriter);
      this.pluginId = pluginId;
      this.rootPath = rootPath;
    }

    @Override
    public String getRootPath() {
      return rootPath;
    }

    @Override
    public String getPluginId(HttpServletRequest request) {
      return pluginId;
    }

    // Expose protected service method for testing
    public void testService(
        HttpServletRequest request,
        HttpServletResponse response,
        String resourcePath,
        String mimeType)
        throws IOException {
      service(request, response, resourcePath, mimeType);
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    // Create mock dependencies
    pluginService = mock(PluginService.class);
    contentStreamWriter = mock(ContentStreamWriter.class);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    classLoader = mock(ClassLoader.class);

    // Create servlet instance with injected dependencies (no reflection needed)
    servlet = new TestResourcesServlet("test.plugin", "web/", pluginService, contentStreamWriter);

    // Create test directory structure
    rootDir = Files.createDirectories(tempDir.resolve("plugin-root").resolve("web")).toFile();
    testFile = new File(rootDir, "test.txt");
    Files.write(testFile.toPath(), "test content".getBytes());

    // Setup common mock behavior
    when(pluginService.getClassLoader("test.plugin")).thenReturn(classLoader);
    when(classLoader.getResource("web/")).thenReturn(rootDir.toURI().toURL());
    doNothing()
        .when(contentStreamWriter)
        .outputStream(any(), any(), any(ContentStream.class), anyBoolean());
  }

  @Test
  public void testLegitimateResourcePath() throws Exception {
    // Test serving a legitimate file
    servlet.testService(request, response, "test.txt", "text/plain");

    verifyContentStreamWritten();
  }

  @Test
  public void testLegitimateResourcePathWithLeadingSlash() throws Exception {
    // Test serving a file with leading slash (should be normalized)
    servlet.testService(request, response, "/test.txt", "text/plain");

    verifyContentStreamWritten();
  }

  @Test
  public void testSubdirectoryResourcePath() throws Exception {
    // Create subdirectory and file
    File subDir = new File(rootDir, "subdir");
    subDir.mkdir();
    File subFile = new File(subDir, "subtest.txt");
    Files.write(subFile.toPath(), "sub content".getBytes());

    servlet.testService(request, response, "subdir/subtest.txt", "text/plain");

    verifyContentStreamWritten();
  }

  @Test
  public void testBasicPathTraversal() {
    // Test basic path traversal attempt with ../
    assertPathTraversalBlocked("../etc/passwd");
  }

  @Test
  public void testPathTraversalWithBackslash() {
    // Test path traversal with Windows-style separator
    assertPathTraversalBlocked("..\\etc\\passwd");
  }

  @Test
  public void testPathTraversalInMiddle() {
    // Test path traversal in the middle of path
    assertPathTraversalBlocked("subdir/../../../etc/passwd");
  }

  @Test
  public void testPathTraversalWithEncodedDots() {
    // This test demonstrates that the string "..%2F..%2Fetc%2Fpasswd" is caught
    // because it contains literal ".." sequences.
    //
    // In a real HTTP request scenario:
    // 1. Client sends: GET /resource/%2e%2e%2fetc%2fpasswd
    // 2. Tomcat decodes: %2e%2e → ..  (URL decoding happens automatically)
    // 3. Servlet receives: "../etc/passwd" (already decoded)
    // 4. validateResourcePath(): Detects ".." and blocks
    //
    // This test passes because even the test string contains ".." literals,
    // simulating what the servlet would receive after HTTP container decoding.
    assertPathTraversalBlocked("..%2F..%2Fetc%2Fpasswd");
  }

  @Test
  public void testMultipleTraversalSequences() {
    // Test multiple traversal sequences
    assertPathTraversalBlocked("../../../../../../etc/passwd");
  }

  @Test
  public void testNullResourcePath() {
    // Test null resource path
    assertThrows(
        NullPointerException.class,
        () -> servlet.testService(request, response, null, "text/plain"));
  }

  @Test
  public void testCanonicalPathVerification() throws Exception {
    // Create a file outside root directory
    File outsideDir = Files.createDirectory(tempDir.resolve("outside")).toFile();
    File outsideFile = new File(outsideDir, "evil.txt");
    Files.write(outsideFile.toPath(), "evil content".getBytes());

    // Create symbolic link (if supported by filesystem) that points outside root
    final String evilLinkName = "evil-link";
    Path linkPath = new File(rootDir, evilLinkName).toPath();
    try {
      Files.createSymbolicLink(linkPath, outsideFile.toPath());

      // Attempt to access via symlink - should be caught by canonical path check
      // Note: This test may not work on all filesystems that don't support symlinks
      SecurityException exception =
          assertThrows(
              SecurityException.class,
              () -> servlet.testService(request, response, evilLinkName, null));

      assertTrue(exception.getMessage().contains("outside root directory"));
    } catch (UnsupportedOperationException | IOException e) {
      // Skip test if symlinks not supported
      System.out.println("Skipping symlink test - not supported on this filesystem");
    }
  }

  @Test
  public void testRootPathNotFound() {
    // Test when root path doesn't exist
    when(classLoader.getResource("web/")).thenReturn(null);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> servlet.testService(request, response, "test.txt", "text/plain"));

    assertTrue(exception.getMessage().contains("Plugin root path not found"));
  }

  @Test
  public void testMimeTypePassedThrough() throws Exception {
    servlet.testService(request, response, "test.txt", "application/custom");

    // Capture the ContentStream argument
    ArgumentCaptor<ContentStream> streamCaptor = ArgumentCaptor.forClass(ContentStream.class);
    verify(contentStreamWriter)
        .outputStream(eq(request), eq(response), streamCaptor.capture(), eq(false));

    ContentStream capturedStream = streamCaptor.getValue();
    assertEquals("application/custom", capturedStream.getMimeType());
  }

  @Test
  public void testETagCalculationEnabled() throws Exception {
    servlet.isCalculateETag = true;
    servlet.testService(request, response, "test.txt", "text/plain");

    verify(contentStreamWriter)
        .outputStream(eq(request), eq(response), any(ContentStream.class), eq(true));
  }

  @Test
  public void testFilenameParsedCorrectly() throws Exception {
    servlet.testService(request, response, "path/to/file.jpg", "image/jpeg");

    ArgumentCaptor<ContentStream> streamCaptor = ArgumentCaptor.forClass(ContentStream.class);
    verify(contentStreamWriter)
        .outputStream(eq(request), eq(response), streamCaptor.capture(), eq(false));

    ContentStream capturedStream = streamCaptor.getValue();
    assertEquals("file.jpg", capturedStream.getFilenameWithoutPath());
  }

  @Test
  public void testPathWithDotsInFilename() throws Exception {
    // Create file with dots in name (but not traversal sequence)
    final String dottedFilename = "my.config.file.txt";
    File dottedFile = new File(rootDir, dottedFilename);
    Files.write(dottedFile.toPath(), "config content".getBytes());

    servlet.testService(request, response, dottedFilename, "text/plain");

    verifyContentStreamWritten();
  }

  @Test
  public void testTraversalAtEndOfPath() {
    assertPathTraversalBlocked("somepath/..");
  }

  @Test
  public void testTraversalAtBeginningOfPath() {
    assertPathTraversalBlocked("../somepath");
  }

  @Test
  public void testEmptyPathSegments() throws Exception {
    // Test path with empty segments (consecutive slashes) - should be normalized and handled
    // gracefully
    // The path "//test.txt" will be normalized to "test.txt" by removing leading slashes
    // and collapsing consecutive slashes
    servlet.testService(request, response, "//test.txt", "text/plain");

    verifyContentStreamWritten();
  }

  @Test
  public void testConsecutiveSlashesInPath() throws Exception {
    // Test path with consecutive slashes in the middle
    // Should be normalized by collapsing multiple slashes
    File subDir = new File(rootDir, "subdir");
    subDir.mkdir();
    File subFile = new File(subDir, "test.txt");
    Files.write(subFile.toPath(), "test content".getBytes());

    servlet.testService(request, response, "subdir//test.txt", "text/plain");

    verifyContentStreamWritten();
  }

  @Test
  public void testURLResourceFallback() throws Exception {
    // Test with a URL that doesn't resolve to a file (stays as URL)
    URL jarUrl = new URL("jar:file:/path/to/plugin.jar!/web/");
    when(classLoader.getResource("web/")).thenReturn(jarUrl);

    // This should work with URLContentStream instead of FileContentStream
    // Note: For JAR resources, canonical path verification is skipped (logged at WARN level)
    // but this is acceptable because:
    // 1. Early validation already rejected ".." sequences
    // 2. JAR resources are immutable
    // 3. URL constructor normalizes paths
    servlet.testService(request, response, "resource.js", "application/javascript");

    verifyContentStreamWritten();
  }

  /** Verifies that the content stream writer was called to output a stream (without ETag). */
  private void verifyContentStreamWritten() {
    verify(contentStreamWriter)
        .outputStream(eq(request), eq(response), any(ContentStream.class), eq(false));
  }

  /**
   * Asserts that the given path is blocked as a directory traversal attempt.
   *
   * @param maliciousPath the path expected to trigger a SecurityException
   */
  private void assertPathTraversalBlocked(String maliciousPath) {
    SecurityException exception =
        assertThrows(
            SecurityException.class,
            () -> servlet.testService(request, response, maliciousPath, null));

    assertTrue(exception.getMessage().contains("directory traversal"));
  }
}
