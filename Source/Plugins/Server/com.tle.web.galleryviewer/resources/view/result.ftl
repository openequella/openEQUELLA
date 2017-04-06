<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<#function keyEmpty key>
	<#local list=m.getRendererList(key)>
	<#return !(list?has_content)>
</#function>

<#macro renderKey key>
	<#local list=m.getRendererList(key)>
	<#if list?size gt 0>
		<@renderList list/>
	<#else>
		&nbsp;
	</#if>
</#macro>

<#assign extra="">
<#if !keyEmpty('actions')>
	<#assign extra="result_with_actions">
</#if>

<div id="result${m.resultIndex}" class="result_imageoverall ${extra}">
	<div class="result_checkbox">
		<@renderKey 'checkbox'/>
	</div>
	<div class="result_imagemiddle">
		<@renderKey 'middle'/>
	</div>
	<div class="result_imageright">
		<@renderKey 'right'/>
	</div>
	<#if !keyEmpty('actions')>
		<div class="result_actions">
			<@renderKey 'right'/>
			<@renderKey 'actions'/>
		</div>
	</#if>
</div>
<@renderKey 'separator' />