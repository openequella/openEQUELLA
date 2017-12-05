# Dev Guide - Events

## Handle user actions
Handling of user interface actions, such as clicking a button, changing a drop down. This usually implemented by hardcoding snippets of
javascript into the markup, relying on certain functions being present and setting some hidden input tags, which had to be placed in the markup manually. Here's an example taken straight from EQUELLA 3.2's Comment.jsp
```
<input type="hidden" name="<%=nest>.method">
<input type="hidden" name="<%=nest>.uuid">
<input type="button" value="Delete" onclick='<%="deleteComment(\\\'"+commentID+"\\\');
return false;"%>' />
```

and
```
function deleteComment(id)
{
var form = getForm();
form.elements['<%=nest%>.method'].value="delete";
form.elements['<%=nest%>.uuid'].value = id;
form.submit();
return false;
}
```
YUCK!

In case you think this sort of thing is ok, here are the reasons it is not:
* You're polluting your view markup template with javascript event handlers, when really all you want to do is have some markup which
says this is where my Delete button should show.
* There is no global validation being done. Because the writer of this code didn't know or forgot to include a call to our hardcoded ```submitit()``` function, it won't do any validation or run any code that needs to run on submission, such as selecting the right side of a shuffle box.
* What if for some reason we had to change the name of ```getForm()```, for example to make it shorter, we can't do it without breaking all this
code
* Because you're modifying hidden input tags, if the user clicks the back button, those values will still be in the form, and if the form gets re-submitted, you can potentially run the same code again, which is likely to cause a stack trace if it is now an invalid request. 
* Your event handling code is only going to get called when your Section is asked to be rendered. Sections pre 4.0 is based on the Struts ```execute()``` way of handling requests, so if for some reason your parent Section decided you didn't need to be rendered, you'd never get to respond to your event. This is more of a problem for the parent Section to deal with but still.

## The 4.0 (and later) way
One of the major difference between 3.2 and 4.0 Sections is the whole "event" based nature of handling a request.

3.2 has 2 distinct phases
1. Process request parameters by calling ```processRequest()``` on the root section, which calls ```BeanUtils.populate()``` on it's model,
and passes the request onto it's children.
2. ```execute()``` the root section, which is responsible for ```execute()```ing all it's children, the results of ```execute()``` are what is to be rendered.

4.0 also has 2 phases
1. Event processing. The initial event is a request parameters event.
2. Rendering. If you get to this phase, you know you are actually rendering to the browser, you never respond to, for example, a user clicking a delete button at this point.

Because of the [PRG](http://en.wikipedia.org/wiki/Post/Redirect/Get) design pattern, usually the rendering phase will only ever happen on the GET request, and you will only ever respond to user interface events during the POST request. There are special cases where this won't be strictly
true, but for the most part, POST is for event handling, GET is for rendering.

## Javascript event generation
4.0 has a new Javascript abstraction layer, which is basically a set of Java interfaces which represent the primitive parts of the Javascript language, such as Functions, Expressions, Statements, Function calls, Literals etc.

It is using this layer which the new event handling mechanisms of Sections are build upon, so rather than writing chunks of javascript into your markup, you get Sections to do it for you, so you can concentrate on the stuff the really matters (I want this piece of code to execute when this button is clicked).

There are a lot of new Web Frameworks around which try and make web programming more like standard desktop programming, such as Wicketand Tapestry. They go out of their way to try and hide things like URLs and Javascript. Which is all very well in a perfect world, but sometimes it's handy to know what you're doing is actually creating javascript functions and generating real URLs.

So a conscience decision was made to not hide the fact that you really are just creating javascript functions and snippets, but instead just to abstract it away to a point where you don't have to know the intimate details of what is being produced, but if you do need to know (for whatever reason... ), you can find out.

It also helps with interfacing to the wide range of Javascript libraries that are around, in particular the seemingly never ending list of JQuery plugins.

## How do we do it?
Let's say we wanted to get some javascript that when triggered, calls a method on the server.
```
public class MySection extends AbstractPrototypeSection<Object>
{
@EventFactory
private EventGenerator events;
public void registered(String id, SectionTree tree)
{
super.registered(id, tree);
JSHandler handler = events.getNamedHandler("buttonClick");
}
@EventHandlerMethod
public void buttonClick(SectionInfo info)
{
System.err.println("Button was clicked");
}
}
```

So the ```events.getNamedHandler(String)``` call returns a ```JSHandler``` object, which is a Java abstraction of an event handler javascript
statement, which can be placed directly onto a html tag as one of the usual event handler attributes (onclick, onchange etc..).

So now you have a ```JSHandler``` which can spit out javascript which does global validation, sets up the event (with optional parameters) and then submits the form back to the server, but we need to bind that bit of javascript to something, like a button for example.
```
public class MySection extends AbstractPrototypeSection<Object> implements
HtmlRenderer
{
@EventFactory
private EventGenerator events;
@Component
private Button clickMe;
public void registered(String id, SectionTree tree)
{
super.registered(id, tree);
JSHandler handler = events.getNamedHandler("buttonClick");
clickMe.addClickHandler(handler);
}
@EventHandlerMethod
public void buttonClick(SectionInfo info)
{
System.err.println("Button was clicked");
}
public SectionResult renderHtml(SectionContext context, RenderEvent event) throws
Exception
{
return renderSection(context.getInfo(), clickMe);
}
}

```

Note: you usually would create a markup file, for example a freemarker template, rather than rendering the button this way.

Thankfully all the components in the "standard" library know exactly how to deal with ```JSHandler```s and will print out the javascript event handler attribute just as you would expect.

We could go into what the actual javascript produced is, but you shouldn't have to care. All that is important is that your java method gets called when you click the button.
