<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@setting label=b.key('tax.label.code') 
	help=b.key('tax.help.code')
	error=m.errors["code"]
	mandatory=true 
	section=s.code />

<@setting label=b.key('tax.label.percent') 
	help=b.key('tax.help.percent') 
	error=m.errors["percent"]
	mandatory=true
	section=s.percent />
