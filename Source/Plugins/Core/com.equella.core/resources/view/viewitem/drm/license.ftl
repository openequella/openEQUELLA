<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<div class="area">
<h2>${b.key("summary.sidebar.summary.termsofuse.title")}</h2>
	<#assign drm=m.drm>
	<#include "/com.tle.web.viewitem.summary@/viewitem/drm/terms.ftl" />

<div class="button-strip">
	<@button section=s.acceptButton showAs="accept" >${b.gkey("viewitem.section.licenceagreement.accept")}</@button>
	<@button section=s.rejectButton showAs="reject" >${b.gkey("viewitem.section.licenceagreement.reject")}</@button>
	<#if m.canpreview>
		<@button section=s.previewButton>${b.gkey("viewitem.section.licenceagreement.preview")}</@button>
	</#if>
</div>
</div>
