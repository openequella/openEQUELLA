<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css "qtiplayviewer.css" />
<@script "mathjax/MathJax.js?config=MML_HTMLorMML" />

<@render s.navBar />
<div class="qti">	
	<div class="area">
		<div class="float-left col">
			<div class="test-summary">
				<div class="button-row">
					<@button section=s.submitButton showAs="blue" />
					<@button section=s.viewResultButton showAs="blue" />
				</div>			
			</div>
			
			<hr>
			
			<@a.div id="test-questions-container" class="test-questions-container row scroll-y">
					<#list m.sections as section>
						<h3>${section.title}</h3>
						<ul class="questions">
				    		<#list section.questions as q>
					    		<li class="${q.styleClass}">
					    			<div class="indicator"></div>
					    			<@render q.link />
					    		</li>
					    	</#list>
						</ul>
					</#list>
			</@a.div>		
		</div>
	
		<div class="float-right col">
			<@a.div id="question-header" class="question-header">
				<div id="question-title" class="question-title">
					<#if m.questionTitle??>
						<h2>${m.questionTitle}</h2>
					</#if>
				</div>
				<#if !m.hideNavigationButtons>
					<div class="btn-group button-row buttons-prevnext">			
						<@button section=s.previousButton showAs="prev" />
						<@button section=s.nextButton showAs="next"/>
					</div>
				</#if>
			</@a.div>
			
			<hr>
			
			<@a.div id="question-body-container" class="question-body-container row scroll-y">
					<#if m.questionRenderable??>
						<@render m.questionRenderable />
					</#if>
			</@a.div>
		</div>
	</div>
</div>