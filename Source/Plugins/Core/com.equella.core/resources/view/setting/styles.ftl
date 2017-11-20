<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax>

<@css "editstyles.css" />

<div class="area">
	<h2>${b.key('settings.styles.title')}</h2> 

	<p>${b.key('settings.styles.help')}</p>
	
	<@render section=s.editor class="styleseditor"/>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton showAs="cancel" />
	</div>
</div>