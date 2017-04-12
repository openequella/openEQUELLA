<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css "storesettings.css" />

<div class="area">
	<h2>${b.key('settings.page.title')}</h2>
	
	<@settingContainer>
		<@setting section=s.collectionList label=b.key('settings.collection') 
			help=b.key('settings.collection.help') />
			
		<@setting label=b.key('settings.includetax') >
			<@checklist section=s.includeTax list=true class="input radio" />
		</@setting>
	</@settingContainer>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save"/>
	</div>
</div>