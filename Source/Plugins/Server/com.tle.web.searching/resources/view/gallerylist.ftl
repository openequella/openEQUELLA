<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl">

<@css path="gallerylist.css" hasRtl=true />

<#if m.nullItemsRemoved?? && m.nullItemsRemoved==true>
	<div id="warningInfo">
		<strong>${b.gkey('command.purge.purgedfromresults')}</strong>
	</div>
</#if>
<#list m.items as item>
	<@div tag=item.tag class="itemresult-wrapper">
		<#if item.thumbnailCount??>
			${item.thumbnailCount}
		</#if>
		<@render item.thumbnail />
		<#if item.ratingBarLeft??>
			<div class="thumb-bar">
				<@renderList item.ratingBarLeft />
			</div>
		</#if>
		<#if item.extras??>
			<div class="gallery-preview">
				<div class="preview-arrow"></div>
				<@renderList item.extras/>
			</div>
		</#if>
	</@div>
</#list>