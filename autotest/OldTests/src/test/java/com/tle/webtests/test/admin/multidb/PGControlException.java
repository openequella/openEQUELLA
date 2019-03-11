package com.tle.webtests.test.admin.multidb;

import com.tle.common.util.ExecUtils.ExecResult;

public class PGControlException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final ExecResult execResult;

  public PGControlException(String msg, ExecResult result) {
    super(msg + "\n" + result.getStderr());
    this.execResult = result;
  }

  public ExecResult getExecResult() {
    return execResult;
  }
}
