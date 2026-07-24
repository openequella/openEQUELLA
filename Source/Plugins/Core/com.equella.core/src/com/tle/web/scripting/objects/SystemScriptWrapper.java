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

import com.tle.common.scripting.objects.SystemScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.ExecutionResultScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.guice.SystemScriptModule;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.scripting.objects.FileScriptingObjectImpl.FileHandleScriptTypeImpl;
import com.tle.web.scripting.types.AttachmentScriptTypeImpl;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
@Bind(SystemScriptObject.class)
@Singleton
public class SystemScriptWrapper implements SystemScriptObject {
  private static final Logger LOGGER = LoggerFactory.getLogger(SystemScriptWrapper.class);

  private final FileSystemService fileSystem;

  private final Set<String> allowedExecutables;

  @Inject
  public SystemScriptWrapper(
      FileSystemService fileSystem,
      @Named(SystemScriptModule.ALLOWED_EXECUTABLES_KEY) String allowedExecutablesConfig) {
    this.allowedExecutables = parseAllowedExecutables(allowedExecutablesConfig);
    this.fileSystem = fileSystem;
  }

  @Override
  public ExecutionResultScriptType execute(String programPath, Object[] parameters) {
    checkAllowed(programPath);
    return new ExecutionResultTypeImpl(ExecUtils.exec(getCommand(programPath, parameters)));
  }

  @Override
  public void executeInBackground(String programPath, Object[] parameters) {
    checkAllowed(programPath);
    final String[] cmd = getCommand(programPath, parameters);
    new Thread("SystemScriptWrapper execution thread") {
      @Override
      public void run() {
        ExecUtils.exec(cmd);
      }
    }.start();
  }

  protected Set<String> getAllowedExecutables() {
    return allowedExecutables;
  }

  private String[] getCommand(String programPath, Object[] parameters) {
    final String[] cmd = new String[parameters.length + 1];
    cmd[0] = programPath;
    // System.arraycopy(parameters, 0, cmd, 1, parameters.length);
    for (int i = 0; i < parameters.length; i++) {
      Object param = parameters[i];
      // munge the param
      String strParam = "";
      if (param instanceof String) {
        strParam = (String) param;
      } else if (param instanceof Number) {
        strParam = Integer.toString(((Number) param).intValue());
      } else if (param instanceof AttachmentScriptType) {
        AttachmentScriptTypeImpl attachmentType = ((AttachmentScriptTypeImpl) param);
        strParam =
            fileSystem
                .getExternalFile(attachmentType.getStagingFile(), attachmentType.getUrl())
                .getAbsolutePath();
      } else if (param instanceof FileHandleScriptType) {
        FileHandleScriptTypeImpl fileType = ((FileHandleScriptTypeImpl) param);
        strParam =
            fileSystem
                .getExternalFile(fileType.getHandle(), fileType.getFilepath())
                .getAbsolutePath();
      }

      cmd[i + 1] = strParam;
    }
    return cmd;
  }

  /**
   * Resolves a file path to its canonical form.
   *
   * @param path the file path to resolve
   * @return an Optional containing the canonical path, or empty if unresolvable
   */
  private Optional<String> getCanonicalPath(String path) {
    try {
      return Optional.of(new File(path).getCanonicalPath());
    } catch (IOException | SecurityException e) {
      LOGGER.warn("Ignoring unresolvable path '{}': {}", path, e.getMessage());
      return Optional.empty();
    }
  }

  private Set<String> parseAllowedExecutables(String config) {
    return Arrays.stream(config.split(","))
        .map(String::trim)
        .filter(path -> !path.isBlank())
        .map(this::getCanonicalPath)
        .flatMap(Optional::stream)
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Validates that the given program path is on the allow-list.
   *
   * @param programPath the absolute path to the executable to validate
   * @throws AccessDeniedException if the path is not allow-listed or cannot be resolved
   */
  private void checkAllowed(String programPath) {
    boolean allowed = getCanonicalPath(programPath).map(allowedExecutables::contains).orElse(false);
    if (!allowed) {
      LOGGER.warn(
          "Rejected system.execute of '{}': not on the system.execute.allowedExecutables"
              + " allow-list",
          programPath);
      throw new AccessDeniedException(
          "system.execute is not permitted to run '" + programPath + "'");
    }
  }

  public static class ExecutionResultTypeImpl implements ExecutionResultScriptType {
    private final ExecResult execResult;

    public ExecutionResultTypeImpl(ExecResult execResult) {
      this.execResult = execResult;
    }

    @Override
    public int getCode() {
      return execResult.getExitStatus();
    }

    @Override
    public String getErrorOutput() {
      return execResult.getStderr();
    }

    @Override
    public String getStandardOutput() {
      return execResult.getStdout();
    }
  }
}
