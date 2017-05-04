<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css 'swagger.css' />
<@css 'swagger-equella.css' />

<@css 'hightlight.default.css' />
<@css 'screen.css' />
<@script path='lib/shred.bundle.js' />
<@script path='lib/jquery.slideto.min.js' />
<@script path='lib/jquery.wiggle.min.js' />
<@script path='lib/jquery.ba-bbq.min.js' />
<@script path='lib/handlebars-1.0.0.js' />
<@script path='lib/underscore-min.js' />
<@script path='lib/backbone-min.js' />
<@script path='lib/swagger.js' />
<@script path='swagger-ui.js' />
<@script path='lib/highlight.7.3.pack.js' />
<script>
$(function()
{
	var url = $("base").attr('href') + 'api/resources';
	var baseurl = $("base").attr('href') + 'apidocs.do';
	$('base').attr('href',baseurl);

	/*	
	window.swaggerUi = new SwaggerUi({
	    url: url,
        dom_id : "resources_container",
        supportHeaderParams : false,
        supportedSubmitMethods : [ 'get', 'post', 'put', 'delete' ],
	});
	window.swaggerUi.load();
		
	*/
	
	window.swaggerUi = new SwaggerUi({
                url: url,
                basePath: url,
                apiKey:"",
                dom_id:"resources_container",
                supportHeaderParams: false,
                supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'head'],
                /*
                onComplete: function(swaggerApi, swaggerUi){
                  $('pre code').each(function(i, e) {hljs.highlightBlock(e)});

                  $("a[href^='#!/']").each(function(i, e) {
                  	e.href = e.href.replace('#!/', 'apidocs.do#!/');
                  });
                  $(".resource .heading h2 a").each(function(i, e) {
                  	$(e).text(e.text.replace(/^\//, ''));
                  });
                  $('.submit').each(function(i, e) {$(e).addClass("btn btn-equella btn-mini")});
                  $('.response_throbber').each(function(i, e) {$(e).attr("src", "${p.url('images/throbber.gif')}")});
                  
                },*/
                onFailure: function(data) {
                	if(console) {
                        console.log("Unable to Load SwaggerUI");
                        console.log(data);
                    }
                },
                docExpansion: "none"
            });

		(function() {
			    var _render = window.swaggerUi.render;     
				window.swaggerUi.render = function() {
					 	var apis = window.swaggerUi.api.apis;
	                    for (var apiKey in apis )
	                    {
	                    	var api = apis[apiKey];
	                    	if (typeof(api.description) !== 'undefined')
	                    	{
		                        api.name = api.description.replace(/ /g, "-");
	                        }
	                        else
	                        {
		                        api.name = apiKey;
	                        }
	                    }
	                    window.swaggerUi.api.apisArray.sort(function(a,b){
	                    					if (a.name < b.name) {
									            return -1;
									        } else if (a.name > b.name) {
									            return 1;
									        } else {
									            return 0;
									        }
	                    				});
       				return _render.apply(this, arguments);
				};
			})();
    	
    	 window.swaggerUi.load();
    
});
</script>

<div class="area">
	<h2>${b.key('docs.title')}</h2>

	<p>${b.key('guide.download', 'https://equella.github.io/')}</p>
	
	<div class="swagger">
		<input id="input_baseUrl" name="baseUrl" type="hidden">
        <div class="swagger-ui-wrap" id='resources_container'><ul id='resources'></ul></div>

	    <div id="message-bar" class="swagger-ui-wrap">
   			&nbsp;
		</div>
	</div>
</div>
