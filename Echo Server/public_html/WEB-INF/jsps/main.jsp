<%@ page contentType="text/html"%>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="n"%>

<html>
  <head>
    <link rel="stylesheet" href="styles.css" type="text/css">
    <title>Echo Server</title>
  </head>
  <body>
    <n:form action="/index">
      <n:hidden property="method" />
      
      <div class="formrow">
        <label>
          Text to echo:
        </label>
        
        <span id="toecho">
          <n:text property="echo" styleClass="formcontrol" />
        </span>
      </div>
      
      <div class="formrow">
        <label>
          Echoed:
        </label>
        
        <span id="echoed"><n:write property="echo"/></span>
      </div>
      
      <div class="formrow">
        <label>
          &nbsp;
        </label>
        
        <span id="submit">
          <n:submit value="Submit" />
        </span>
      </div>
      
    </n:form>
  </body>
</html>
