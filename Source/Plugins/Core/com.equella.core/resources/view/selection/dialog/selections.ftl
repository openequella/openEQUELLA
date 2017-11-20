<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/table.ftl" />

<@css path="versiondialog.css" />
<@css plugin="com.tle.web.sections.equella" path="selectionreview.css"/>

<h3>${m.courseTitle}</h3>

<@ajax.div id="table-div">
	<@render s.versionSelectionSection />
</@ajax.div>