<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#--<#include "/com.tle.web.sections.standard@/ajax.ftl"/>-->

<@css "htmleditorwebcontrol.css"/>

<#if m.lazyLoad && c.enabled>
<@render section=s.editLink class="toggleEditLink"><#if m.editing><@bundlekey "editlink.turnoff"/><#else><@bundlekey "editlink.turnon"/></#if></@render>
</#if>

<#--<@div id="${id}content">-->
<#if c.enabled && (!m.lazyLoad || m.editing)>
<@render m.editor />
<#else>
<div class="lockedHtml">${m.staticHtml}</div>
</#if>
<#--</@div>-->