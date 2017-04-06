<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl"/>

<@css path="shop/itemlist.css" hasRtl=true />

<#list m.items as item>
<div class="itemresult-wrapper">
	<div class="itemresult">
		<div class="itemresult-content">
			<#if item.thumbnail??>
				<div class="thumbnailinlist">
					<@render item.thumbnail />
				</div>
			</#if>
			<h3><@link item.title /></h3>
			<p>${item.description}</p>
			<div class="itemresult-meta">
				<#list item.metadata as meta>
					<div class="itemresult-metaline">
						<strong>${meta.label}:</strong>
						<span class="greentext"><@render meta.value/></span>
					</div>
				</#list>
			</div>
		<div style="clear:both;"></div>
		</div>
	</div>
</div>
<hr/>
</#list>
