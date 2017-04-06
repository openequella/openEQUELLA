<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "metsexport.css" />

<@div id="metsExport">
	<h3><@bundlekey "export.record"/></h3>
	<ul>
		<li>
			<div class="input checkbox"><@render s.attachments /></div>
		</li>
		<li>
			<@render m.export />
		</li>
	</ul>
</@div>