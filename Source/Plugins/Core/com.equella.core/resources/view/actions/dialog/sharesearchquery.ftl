<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="dialog/sharesearchquery.css" hasRtl=true />

<div class="sharesearchquery">
	<h3>${b.key("actions.share.dialog.feeds")}</h3>
	<div>
		<@render section=s.rssLink class="feedlink focus" >${b.key("actions.share.dialog.feeds.rsstype")}</@render> <br>
		<@render section=s.atomLink class="feedlink" >${b.key("actions.share.dialog.feeds.atomtype")}</@render>
	</div>
	<hr>
	<h3>${b.key("actions.share.dialog.url.desc")}</h3>
	<label for="${s.url}">
		<p>${b.key("actions.share.dialog.url.prompt")}</p>
	</label>
	<p>
		<@textfield section=s.url size=75 class="sharefield" />
	</p>
	<hr>
		
	<#if m.showEmail >
		<@div id="show-email">
	<#if m.emailMessage??>
	<div class="alert-success">
		<p class="message">
			${m.emailMessage}
		</p>
	</div>	
	</#if>
	
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

	<div class="input checkbox">
		<@render section=s.guest />
	</div>
	</@div>
	<hr>
	</#if>
</div>