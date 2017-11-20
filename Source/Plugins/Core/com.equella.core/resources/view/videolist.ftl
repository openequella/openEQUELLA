<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>


<@css path="videolist.css" hasRtl=true />

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
			<@div id="preview"+item.item.uuid+item.item.version  class="video-preview">
				<div></div>
			</@div>
			
			<div class="video-preview-placeholder">
				<@renderList item.extras/>
			</div>
		</#if>
		
	</@div>
</#list>