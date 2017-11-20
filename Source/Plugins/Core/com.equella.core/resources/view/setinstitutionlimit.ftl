<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css "contentrestrictions.css" />

<div class="area">
	<h2>${b.key('institutionlimit.heading')}</h2>
	<div class="input checkbox enablelimit">
		<@settingContainer mandatory=false>
			<@a.div id="enableajaxdiv" class="enablePanel">
				<@render section=s.enableLimit />
				<#--if m.showControls-->
					<@setting label=b.key('institutionlimit.enter') section=s.specifiedLimit error=m.errors['limitformat'] help=b.key('institutionlimit.specify') />
			</@>
			<@a.div id="updatestatus" showEffect="slide">
				<#if m.errors['updateSuccess']?? >
					<div class="success">
						${m.errors['updateSuccess']}
					</div>
				</#if>
			</@>
		</@settingContainer>
		<div class="button-strip">
			<@button section=s.saveButton showAs="save" />
			<@button section=s.cancelButton />
		</div>
	</div>
</div>
