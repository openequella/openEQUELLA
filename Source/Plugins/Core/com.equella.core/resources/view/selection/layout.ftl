<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="selection.css" hasRtl=true />

<#assign d = m.decorations />

<#assign TEMP_body>
	<div id="wrapper">
		<div id="selection-header">
			<div id="selection-header-content" <#if m.hideDividers??> class="${m.hideDividers}"</#if>>
				<@render s.navBar />
			</div>
		</div>
	 	<div id="body">
			<div id="body-wrap1">
			<div id="body-wrap2">
			<div id="body-wrap3">
				<div id="body-inner">
					<div id="selection-page" class="selection<#if m.selectItems> selectItems</#if><#if m.selectAttachments> selectAttachments</#if>">

						<@render section=m.sections["helpandoptions"] />
						<@render section=m.sections["servermessage"] />

						<div id="selection-content" class="content">
							<#if m.breadcrumbs?? && (m.breadcrumbs.links?size > 0) >
								<div id="breadcrumbs">
									<span id="breadcrumb-inner">
									<#list m.breadcrumbs.links as bc>
										<@render bc /> ${b.key("breadcrumb.separator")}
									</#list>
									<#if m.breadcrumbs.forcedLastCrumb?? >
										${m.breadcrumbs.forcedLastCrumb}
									<#else>
										<@render section=d.titleAsRenderable />
									</#if>
									</span>
								</div>
							</#if>
							<div id="selection-content-bottom">
								<div id="selection-content-inner" ${d.contentBodyAttributes}>
									<@render m.parts["body"]/>	
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			</div>
			</div>
		</div>
	
		<div id="footer">
		</div>
	</div>
</#assign>
