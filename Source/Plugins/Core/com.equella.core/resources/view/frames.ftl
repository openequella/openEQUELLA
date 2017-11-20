<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "externallink.css"/>
 
<@render s.navBar />
<iframe id="external-content" name="content" src="${m.contentUrl?html}"></iframe>
    
<#assign PART_READY>
$(window).resize(function() {
	$('#external-content').height($(window).height() - $('.navbar').height());
});
$(window).resize();
</#assign>
