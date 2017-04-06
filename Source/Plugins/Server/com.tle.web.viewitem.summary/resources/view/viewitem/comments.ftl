<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textarea.ftl" />
<#include "/com.tle.web.sections.standard@/star-rating-static.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="comments.css" hasRtl=true />

<@div id="comments-list">
	<#if m.canView && m.hasComments>
		<div id="comments-list-head">
			<#assign numComments=m.comments?size />
			<a name="comments"></a>
			<h3>${b.key("comments.existing." + (numComments == 1)?string("singular", "plural"), numComments)}</h3>
		</div>
		
		<#list m.comments as comment>

			<div id="comment${comment.id?c}" class="comment">
				<#if comment.deleteButton??>
					<div class="comment-delete">
						<@render section=comment.deleteButton><img src=${p.url("images/remove.png")} alt="<@bundlekey value="comments.delete" />" /></@render>
					</div>
				</#if>
				<div class="comment-username"><span>${comment.username}</span> <@render class="comment-date" section=comment.dateCreatedRenderable /></div>

				<#if comment.comment??>
					<div class="comment-content">
						<p>${comment.comment?html?replace("\n", "<br>")}</p>
					</div>
				</#if>
				<div class="ratingwrapper">
				<#if comment.hasRating()>
					<@starrating rating="${comment.rating}" classes="rating" />
				</#if>
				</div>
			</div>
		</#list>
	</#if>
</@div>

<#if m.canAdd>
	<label for="${s.textArea}"><h3><#if m.sectionTitle??>${m.sectionTitle}</#if></h3></label>
	<@div id="comments-add" class="rating-form comment-form">
		<div class="rate">
			<label for="${s.addRating}">
				${b.key("comments.rate")}
			</label>
			<div class="rate-stars">
				<@render section=s.addRating type="dropdown_star_rating" />
			</div>
		</div>
		<div class="input textarea">
			<@textarea section=s.textArea cols=82 rows=6/>
		</div>
		<div class="input checkbox float-left">
			<#if s.allowAnonymous>
				<@render s.anonymous />
			</#if>
		</div>
		<@button section=s.addComment class="float-right addComment" showAs="add" />
	</@div>
	<div class="clear"></div>
</#if>
