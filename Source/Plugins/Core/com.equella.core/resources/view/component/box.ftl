<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@script "component/box.js"/>


<#assign bhc = "box_head" />
<#if !m.minimise??>
	<#assign bhc = bhc + " nominimise" />
</#if>
<#if !(m.edit?? || m.close??)>
	<#assign bhc = bhc + " noactions" />
</#if>


<@render section=m.boxHead class=bhc>
	<div class="box_title_wrapper" tabIndex="0" 
		title="${m.title}">
		<h3><#rt/>${m.title}<#t/></h3><#lt/>
	</div>
	<#if m.minimise??><@render section=m.minimise class="box_minimise" /></#if><#t/>
	<#if m.edit??><@render section=m.edit class="box_edit" /></#if><#t/>
	<#if m.close??><@render section=m.close class="box_close" /></#if><#t/>
</@render>


<#if !m.minimised && m.result??>
	<div class="box_content">
		<div class="box_content_inner">
			<@render m.result />
		</div>
	</div>
</#if>

