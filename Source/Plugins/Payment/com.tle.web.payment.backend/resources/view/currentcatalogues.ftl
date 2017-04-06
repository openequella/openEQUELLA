<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />
<@css path="currentcatalogues.css" hasRtl=true />

<h2>${b.key('viewitem.current.title')}</h2>
<#if !m.pricing>
	<p>${b.key('viewitem.current.help')}</p>
</#if>
<@div id="catalogues">
	<@render s.currentCatalogues />
</@div>

<div id="key">
	<div class="live"><p>${b.key('viewitem.current.live')}</p></div>
	<div class="notlive"><p>${b.key('viewitem.current.notlive')}</p></div>
</div>