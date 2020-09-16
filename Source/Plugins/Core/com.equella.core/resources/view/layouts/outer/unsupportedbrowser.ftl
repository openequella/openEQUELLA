<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/><#t/>
<!DOCTYPE html>
<html>
<head>
  <style>
    html {
      font-family: Arial, Helvetica, sans-serif;
      background-color: #FAFAFA;
      text-align: center;
      line-height: 1.5em;
    }

    #unsupported {
      background-color: #FFFFFF;
      position: absolute;
      width: 50%;
      left: 50%;
      box-shadow: 0px 2px 1px -1px rgba(0, 0, 0, 0.2), 0px 1px 1px 0px rgba(0, 0, 0, 0.14), 0px 1px 3px 0px rgba(0, 0, 0, 0.12);
      padding: 16px;
      border-radius: 10px;
      -ms-transform: translate(-50%);
      margin-top: 24px;
    }
  </style>
</head>
<div id="unsupported">
  <h1>${b.key('unsupportedbrowser.heading')}</h1>
  <p>${b.key('unsupportedbrowser.message')}
  </p>
</div>
</html>
