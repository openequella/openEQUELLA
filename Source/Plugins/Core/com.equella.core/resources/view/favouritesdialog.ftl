<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css path="favouritesdialog.css" hasRtl=true />

<h3>${b.key("add.description")}</h3>
<p>${b.key("favourites.description")}</p>

<@textfield section=s.tagsField autoSubmitButton=s.ok size=50 class="focus" />

<#if m.showVersion>	
	<p>${b.key("selectversion")}</p>
	<@checklist section=s.version class="input radio"/>
<#else>
	<p>${b.key("thisversiononly")}</p>
</#if>