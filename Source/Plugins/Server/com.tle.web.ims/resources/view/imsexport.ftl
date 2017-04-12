<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "imsexport.css" />

<div id="imsExport">
	<h3><@bundlekey "export.imstitle"/></h3>
	<ul>
		<li>
			<@render m.export />
		</li>
		<#if m.original??>
			<li>
				<@render m.original />
			</li>
		</#if>
	</ul>
</div>
