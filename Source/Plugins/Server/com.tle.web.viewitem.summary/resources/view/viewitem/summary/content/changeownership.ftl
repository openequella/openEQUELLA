<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.key("summary.content.changeownership.pagetitle")}</h2>

<@button section=s.backButton showAs="prev" />

<h3>${b.key("summary.content.changeownership.owner.title")}</h3>
<@a.div id="owner">
	<@render s.ownerTable />
</@a.div>
<br>
<h3>${b.key("summary.content.changeownership.share.title")}</h3>
<@a.div id="collaborators">
	<@render s.collabTable />
</@a.div>