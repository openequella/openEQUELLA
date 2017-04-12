<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="shop/viewitem.css" hasRtl=true />

<h2>${m.name}</h2>

<#if m.description??>
	<h3><@bundlekey "shop.viewitem.description.title" /></h3>
	<p>${m.description}</p>
</#if>

<#if m.attachmentRows?has_content>
	<h3><@bundlekey "shop.viewitem.attachments.title" /></h3>
	<@render section=s.div class="attachments">
		<ul class="attachments-browse structured">
			<#list m.attachmentRows as attachmentRow>
				<@render attachmentRow />
			</#list>
		</ul>
	</@render>
</#if>