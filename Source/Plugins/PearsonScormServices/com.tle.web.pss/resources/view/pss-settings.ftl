<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" >
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area psssettings">
	<h2>${b.key('settings.title')}</h2>
	<p>${b.key('settings.description')}</p>
	<@setting label=b.key('editor.enablelabel') section=s.enable />
		
	<@a.div id="overallajaxdiv">
		<@a.div id="controls">	
			<#if m.showControls>
				<@setting label=b.key("editor.baseurl") section=s.baseUrl mandatory=true error=m.errors["baseurl"] />
				<@setting label=b.key("editor.consumerkey") section=s.consumerKey mandatory=true error=m.errors["key"] />
				<@setting label=b.key("editor.consumersecret") section=s.consumerSecret mandatory=true error=m.errors["secret"] />
				<@setting label=b.key("editor.accountnamespace") section=s.accountNamespace mandatory=true error=m.errors["namespace"] help=b.key("editor.namespace.help") />
			</#if>
		</@a.div>
		<#if m.showControls>
		<hr>
			<div id="testconnection">
				<@setting label=b.key('editor.label.test.status') >
					<@a.div id="connectionstatus" showEffect="slide">
						<#if !m.successful && m.errors['connectiontest']?? >
							<div class="ctrlinvalid">
								<span>${m.errors['connectiontest']}</span>
							</div>
						<#elseif m.successful >
							<div class="success">
								<span>${b.key('editor.test.success')}</span>
							</div>
						<#else>
							<div class="waiting">
								${b.key('editor.test.prompt')}
							</div>
						</#if>
					</@a.div>
				</@setting>
				<@a.div id="testbutton" >
					<@setting label="" error=m.errors['nottested'] >
						<@button section=s.testButton showAs="verify" />
					</@setting>
				</@a.div>
			</div>
		</#if>
	</@a.div>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>
