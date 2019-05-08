/**
 * SoapService51ServiceSoapBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package com.tle.web.remoting.soap;

public class SoapService51ServiceSoapBindingStub extends org.apache.axis.client.Stub
    implements com.tle.web.remoting.soap.SoapService51 {
  private java.util.Vector cachedSerClasses = new java.util.Vector();
  private java.util.Vector cachedSerQNames = new java.util.Vector();
  private java.util.Vector cachedSerFactories = new java.util.Vector();
  private java.util.Vector cachedDeserFactories = new java.util.Vector();

  static org.apache.axis.description.OperationDesc[] _operations;

  static {
    _operations = new org.apache.axis.description.OperationDesc[66];
    _initOperationDesc1();
    _initOperationDesc2();
    _initOperationDesc3();
    _initOperationDesc4();
    _initOperationDesc5();
    _initOperationDesc6();
    _initOperationDesc7();
  }

  private static void _initOperationDesc1() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("deleteGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[0] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getContributableCollections");
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[1] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getCollection");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[2] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("editTopic");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[3] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getComment");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[4] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getSchema");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[5] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("addGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[6] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("deleteComment");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[7] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("searchGroups");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[8] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("cloneItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[9] = oper;
  }

  private static void _initOperationDesc2() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("searchItems");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in5"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in6"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in7"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[10] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("unlock");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[11] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("deleteItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[12] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("deleteUser");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[13] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("facetCount");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[14] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("userExists");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
    oper.setReturnClass(boolean.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[15] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("rejectTask");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in5"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[16] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("login");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[17] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getTopic");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[18] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getGroupsByUser");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[19] = oper;
  }

  private static void _initOperationDesc3() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getItemFilenames");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"));
    oper.setReturnClass(java.lang.String[].class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    param = oper.getReturnParamDesc();
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[20] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("addSharedOwner");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[21] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("deleteTopic");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[22] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("logout");
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[23] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("editItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[24] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("userNameExists");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
    oper.setReturnClass(boolean.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[25] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getComments");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[26] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("searchItemsFast");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in5"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in6"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in7"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in8"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[27] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("archiveItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[28] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("addUserToGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[29] = oper;
  }

  private static void _initOperationDesc4() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getTaskFilterNames");
    oper.setReturnType(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"));
    oper.setReturnClass(java.lang.String[].class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    param = oper.getReturnParamDesc();
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[30] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("removeUserFromAllGroups");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[31] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("uploadFile");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[32] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("queryCount");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
    oper.setReturnClass(int.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[33] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("listTopics");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[34] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("saveItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[35] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getUser");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[36] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("editUser");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in5"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[37] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("deleteFile");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[38] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getGroupUuidForName");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[39] = oper;
  }

  private static void _initOperationDesc5() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("itemExists");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
    oper.setReturnClass(boolean.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[40] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("createTopic");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[41] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("addUser");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in5"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[42] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("moveTopic");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[43] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("removeSharedOwner");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[44] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("loginWithToken");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[45] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("setParentGroupForGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[46] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("isUserInGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
    oper.setReturnClass(boolean.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[47] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("editGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[48] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("newItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[49] = oper;
  }

  private static void _initOperationDesc6() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("acceptTask");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[50] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("addComment");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in3"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in4"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[51] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[52] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("queryCounts");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString"),
            java.lang.String[].class,
            false,
            false);
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string"));
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfInt"));
    oper.setReturnClass(int[].class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    param = oper.getReturnParamDesc();
    param.setItemQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "int"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[53] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("newVersionItem");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[54] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getTaskList");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[55] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("keepAlive");
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[56] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getSearchableCollections");
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[57] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("groupExists");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(
        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
    oper.setReturnClass(boolean.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[58] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("searchUsersByGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[59] = oper;
  }

  private static void _initOperationDesc7() {
    org.apache.axis.description.OperationDesc oper;
    org.apache.axis.description.ParameterDesc param;
    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("setOwner");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[60] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("removeUserFromGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[61] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("removeAllUsersFromGroup");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[62] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("unzipFile");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in2"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[63] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("cancelItemEdit");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
            java.lang.String.class,
            false,
            false);
    param.setOmittable(true);
    param.setNillable(true);
    oper.addParameter(param);
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in1"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"),
            int.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    oper.addFault(
        new org.apache.axis.description.FaultDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "Exception"),
            "com.tle.web.remoting.exception.Exception",
            new javax.xml.namespace.QName("http://lang.java", "Exception"),
            true));
    _operations[64] = oper;

    oper = new org.apache.axis.description.OperationDesc();
    oper.setName("getTaskFilterCounts");
    param =
        new org.apache.axis.description.ParameterDesc(
            new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "in0"),
            org.apache.axis.description.ParameterDesc.IN,
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"),
            boolean.class,
            false,
            false);
    oper.addParameter(param);
    oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    oper.setReturnClass(java.lang.String.class);
    oper.setReturnQName(new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "out"));
    oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
    oper.setUse(org.apache.axis.constants.Use.LITERAL);
    _operations[65] = oper;
  }

  public SoapService51ServiceSoapBindingStub() throws org.apache.axis.AxisFault {
    this(null);
  }

  public SoapService51ServiceSoapBindingStub(
      java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
    this(service);
    super.cachedEndpoint = endpointURL;
  }

  public SoapService51ServiceSoapBindingStub(javax.xml.rpc.Service service)
      throws org.apache.axis.AxisFault {
    if (service == null) {
      super.service = new org.apache.axis.client.Service();
    } else {
      super.service = service;
    }
    ((org.apache.axis.client.Service) super.service).setTypeMappingVersion("1.2");
    java.lang.Class cls;
    javax.xml.namespace.QName qName;
    javax.xml.namespace.QName qName2;
    java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
    java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
    java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
    java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
    java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
    java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
    java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
    java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
    java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
    java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
    qName = new javax.xml.namespace.QName("http://lang.java", "Exception");
    cachedSerQNames.add(qName);
    cls = com.tle.web.remoting.exception.Exception.class;
    cachedSerClasses.add(cls);
    cachedSerFactories.add(beansf);
    cachedDeserFactories.add(beandf);

    qName = new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfInt");
    cachedSerQNames.add(qName);
    cls = int[].class;
    cachedSerClasses.add(cls);
    qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int");
    qName2 = new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "int");
    cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
    cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    qName = new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "ArrayOfString");
    cachedSerQNames.add(qName);
    cls = java.lang.String[].class;
    cachedSerClasses.add(cls);
    qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
    qName2 = new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "string");
    cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
    cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
  }

  protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
    try {
      org.apache.axis.client.Call _call = super._createCall();
      if (super.maintainSessionSet) {
        _call.setMaintainSession(super.maintainSession);
      }
      if (super.cachedUsername != null) {
        _call.setUsername(super.cachedUsername);
      }
      if (super.cachedPassword != null) {
        _call.setPassword(super.cachedPassword);
      }
      if (super.cachedEndpoint != null) {
        _call.setTargetEndpointAddress(super.cachedEndpoint);
      }
      if (super.cachedTimeout != null) {
        _call.setTimeout(super.cachedTimeout);
      }
      if (super.cachedPortName != null) {
        _call.setPortName(super.cachedPortName);
      }
      java.util.Enumeration keys = super.cachedProperties.keys();
      while (keys.hasMoreElements()) {
        java.lang.String key = (java.lang.String) keys.nextElement();
        _call.setProperty(key, super.cachedProperties.get(key));
      }
      // All the type mapping information is registered
      // when the first call is made.
      // The type mapping information is actually registered in
      // the TypeMappingRegistry of the service, which
      // is the reason why registration is only needed for the first call.
      synchronized (this) {
        if (firstCall()) {
          // must set encoding style before registering serializers
          _call.setEncodingStyle(null);
          for (int i = 0; i < cachedSerFactories.size(); ++i) {
            java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
            javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames.get(i);
            java.lang.Object x = cachedSerFactories.get(i);
            if (x instanceof Class) {
              java.lang.Class sf = (java.lang.Class) cachedSerFactories.get(i);
              java.lang.Class df = (java.lang.Class) cachedDeserFactories.get(i);
              _call.registerTypeMapping(cls, qName, sf, df, false);
            } else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
              org.apache.axis.encoding.SerializerFactory sf =
                  (org.apache.axis.encoding.SerializerFactory) cachedSerFactories.get(i);
              org.apache.axis.encoding.DeserializerFactory df =
                  (org.apache.axis.encoding.DeserializerFactory) cachedDeserFactories.get(i);
              _call.registerTypeMapping(cls, qName, sf, df, false);
            }
          }
        }
      }
      return _call;
    } catch (java.lang.Throwable _t) {
      throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
    }
  }

  public void deleteGroup(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[0]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "deleteGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getContributableCollections()
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[1]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName(
            "http://soap.remoting.web.tle.com", "getContributableCollections"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String getCollection(java.lang.String in0)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[2]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getCollection"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void editTopic(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[3]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "editTopic"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getComment(java.lang.String in0, int in1, java.lang.String in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[4]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getComment"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1), in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getSchema(java.lang.String in0)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[5]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getSchema"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void addGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[6]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "addGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void deleteComment(java.lang.String in0, int in1, java.lang.String in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[7]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "deleteComment"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1), in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String searchGroups(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[8]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "searchGroups"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String cloneItem(java.lang.String in0, int in1, boolean in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[9]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "cloneItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {in0, new java.lang.Integer(in1), new java.lang.Boolean(in2)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String searchItems(
      java.lang.String in0,
      java.lang.String[] in1,
      java.lang.String in2,
      boolean in3,
      int in4,
      boolean in5,
      int in6,
      int in7)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[10]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "searchItems"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0,
                in1,
                in2,
                new java.lang.Boolean(in3),
                new java.lang.Integer(in4),
                new java.lang.Boolean(in5),
                new java.lang.Integer(in6),
                new java.lang.Integer(in7)
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void unlock(java.lang.String in0, int in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[11]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "unlock"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void deleteItem(java.lang.String in0, int in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[12]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "deleteItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void deleteUser(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[13]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "deleteUser"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String facetCount(
      java.lang.String in0, java.lang.String[] in1, java.lang.String in2, java.lang.String[] in3)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[14]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "facetCount"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public boolean userExists(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[15]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "userExists"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return ((java.lang.Boolean) _resp).booleanValue();
        } catch (java.lang.Exception _exception) {
          return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
              .booleanValue();
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String rejectTask(
      java.lang.String in0,
      int in1,
      java.lang.String in2,
      java.lang.String in3,
      java.lang.String in4,
      boolean in5)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[16]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "rejectTask"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0, new java.lang.Integer(in1), in2, in3, in4, new java.lang.Boolean(in5)
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String login(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[17]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "login"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String getTopic(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[18]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getTopic"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getGroupsByUser(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[19]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getGroupsByUser"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String[] getItemFilenames(
      java.lang.String in0, int in1, java.lang.String in2, boolean in3)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[20]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getItemFilenames"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0, new java.lang.Integer(in1), in2, new java.lang.Boolean(in3)
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String[]) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String[])
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String[].class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void addSharedOwner(java.lang.String in0, int in1, java.lang.String in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[21]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "addSharedOwner"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1), in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void deleteTopic(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[22]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "deleteTopic"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void logout() throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[23]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "logout"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String editItem(java.lang.String in0, int in1, boolean in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[24]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "editItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {in0, new java.lang.Integer(in1), new java.lang.Boolean(in2)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public boolean userNameExists(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[25]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "userNameExists"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return ((java.lang.Boolean) _resp).booleanValue();
        } catch (java.lang.Exception _exception) {
          return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
              .booleanValue();
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getComments(java.lang.String in0, int in1, int in2, int in3, int in4)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[26]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getComments"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0,
                new java.lang.Integer(in1),
                new java.lang.Integer(in2),
                new java.lang.Integer(in3),
                new java.lang.Integer(in4)
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String searchItemsFast(
      java.lang.String in0,
      java.lang.String[] in1,
      java.lang.String in2,
      boolean in3,
      int in4,
      boolean in5,
      int in6,
      int in7,
      java.lang.String[] in8)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[27]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "searchItemsFast"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0,
                in1,
                in2,
                new java.lang.Boolean(in3),
                new java.lang.Integer(in4),
                new java.lang.Boolean(in5),
                new java.lang.Integer(in6),
                new java.lang.Integer(in7),
                in8
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void archiveItem(java.lang.String in0, int in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[28]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "archiveItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void addUserToGroup(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[29]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "addUserToGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String[] getTaskFilterNames() throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[30]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getTaskFilterNames"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String[]) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String[])
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String[].class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void removeUserFromAllGroups(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[31]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName(
            "http://soap.remoting.web.tle.com", "removeUserFromAllGroups"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void uploadFile(
      java.lang.String in0, java.lang.String in1, java.lang.String in2, boolean in3)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[32]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "uploadFile"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, in1, in2, new java.lang.Boolean(in3)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public int queryCount(java.lang.String[] in0, java.lang.String in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[33]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "queryCount"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return ((java.lang.Integer) _resp).intValue();
        } catch (java.lang.Exception _exception) {
          return ((java.lang.Integer) org.apache.axis.utils.JavaUtils.convert(_resp, int.class))
              .intValue();
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String listTopics(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[34]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "listTopics"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String saveItem(java.lang.String in0, boolean in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[35]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "saveItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Boolean(in1)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String getUser(java.lang.String in0)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[36]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getUser"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String editUser(
      java.lang.String in0,
      java.lang.String in1,
      java.lang.String in2,
      java.lang.String in3,
      java.lang.String in4,
      java.lang.String in5)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[37]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "editUser"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3, in4, in5});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void deleteFile(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[38]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "deleteFile"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String getGroupUuidForName(java.lang.String in0)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[39]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getGroupUuidForName"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public boolean itemExists(java.lang.String in0, int in1) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[40]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "itemExists"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return ((java.lang.Boolean) _resp).booleanValue();
        } catch (java.lang.Exception _exception) {
          return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
              .booleanValue();
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String createTopic(java.lang.String in0, java.lang.String in1, int in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[41]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "createTopic"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, in1, new java.lang.Integer(in2)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String addUser(
      java.lang.String in0,
      java.lang.String in1,
      java.lang.String in2,
      java.lang.String in3,
      java.lang.String in4,
      java.lang.String in5)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[42]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "addUser"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3, in4, in5});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void moveTopic(java.lang.String in0, java.lang.String in1, int in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[43]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "moveTopic"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, in1, new java.lang.Integer(in2)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void removeSharedOwner(java.lang.String in0, int in1, java.lang.String in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[44]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "removeSharedOwner"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1), in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String loginWithToken(java.lang.String in0)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[45]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "loginWithToken"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void setParentGroupForGroup(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[46]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName(
            "http://soap.remoting.web.tle.com", "setParentGroupForGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public boolean isUserInGroup(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[47]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "isUserInGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return ((java.lang.Boolean) _resp).booleanValue();
        } catch (java.lang.Exception _exception) {
          return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
              .booleanValue();
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void editGroup(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[48]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "editGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String newItem(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[49]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "newItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String acceptTask(
      java.lang.String in0, int in1, java.lang.String in2, boolean in3)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[50]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "acceptTask"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0, new java.lang.Integer(in1), in2, new java.lang.Boolean(in3)
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void addComment(java.lang.String in0, int in1, java.lang.String in2, int in3, boolean in4)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[51]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "addComment"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {
                in0,
                new java.lang.Integer(in1),
                in2,
                new java.lang.Integer(in3),
                new java.lang.Boolean(in4)
              });

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getItem(java.lang.String in0, int in1, java.lang.String in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[52]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1), in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public int[] queryCounts(java.lang.String[] in0, java.lang.String[] in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[53]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "queryCounts"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (int[]) _resp;
        } catch (java.lang.Exception _exception) {
          return (int[]) org.apache.axis.utils.JavaUtils.convert(_resp, int[].class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String newVersionItem(java.lang.String in0, int in1, boolean in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[54]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "newVersionItem"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {in0, new java.lang.Integer(in1), new java.lang.Boolean(in2)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getTaskList(java.lang.String in0, int in1, int in2)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[55]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getTaskList"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(
              new java.lang.Object[] {in0, new java.lang.Integer(in1), new java.lang.Integer(in2)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void keepAlive() throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[56]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "keepAlive"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String getSearchableCollections()
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[57]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName(
            "http://soap.remoting.web.tle.com", "getSearchableCollections"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public boolean groupExists(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[58]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "groupExists"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return ((java.lang.Boolean) _resp).booleanValue();
        } catch (java.lang.Exception _exception) {
          return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
              .booleanValue();
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public java.lang.String searchUsersByGroup(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[59]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "searchUsersByGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void setOwner(java.lang.String in0, int in1, java.lang.String in2)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[60]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "setOwner"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1), in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void removeUserFromGroup(java.lang.String in0, java.lang.String in1)
      throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[61]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "removeUserFromGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void removeAllUsersFromGroup(java.lang.String in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[62]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName(
            "http://soap.remoting.web.tle.com", "removeAllUsersFromGroup"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }

  public void unzipFile(java.lang.String in0, java.lang.String in1, java.lang.String in2)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[63]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "unzipFile"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public void cancelItemEdit(java.lang.String in0, int in1)
      throws java.rmi.RemoteException, com.tle.web.remoting.exception.Exception {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[64]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "cancelItemEdit"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp =
          _call.invoke(new java.lang.Object[] {in0, new java.lang.Integer(in1)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      }
      extractAttachments(_call);
    } catch (org.apache.axis.AxisFault axisFaultException) {
      if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
          throw (java.rmi.RemoteException) axisFaultException.detail;
        }
        if (axisFaultException.detail instanceof com.tle.web.remoting.exception.Exception) {
          throw (com.tle.web.remoting.exception.Exception) axisFaultException.detail;
        }
      }
      throw axisFaultException;
    }
  }

  public java.lang.String getTaskFilterCounts(boolean in0) throws java.rmi.RemoteException {
    if (super.cachedEndpoint == null) {
      throw new org.apache.axis.NoEndPointException();
    }
    org.apache.axis.client.Call _call = createCall();
    _call.setOperation(_operations[65]);
    _call.setUseSOAPAction(true);
    _call.setSOAPActionURI("");
    _call.setEncodingStyle(null);
    _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
    _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
    _call.setOperationName(
        new javax.xml.namespace.QName("http://soap.remoting.web.tle.com", "getTaskFilterCounts"));

    setRequestHeaders(_call);
    setAttachments(_call);
    try {
      java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Boolean(in0)});

      if (_resp instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) _resp;
      } else {
        extractAttachments(_call);
        try {
          return (java.lang.String) _resp;
        } catch (java.lang.Exception _exception) {
          return (java.lang.String)
              org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
        }
      }
    } catch (org.apache.axis.AxisFault axisFaultException) {
      throw axisFaultException;
    }
  }
}
