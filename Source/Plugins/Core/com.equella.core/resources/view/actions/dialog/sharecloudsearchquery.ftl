<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css path="dialog/sharecloudsearchquery.css" hasRtl=true />

<div class="sharecloudsearchquery input">
	<h3>${b.key("actions.share.dialog.url.desc")}</h3>
	<label for="${s.url}">
		<p>${b.key("actions.share.dialog.url.prompt")}</p>
	</label>
	<p>
		<@textfield section=s.url size=75 class="sharefield" />
	</p>
	<hr>
	
	<#if m.showEmail >
	<h3>${b.key("actions.share.dialog.email.desc")}</h3>
	<label for="${s.email}">
		<p>${b.key("actions.share.dialog.email.prompt")} 
			<#if m.emailProblem??>
				<span class="error">
					${m.emailProblem}
				</span>
			</#if>
		</p>
	</label>
	<div class="input-append">
		<@textfield section=s.email size=50 class="sharefield email" />
		<@button section=s.sendEmailButton showAs="email" iconOnly=true />
	</div>
	<hr>
	</#if>
</div>