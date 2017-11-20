<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="toolbar.css" hasRtl=true />

<div class="area">
	<h2>${b.key('settings.toolbar.title')}</h2> 
	
	<p>${b.key('settings.toolbar.help')}<p>
	
	<@render s.containerDiv>
	
		<h4>${b.key('settings.toolbar.current')}</h4> 
	
		<#list m.currentRows as row>
			<@render row />
		</#list>
		
		<div class="toolbaractions">
			<@button section=s.resetButton class="toolbaractionbutton" />
			<@button section=s.clearButton showAs="delete" class="toolbaractionbutton" />
		</div>
	
	
		<h4>${b.key('settings.toolbar.available')}</h4> 
		
		<@render m.availableRow />
		
	</@render>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton showAs="cancel" />
	</div>
</div>