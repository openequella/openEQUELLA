package com.tle.configmanager;

// Author: Andrew Gibb

public class ConfigProfile {
  private String profName;

  // Hibernate Details
  private String dbtype;
  private String port;
  private String host;
  private String database;
  private String username;
  private String password;

  // Old mandatory
  @Deprecated private String tomcat; // deprecated
  @Deprecated private String clustergroup;

  // Mandatory Details
  private String https;
  private String http;
  private String ajp;
  private String filestore;
  private String javahome;
  private String adminurl;
  private String freetext;
  private String stopwords;
  private String reporting;
  private String plugins;

  // Optional Details
  private boolean devinst;
  private String conversion;
  private String imagemagick;

  public ConfigProfile(String name) {
    this.profName = name;
  }

  public String getProfName() {
    return profName;
  }

  public void setProfName(String profName) {
    this.profName = profName;
  }

  public String getDbtype() {
    return dbtype;
  }

  public void setDbtype(String dbtype) {
    this.dbtype = dbtype;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
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

  @Deprecated
  public String getTomcat() {
    return tomcat;
  }

  @Deprecated
  public void setTomcat(String tomcat) {
    this.tomcat = tomcat;
  }

  public String getFilestore() {
    return filestore;
  }

  public void setFilestore(String filestore) {
    this.filestore = filestore;
  }

  public String getJavahome() {
    return javahome;
  }

  public void setJavahome(String javahome) {
    this.javahome = javahome;
  }

  public String getAdminurl() {
    return adminurl;
  }

  public void setAdminurl(String adminurl) {
    this.adminurl = adminurl;
  }

  public String getFreetext() {
    return freetext;
  }

  public void setFreetext(String freetext) {
    this.freetext = freetext;
  }

  public String getStopwords() {
    return stopwords;
  }

  public void setStopwords(String stopwords) {
    this.stopwords = stopwords;
  }

  @Deprecated
  public String getClustergroup() {
    return clustergroup;
  }

  @Deprecated
  public void setClustergroup(String clustergroup) {
    this.clustergroup = clustergroup;
  }

  public String getReporting() {
    return reporting;
  }

  public void setReporting(String reporting) {
    this.reporting = reporting;
  }

  public String getPlugins() {
    return plugins;
  }

  public void setPlugins(String plugins) {
    this.plugins = plugins;
  }

  public boolean isDevinst() {
    return devinst;
  }

  public void setDevinst(boolean devinst) {
    this.devinst = devinst;
  }

  public String getConversion() {
    return conversion;
  }

  public void setConversion(String conversion) {
    this.conversion = conversion;
  }

  public String getImagemagick() {
    return imagemagick;
  }

  public void setImagemagick(String imagemagik) {
    this.imagemagick = imagemagik;
  }

  @Override
  public String toString() {
    return profName;
  }

  public String getHttps() {
    return https;
  }

  public void setHttps(String https) {
    this.https = https;
  }

  public String getHttp() {
    return http;
  }

  public void setHttp(String http) {
    this.http = http;
  }

  public String getAjp() {
    return ajp;
  }

  public void setAjp(String ajp) {
    this.ajp = ajp;
  }
}
