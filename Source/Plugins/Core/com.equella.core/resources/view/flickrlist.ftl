<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="flickr.css" hasRtl=true />

<#list m.items as item>
	<div class="itemresult-wrapper">
		<h3><@render item.title/></h3>
		<p><#if item.description??>${item.description}</#if></p>
		<#if item.metadata??>
			<div class="itemresult-meta">
				<#list item.metadata as meta>
					<strong>${meta.label}:</strong>
					<@render meta.value/>
					<br>
				</#list>
			</div>
		</#if>
	</div>
</#list>

