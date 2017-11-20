<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<@css "terms.css" />
<#assign drm = m.drm>
<h3>${b.key("summary.content.termsofuse.terms.title")}</h3>
<p>${b.key("summary.content.termsofuse.terms.description")}</p> 
<ol class="termsofuse focus" tabIndex="0">
	<#if drm.hasPermissions1>
		<li>${drm.itemMayFreelyBeText?html} ${drm.permissions1List?html}</li>
	</#if>
	<#if drm.hasPermissions2>
		<li>${drm.additionallyUserMayText?html} ${drm.permissions2List?html}</li>
	</#if>
	<#if drm.useEducation>
		<li>${drm.educationSectorText?html}</li>
	</#if>
    <#if drm.attribution && drm.parties?size gt 0>
		<li>${drm.attributeOwnersText?html}
			<ul>
				<#list drm.parties as party>
					<li>${party.name?html} (${party.email?html})</li>
				</#list>
			</ul>
		</li>
	</#if>
	<#if drm.terms??>
		<li>
			${drm.termsText?html}
			<div class="terms">
				${drm.termsAsHtml}
			</div>
		</li>
	</#if>
</ol>
