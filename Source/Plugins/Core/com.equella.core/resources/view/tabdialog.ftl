<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div class="controlcontainer">
	<@settingContainer mandatory=false skinny=true>
		<@setting label=b.key('tabs.attachment') section=s.tabAttachmentList />			
		<@setting label=b.key('tabs.name') section=s.popupTabName />
		<@setting label=b.key('tabs.viewer') section=s.tabViewerList />
	</@settingContainer>
</div>