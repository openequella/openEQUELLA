<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="contribute.css" hasRtl=true />

<div class="area">

	<#if !m.hideResumable && m.resumables?size gt 0>
		<h2>${b.key("resumablewizards")}</h2>
		<p>${b.key("resumablewizardsinfo")}</p>
		<div class="resumeables">
			<ul>
				<#list m.resumables as resumeable >
					<li>
						<span class="resumable"><@render resumeable.resumeLink>${resumeable.collectionName}</@render>  - ${resumeable.startedDate}</span>
						<span class="removelink">[<@render resumeable.removeLink />]</span>
					</li>
				</#list>
			</ul>
		</div>
	</#if>

	<#if m.categories?size == 0>
		<h2>${b.key("nowizards")}</h2>
	<#else>
		<h2>${b.key("help")}</h2>

		<#list m.categories as category>
				<@render section=category class="large" />
		</#list>
	</#if>
</div>
