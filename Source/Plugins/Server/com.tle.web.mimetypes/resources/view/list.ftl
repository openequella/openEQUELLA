<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl"/>

<@css path="mime.css" hasRtl=true />

<#list m.items as item>
<div class="itemresult-wrapper">
	<div class="itemresult">
		<div class="itemresult-content">
			<#if item.image??>
				<@render item.image />
			</#if>
			<h3><@link item.title /></h3>
			<p>${item.description}</p>
			<div class="itemresult-meta">
				<#list item.metadata as meta>
					<strong>${meta.label}:</strong>
					<@render meta.value/>
					<br>
				</#list>
			</div>
			<div style="clear:both;"></div>
		</div>
	</div>
	<div class="itemresult-rating">
		<div class="float-right">
			<@renderList item.actions />
		</div>
	</div>
</div>
</#list>