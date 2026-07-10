package com.tle.webtests.framework.soap;

/**
 * Client-side mirror of {@code com.tle.core.harvester.soap.SoapHarvesterService}, used to build a
 * dynamic SOAP proxy against the "/SoapHarvesterService" endpoint. Only the methods actually
 * exercised by tests need to be declared here.
 */
public interface SoapHarvesterService {
  String login(String username, String password) throws Exception;
}
