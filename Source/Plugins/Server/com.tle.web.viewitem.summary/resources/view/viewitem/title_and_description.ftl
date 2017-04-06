<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "titleanddesc.css" />

<div class="item-title-and-desc">
	<#if m.selectDiv??>
		<@render m.selectDiv />
	</#if>
	
	<h2 class="item-title<#if !m.selectable>-unselectable</#if>" data-itemuuid="${m.dataUuid}" data-itemversion="${m.dataVersion}"<#if m.dataExtensionType??> data-extensiontype="${m.dataExtensionType}"</#if>>
		<#if m.nameLength gt 0>
			<@wrap maxlength=m.nameLength maxwords=200>${m.name?html}</@wrap>
		<#else>
			${m.name?html}
		</#if>
	</h2>
	
	<#if m.description?? && m.description?length gt 0>
		<h3 class="item-description-title">${b.key("titleanddescription.description")}</h3>
		<p class="item-description">
			<#if m.descLength gt 0>
				<@wrap maxlength=m.descLength maxwords=200>${m.description?html}</@wrap>
			<#else>
				${m.description?html}
			</#if>
		</p>
	</#if>
</div>