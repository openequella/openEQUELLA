<#include "/com.tle.web.freemarker@/macro/sections/render.ftl"/>
<#include "/com.tle.web.freemarker@/macro/sections/text.ftl"/>

<#assign d = m.decorations />

<#assign TEMP_body>
	<div id="wrapper">
		<@render section=m.template["topbar"]/>

	 	<div id="body">
			<div id="body-wrap1">
				<div id="body-wrap2">
					<div id="body-wrap3">
						<div id="body-inner">

							<@render section=m.template["helpandoptions"] />
							<@render section=m.template["servermessage"] />
							<@render section=m.template["upperbody"] />
							<div id="content" class="content">
								<div id="content-bottom">
									<div id="content-inner" class="${d.pageLayoutDisplayClass}">
		
										<#if d.forceBreadcrumbsOn || (m.breadcrumbs?? && m.breadcrumbs.links?size > 0) >
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
		
										<div id="content-body" role="main" ${m.decorations.contentBodyAttributes}>
					 						<@render m.template["body"]/>
										</div>
										<@render m.template["menu"]/>	
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<@render m.template["footer"]/>
	</div>
</#assign>

