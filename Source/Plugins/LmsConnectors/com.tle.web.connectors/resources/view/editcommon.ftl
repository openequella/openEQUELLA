<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "editcommon.css" />

<@setting label=b.key('entity.uuid') >
	<input type="text" readonly="readonly" value="${m.entityUuid?html}" />
</@setting>

<@setting label=b.key('editor.label.title') section=s.title error=m.errors["title"] mandatory=true />

<@setting label=b.key('editor.label.description') section=s.description />

<#if m.customEditor??><@render m.customEditor /></#if>

<#if s.usernameDiv??>
	<@render s.usernameDiv>
		<@setting section=s.modifyUsername label='' />
		
		<@render s.modifyUsernameDiv>
			<@setting section=s.usernameScript label='' type="textarea" help=b.key('editor.label.usernamescript', m.connectorLmsName) rowStyle="usernameScriptRow" />
		</@render>
	</@render>
</#if>

<@render s.exportDiv>
	<@ajax.div id="exportable">
		<@setting label='' section=s.exportSummary />
		
		<@setting label=b.key('editor.label.exportableby') help=b.key('editor.label.exportableby.help') rowStyle="exportableByRow">
			${m.exportableExpressionPretty}
			<@button section=s.exportableSelector.opener showAs="select_user"><@bundlekey "editor.expressionselector.button.change"/></@button>
		</@setting>
	</@ajax.div>
</@render>

<@render s.viewDiv>
	<@ajax.div id="viewable">
		<@setting label=b.key('editor.label.contentviewableby') help=b.key('editor.label.contentviewableby.help')>
			${m.viewableExpressionPretty}
			<@button section=s.viewableSelector.opener showAs="select_user"><@bundlekey "editor.expressionselector.button.change"/></@button>
		</@setting>
	</@ajax.div>
</@render>