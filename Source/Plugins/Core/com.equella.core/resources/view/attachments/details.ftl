<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div id="${m.id}" class="detail">

	<div class="attachments-thumb">
		<@render m.thumbnail />
	</div>

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