<%@ page contentType="text/html"%>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="n"%>

<html>
  <head>
    <link rel="stylesheet" href="styles.css" type="text/css">
    <title>Integration Tester</title>
  </head>
  <body>
    <n:form action="/index">
      <n:hidden property="method" />
      <n:iterate property="returnVals">
        <div class="formrow">
          <label>
            <n:write property="name"/>:
          </label>
          <n:textarea property="value" styleClass="itemXml" />
        </div>
      </n:iterate>
    </n:form>
  </body>
</html>
