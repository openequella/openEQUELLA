<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="htree.css" hasRtl=true/>

<h2><@bundlekey "aftercontribution.title" /></h2>

<p><@bundlekey "aftercontribution.msg" /></p>
<div class="topic-tree-container">
	<@render section=s.topicTree class="topic-tree treeview-gray" />
</div>
<div class="button-strip">
<@button section=s.addButton showAs="save" size="medium" />
</div>