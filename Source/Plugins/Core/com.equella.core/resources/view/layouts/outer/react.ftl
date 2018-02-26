<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/><#t/>
<#assign template = m.template><#t/>
<!DOCTYPE html>
<html>
    <head>
        <@render template["header"]/>
    	<script type="text/javascript">
    	var renderData = ${m.renderJs};
    	</script>
    </head>
    <body>
        <div id="mainDiv" style="min-height: calc(100vh + 1px);"></div>
        <script src="${m.scriptUrl}"></script>
    </body>
</html>
