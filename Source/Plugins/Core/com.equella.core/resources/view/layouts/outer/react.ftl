<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/><#t/>
<#assign template = m.template><#t/>
<#assign faviconSizes = ["32x32", "48x48", "64x64", "96x96", "128x128", "196x196", "320x320", "400x400", "640x640"]>
<#assign faviconAppleSizes = ["120x120", "152x152", "167x167", "180x180"]>
<!DOCTYPE html>
<html class="newui" lang="${m.lang?html}" <#if m.rightToLeft>dir="rtl"</#if> ${m.htmlAttrs}>
    <head>
      <meta http-equiv="X-UA-Compatible" content="IE=edge" >
    	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    	<meta name="viewport" content="width=device-width, initial-scale=1">
    	<base href="${baseHref}">

			<!-- Favicon -->
			<link rel="icon" type="image/vnd.microsoft.icon" href="${p.instUrl(p.url("images/favicon.ico"))}">
			<#list faviconAppleSizes as size>
				<link rel="apple-touch-icon" sizes="${size}" href="${p.instUrl(p.url('images/favicon.' + size + 'px.png'))}">
			</#list>
			<#list faviconSizes as size>
				<link rel="icon" type="image/png" sizes="${size}" href="${p.instUrl(p.url('images/favicon.' + size + 'px.png'))}">
			</#list>
			<!-- SVG version for high-res displays -->
			<link rel="icon" type="image/svg+xml" href=${p.instUrl(p.url('images/favicon.svg'))}>

      <noscript id="_dynamicStart"></noscript>
      <noscript id="_dynamicInsert"></noscript>
      <@render template["header"]/>

    	<script type="text/javascript">
      	var renderData = ${m.renderJs};
    	</script>
      ${m.head}
    </head>
    ${m.body}
</html>
