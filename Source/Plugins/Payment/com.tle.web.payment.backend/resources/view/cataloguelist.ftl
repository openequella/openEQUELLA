<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />
<#include "/com.tle.web.sections.standard@/shufflebox.ftl" />

<@css "cataloguelist.css" />

<@div id="cataloguelist">
	<h3>${b.key('viewitem.catalogues.select')}</h3>
	
	<@shufflebox section=s.catalogueList />
</@div>