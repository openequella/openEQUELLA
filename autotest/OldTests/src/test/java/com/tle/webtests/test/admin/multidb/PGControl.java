package com.tle.webtests.test.admin.multidb;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import java.io.File;
import java.util.List;
import java.util.Map;

public class PGControl {
  private String hostname;
  private String username;
  private String password;
  private String version;

  private void addDefaultParams(List<String> args, Map<String, String> env) {
    args.add("--host");
    args.add(hostname);
    args.add("--username");
    args.add(username);
    args.add("--port");
    args.add(version + "11");
    env.put("PGPASSWORD", password);
  }

  public void restore(String database, File restoreFile) {
    dropLang(database);
    List<String> args = Lists.newArrayList();
    Map<String, String> env = Maps.newHashMap();
    args.add("pg_restore");
    addDefaultParams(args, env);
    args.add("--dbname");
    args.add(database);
    args.add(restoreFile.getAbsolutePath());
    exec(args, env, "Failed to fully restore '" + restoreFile + "'");
  }

  private void dropLang(String database) {
    List<String> args = Lists.newArrayList();
    Map<String, String> env = Maps.newHashMap();
    args.add("droplang");
    addDefaultParams(args, env);
    args.add("--dbname");
    args.add(database);
    args.add("plpgsql");
    ExecUtils.exec(args.toArray(new String[args.size()]), env, new File("."));
  }

  public void dropDb(String database) {
    List<String> args = Lists.newArrayList();
    Map<String, String> env = Maps.newHashMap();
    args.add("dropdb");
    addDefaultParams(args, env);
    args.add(database);
    ExecResult exec = ExecUtils.exec(args.toArray(new String[args.size()]), env, new File("."));
    if (exec.getExitStatus() != 0) {
      String notExist =
          "dropdb: database removal failed: ERROR:  database \"" + database + "\" does not exist";
      if (exec.getStderr().startsWith(notExist)) {
        return;
      }
      throw new PGControlException("Failed to drop database '" + database + "'", exec);
    }
  }

  public void createDb(String database) {
    List<String> args = Lists.newArrayList();
    Map<String, String> env = Maps.newHashMap();
    args.add("createdb");
    addDefaultParams(args, env);
    args.add(database);
    exec(args, env, "Failed to create database '" + database + "'");
  }

  public boolean checkDbUp(String database) {
    List<String> args = Lists.newArrayList();
    Map<String, String> env = Maps.newHashMap();
    args.add("psql");
    args.add("-c");
    args.add("'select 1'");
    addDefaultParams(args, env);
    args.add(database);
    ExecResult exec = ExecUtils.exec(args.toArray(new String[args.size()]), env, new File("."));
    return exec.getExitStatus() != 0;
  }

  private void exec(List<String> args, Map<String, String> env, String error) {
    ExecResult exec = ExecUtils.exec(args.toArray(new String[args.size()]), env, new File("."));
    if (exec.getExitStatus() != 0) {
      throw new PGControlException(error, exec);
    }
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
