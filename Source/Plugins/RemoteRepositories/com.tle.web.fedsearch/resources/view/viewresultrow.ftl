<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "viewresult.css" />

<h3><@render m.label/></h3>

<#if m.renderable??>
	<p><@render m.renderable /><p>

<#else>
	<p>
	<#if m.text?size == 1>
		${m.text[0]}
	<#else>
		<ul class="fieldvalues">
		<#list m.text as t>
			<li>${t}</li>
		</#list>
		</ul>
	</#if>
	</p>
</#if>