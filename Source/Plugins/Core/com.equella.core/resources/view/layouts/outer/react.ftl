<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/><#t/>
<#assign template = m.template><#t/>
<!DOCTYPE html>
<html lang="${m.lang?html}" <#if m.rightToLeft>dir="rtl"</#if> ${m.htmlAttrs}>
    <head>
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
