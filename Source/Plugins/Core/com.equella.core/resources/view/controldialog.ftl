<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "controldialog.css" />

<div class="area">
	<div id="wizard-controls" class="wizard-controls">
		<#list m.renderedControls as control>
			<@render control/> 
		</#list>
	</div>
</div>