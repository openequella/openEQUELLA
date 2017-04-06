<#ftl strip_whitespace=true/><#t/>
<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/><#t/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/><#t/>
<#t/>
<!DOCTYPE html>
<#assign template = m.template><#t/>
<#t/>
<#assign body><#t/>
	<@render m.body>
		<@render m.form><@render template["body"]/><@render template["unnamed"]/></@render>
		<@render template["tail"]/>
	</@render>
</#assign><#t/>
<#t/>
<html lang="${m.lang?html}" <#if m.rightToLeft>dir="rtl"</#if> ${m.htmlAttrs}>
	<head>
		<@render template["header"] />
	</head>
	${body}		
</html>
