# Dev Guide - AJAX

Sections contains API's for easily and mostly transparently adding of AJAX functionality to your pages. Using the ```AjaxGenerator``` interface allows you to create ```JSCallable``` objects which allow you to do a number of things:
* Update a DOM node dynamically with support for different update effects, such as fade in/out, spinners etc..
* Return an arbitrary JSON object from the server
* A mixture of both, DOM updates and a JSON object

## Setup
In order to use the sections AJAX features you need to mark a field in your section with a special annotation:
```
@AjaxFactory
private AjaxGenerator ajax;
```

## Updating the DOM dynamically
Let's say you have a page which displays a times table for a number which you enter into a textfield and does this via a form submit.
The interaction with the server comes with the "click" handler of the submit button.  For example:

```
public void registered(String id, SectionTree tree)
{
submit.addClickHandler(events.getNamedHandler("clicked"));
super.registered(id, tree);
}
```

and it renders the table in FreeMarker like this:
```
<div id="timesDiv">
<#if m.showTable>
<#list 1..12 as times>
${times_index+1} * ${m.number?c} = ${(times*m.number)?c}<br>
</#list>
</#if>
</div>
```

It all works nicely, but you really need to make it Web 2.0 because it's the _in_ thing to do. Luckily that's pretty easy to do using the ```AjaxGenerator```.

You simply need to create a ```JSHandler``` which calls an ajax update DOM function.
```
public void registered(String id, SectionTree tree)
{
submit.addClickHandler(new SimpleJSHandler(ajax.getAjaxUpdateDomFunction(tree, id,
"timesDiv", events.getEventHandler("clicked"))));
super.registered(id, tree);
}
```

In order for this to work you need to use a special AJAX enabled ```<@div>``` macro in your FreeMarker code so that the HTML for that div can be captured:
```
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<@div id="timesDiv">
<#if m.showTable>
<#list 1..12 as times>
${times_index+1} * ${m.number?c} = ${(times*m.number)?c}<br>
</#list>
</#if>
</@div>
```

## JSON responses
You can create AJAX functions which can return an arbitrary JSON object.


## AJAX and scripting resources
The AJAX rendering context only tries to capture the web resources (CSS, JS files and inline JS) required by your AJAX update and includes those resources in the page if they aren't already.  In order for this to work properly, the resources you need must be (pre)rendered inside the AJAX div you are updating, otherwise it will not be included as part of the response.

Let's look at the example of doing a "slideDown" animation on a button.

Here's how _not_ to do it (getters removed for brevity):
```
public SectionResult renderHtml(RenderEventContext context)
{
JQueryCore.appendReady(context, new JQueryStatement(Type.ID, "animateMe", new
ScriptExpression("slideDown()"));
return viewFactory.createResult("slide.ftl", context);
}
```

The FreeMarker:
```
<@div id="ajaxDiv">
<@render section=s.button id="animateMe">${m.dynamicText?html}</@render>
</@div>
```

This will work fine when not used with AJAX, but if you try and update the DOM with AJAX, it won't do the slide animation as there is no way for the AJAX rendering context to know that the animation statement is meant to run when that button is updated, as well as on the DOM ready event. So how do we fix it?
```
public void registered(String id, SectionTree tree)
{
super.register(id, tree);
button.addReadyStatements(new JQueryStatement(Type.ID, "animateMe", new
ScriptExpression("slideDown()"));
}

public SectionResult renderHtml(RenderEventContext context)
{
return viewFactory.createResult("slide.ftl", context);
}
```

And the freemarker remains the same.

```
addReadyStatemements(statements) is equivalent to addEventHandler(JSHandler.EVENT_READY, new
StatementHandler(statement));
```

