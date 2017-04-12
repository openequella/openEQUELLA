<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<@css "attachments/attachments.css" />

<div id="${m.id}" class="detail">

	<#if m.thumbnail??>
		<div class="attachments-thumb">
			<@render m.thumbnail />
		</div>
	</#if>

	<div class="attachments-meta">
		<#list m.details as detail >
			<span style="display: block; padding-top: 2px;">
				<b>${detail.name}</b> ${detail.description}
			</span>
		</#list>
	</div>

	<#if m.viewerLinks?size gt 0>
		<hr>
		<@renderAsHtmlList list=m.viewerLinks class="attachments-actions" />
	</#if>

</div>