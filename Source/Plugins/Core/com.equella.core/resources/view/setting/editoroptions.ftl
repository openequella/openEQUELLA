<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax>

<@css "editoroptions.css" />

<div class="area editoroptions">
	<h2>${b.key('settings.editoroptions.title')}</h2> 

	<p>${b.key('settings.editoroptions.help')}</p>

	<#if m.error??>
		<div class="settingField">
			<div class="ctrlinvalid ctrlinvalidmessage">
				${m.error}
			</div>
		</div>
	</#if>
	
	<@render section=s.editor class="editor"/>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton showAs="cancel" />
	</div>
</div>