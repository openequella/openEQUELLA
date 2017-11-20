<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />
<#import "/com.tle.web.sections.standard@/list.ftl" as l />


<@css path="flickr.css" hasRtl=true/>
<#-- dodgy.  should be using flickrlistsection which would do this, but there were too
many refactors for 5.1 GA -->
<@css path="itemlist.css" plugin="com.tle.web.itemlist" />

<p>${b.key('label.disclaimer')}</p>

<div id="outer-div">
		<@render s.cssInclude/>
	
		<@a.div id="searchresults-header-cont">
			<div class="searchresults-header">
				<#if m.resultsTitle??>
					<h2>${m.resultsTitle}</h2>
				</#if>
				<#if m.resultsAvailable>
					<div id="searchresults-stats">${m.resultsText}</div>
				</#if>
			</div>		
		</@a.div>		
		
		<@render m.actions/>
		
		<@a.div id="searchresults-cont">
		
			<@a.div id="searchresults" class="searchresults area">
				
				<#if m.resultsAvailable>
					
					<#if s.results??>	
						<div class="itemlist">
						<@l.boollist section=s.results; opt, state>
							<div class="itemresult-wrapper">
								<div class="itemresult-container flickrresult">
									<div class="itemresult">
									
										<div class="select">
											<@render state />
										</div>
									
										<div class="itemresult-content">
											<#if opt.thumbnail??>
												<div class="thumbnailinlist">
													<@render opt.thumbnail />
												</div>
											</#if>
					
											<#-- some descriptions have embedded html, as do some author strings, so use ?html to escape it -->
											<label for="${state.id}">
												<h3><@render opt.link /></h3>
												<#if opt.description??>
													<p>${opt.description?html}</p>
												</#if>
											</label>
											<div class="itemresult-meta">
					
												<#if opt.photoSize??>
													<div class="itemresult-metaline">
														<strong>${b.key('flickr.details.imagesize')}</strong>${opt.photoSize}
													</div>
												</#if>
					
												<#if opt.dateTaken??>
													<div class="itemresult-metaline">
														<strong>${b.key('flickr.details.taken')}</strong>${opt.dateTaken}
													</div>
												</#if>
					
												<#if opt.license??>
													<div class="itemresult-metaline">
														<strong>${b.key('flickr.details.license')}</strong>${opt.license}
													</div>
												</#if>
											</div>
					
											<div class="clear"></div>
										</div>
									</div>
			
									<div class="itemresult-rating">
										<div class="rating-bar">
											<span>
												<#if opt.author??>
													<@bundlekey value="add.author" params=[opt.author?html] /> |
												</#if>
												<@render opt.datePosted />
												<#if opt.views?? && opt.views gt 0> |
													<strong><@bundlekey value="add.views" params=[opt.views] /></strong>
												</#if>
											</span>
										</div>
									</div>
								</div>
							</div>
						</@l.boollist>
						</div>
						
					</#if>
					<@render s.paging.pager/>
				<#--  !m.resultsAvailable -->
				<#else>
							
					<#if m.errored>
						<h3>${m.errorTitle}</h3>
						<#if m.errorMessageLabels??>
							<p>
								<#list m.errorMessageLabels as errorLabel>
									${errorLabel}<br>
								</#list>
							</p>
						</#if>
					<#elseif m.noResultsTitle??>
						<h3>${m.noResultsTitle}</h3>
					</#if>
					
					<#if m.suggestions??>${m.suggestions}</#if>
					
				</#if>
			</@a.div>
		</@a.div>
</div>

