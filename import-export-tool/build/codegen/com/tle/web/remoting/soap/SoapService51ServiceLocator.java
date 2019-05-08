/**
 * SoapService51ServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package com.tle.web.remoting.soap;

public class SoapService51ServiceLocator extends org.apache.axis.client.Service
    implements com.tle.web.remoting.soap.SoapService51Service {

  public SoapService51ServiceLocator() {}

  public SoapService51ServiceLocator(org.apache.axis.EngineConfiguration config) {
    super(config);
  }

  public SoapService51ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName)
      throws javax.xml.rpc.ServiceException {
    super(wsdlLoc, sName);
  }

  // Use to get a proxy class for SoapService51Endpoint
  private java.lang.String SoapService51Endpoint_address =
      "http://origliasso/Shiny/my/services/SoapService51";

  public java.lang.String getSoapService51EndpointAddress() {
    return SoapService51Endpoint_address;
  }

  // The WSDD service name defaults to the port name.
  private java.lang.String SoapService51EndpointWSDDServiceName = "SoapService51Endpoint";

  public java.lang.String getSoapService51EndpointWSDDServiceName() {
    return SoapService51EndpointWSDDServiceName;
  }

  public void setSoapService51EndpointWSDDServiceName(java.lang.String name) {
    SoapService51EndpointWSDDServiceName = name;
  }

  public com.tle.web.remoting.soap.SoapService51 getSoapService51Endpoint()
      throws javax.xml.rpc.ServiceException {
    java.net.URL endpoint;
    try {
      endpoint = new java.net.URL(SoapService51Endpoint_address);
    } catch (java.net.MalformedURLException e) {
      throw new javax.xml.rpc.ServiceException(e);
    }
    return getSoapService51Endpoint(endpoint);
  }

  public com.tle.web.remoting.soap.SoapService51 getSoapService51Endpoint(java.net.URL portAddress)
      throws javax.xml.rpc.ServiceException {
    try {
      com.tle.web.remoting.soap.SoapService51ServiceSoapBindingStub _stub =
          new com.tle.web.remoting.soap.SoapService51ServiceSoapBindingStub(portAddress, this);
      _stub.setPortName(getSoapService51EndpointWSDDServiceName());
      return _stub;
    } catch (org.apache.axis.AxisFault e) {
      return null;
    }
  }

  public void setSoapService51EndpointEndpointAddress(java.lang.String address) {
    SoapService51Endpoint_address = address;
  }

  /**
   * For the given interface, get the stub implementation. If this service has no port for the given
   * interface, then ServiceException is thrown.
   */
  public java.rmi.Remote getPort(Class serviceEndpointInterface)
      throws javax.xml.rpc.ServiceException {
    try {
      if (com.tle.web.remoting.soap.SoapService51.class.isAssignableFrom(
          serviceEndpointInterface)) {
        com.tle.web.remoting.soap.SoapService51ServiceSoapBindingStub _stub =
            new com.tle.web.remoting.soap.SoapService51ServiceSoapBindingStub(
                new java.net.URL(SoapService51Endpoint_address), this);
        _stub.setPortName(getSoapService51EndpointWSDDServiceName());
        return _stub;
      }
    } catch (java.lang.Throwable t) {
      throw new javax.xml.rpc.ServiceException(t);
    }
    throw new javax.xml.rpc.ServiceException(
        "There is no stub implementation for the interface:  "
            + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
  }

  /**
   * For the given interface, get the stub implementation. If this service has no port for the given
   * interface, then ServiceException is thrown.
   */
  public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
      throws javax.xml.rpc.ServiceException {
    if (portName == null) {
      return getPort(serviceEndpointInterface);
    }
    java.lang.String inputPortName = portName.getLocalPart();
    if ("SoapService51Endpoint".equals(inputPortName)) {
      return getSoapService51Endpoint();
    } else {
      java.rmi.Remote _stub = getPort(serviceEndpointInterface);
      ((org.apache.axis.client.Stub) _stub).setPortName(portName);
      return _stub;
    }
  }

  public javax.xml.namespace.QName getServiceName() {
    return new javax.xml.namespace.QName(
        "http://soap.remoting.web.tle.com", "SoapService51Service");
  }

  private java.util.HashSet ports = null;

  public java.util.Iterator getPorts() {
    if (ports == null) {
      ports = new java.util.HashSet();
      ports.add(
          new javax.xml.namespace.QName(
              "http://soap.remoting.web.tle.com", "SoapService51Endpoint"));
    }
    return ports.iterator();
  }

  /** Set the endpoint address for the specified port name. */
  public void setEndpointAddress(java.lang.String portName, java.lang.String address)
      throws javax.xml.rpc.ServiceException {

    if ("SoapService51Endpoint".equals(portName)) {
      setSoapService51EndpointEndpointAddress(address);
    } else { // Unknown Port Name
      throw new javax.xml.rpc.ServiceException(
          " Cannot set Endpoint Address for Unknown Port" + portName);
    }
  }

  /** Set the endpoint address for the specified port name. */
  public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address)
      throws javax.xml.rpc.ServiceException {
    setEndpointAddress(portName.getLocalPart(), address);
  }
}
