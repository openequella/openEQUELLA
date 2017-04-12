<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css "storesettings.css" />

<div class="area">
	<h2>${b.key('store.settings.page.title')}</h2>
	
	<@settingContainer mandatory=false wide=true>
			<@setting label=b.key('store.settings.label.store.allow') section=s.allowStore help=b.key('store.help.enable') />
	</@settingContainer>
	
	<@ajax.div id="overallajaxdiv" class="overallPanel">
		<@settingContainer mandatory=true wide=true>
			
			<#if m.showControls>
				<div>
					<@setting label=b.key('store.settings.label.store.name') section=s.storeName mandatory=true error=m.errors["nameErrors"] />
					<@setting label=b.key('store.settings.label.store.description') mandatory=true error=m.errors["descriptionErrors"] help=b.key('store.help.description') >
						 <@textarea section=s.storeDescription cols=82 rows=6/>
					</@setting>
				</div>
				<div>
					<@setting label=b.key("store.settings.upload.label.small") error=m.errors["uploadsmall"] help=b.key('store.help.icon.small') >
						<@file section=s.smallFile/>
						<@button section=s.smallUploadButton icon="upload" class="marginbutton" />
					</@setting>
					<#if m.smallFilename??>
					<div id="current-icon">
						<@setting label=b.key("store.settings.upload.filenameicon")>
							<@render m.smallImage /> <@button section=s.smallDeleteIconButton showAs="delete" /> 
						</@setting>
					</div>
					</#if>
				</div>
				<div>
					<@setting label=b.key("store.settings.upload.label.large") error=m.errors["uploadlarge"] help=b.key('store.help.icon.large') >
						<@file section=s.largeFile/>
						<@button section=s.largeUploadButton icon="upload" class="marginbutton" />
					</@setting>
					<#if m.largeFilename??>
					<div id="current-image">	
						<@setting label=b.key("store.settings.upload.filenameimage")>
							<@render m.largeImage /><br/><@button section=s.largeDeleteIconButton showAs="delete" class="marginbutton" /> 
						</@setting>
					</div>
					</#if>
				</div>
				<div>
					<h2>${b.key('store.settings.page.contact.heading')}</h2>
					<@setting label=b.key('store.settings.page.contact.name') section=s.contactName mandatory=true error=m.errors["contactNameErrors"] />
					<@setting label=b.key('store.settings.page.contact.number') section=s.contactNumber mandatory=true error=m.errors["contactNumberErrors"] />
					<@setting label=b.key('store.settings.page.contact.email') section=s.contactEmail mandatory=true error=m.errors["contactEmailErrors"] help=b.key('store.help.contact') />
				</div>
			</#if>
		</@settingContainer>
		<div class="button-strip">
			<@button section=s.saveButton showAs="save"/>
		</div>
	</@ajax.div>
</div>