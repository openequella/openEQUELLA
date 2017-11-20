<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<@script "qtiplayviewer.js" />

<@css "qtiplayviewer.css" />
<@css "qtitestsummary.css" />

<div class="qti">
	<@render s.navBar />
	<div class="area">
		<div class="summary-box-container">
		<div class="summary-box">
			<h2>${b.key('coverpage.title')}</h2>
					
			<@settingContainer mandatory=false>
				<#if m.questionCount??>
					<@setting label=b.key('qti.details.questions.count')>
						${m.questionCount}
					</@setting>
				</#if>
				
				<#if m.sectionCount??>
					<@setting label=b.key('qti.details.sections.count') >
						${m.sectionCount}
					</@setting>
				</#if>
			</@settingContainer>

			<div class="button-row">
				<@button section=s.startButton showAs="accept" />
				<@button section=s.cancelButton showAs="cancel"/>
			</div>
		</div>
		</div>
	</div>
</div>	