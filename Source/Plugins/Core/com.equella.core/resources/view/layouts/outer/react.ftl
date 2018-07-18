<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/><#t/>
<#assign template = m.template><#t/>
<!DOCTYPE html>
<html lang="${m.lang?html}" <#if m.rightToLeft>dir="rtl"</#if> ${m.htmlAttrs}>
    <head>
    	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    	<meta http-equiv="X-UA-Compatible" content="IE=edge" >
    	<meta name="viewport" content="width=device-width, initial-scale=1">
    	<base href="${baseHref}">

    	<link rel="apple-touch-icon" href="${p.url("images/apple-touch-icon.png")}">
    	<link rel="icon" type="image/png" href="${p.instUrl(p.url("images/favicon.png"))}">
    	<link rel="icon" type="image/vnd.microsoft.icon" href="${p.instUrl(p.url("images/favicon.ico"))}">
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <noscript id="_dynamicStart"></noscript>
        <noscript id="_dynamicInsert"></noscript>
        <@render template["header"]/>
    	<script type="text/javascript">
    	var renderData = ${m.renderJs};
    	</script>
    </head>
    <body>
        <div id="mainDiv"></div>
        <script src="${m.scriptUrl}"></script>
        <@render template["postmarkup"]/>
    </body>
</html>
