# Dev Guide - Standard Components

## The Standard Component Library

The standard component library `com.tle.web.sections.standard` is a library containing standard user interface components such as Text Field, Button, Link, Tab Layouts, Tree, Single and Multi Selection list, and a few others.

There are 3 main types of classes in the library:

- Sections - Button, TextField, Checkbox ...
- Models - HtmlComponentState, HtmlBooleanState, HtmlValueState, HtmlListState ...
- Renderers - ButtonRenderer, TextFieldRenderer, DropDownRenderer, ShuffleBoxRenderer ...

Models and Renderers can actually be used without the need for a Section, and in this fact comes in handy for displaying `TabularData`.

### The Section

The Sections are the "server" side and are responsible for:

- State management / Bookmarking
- Setting up the model with defaults
- Abstracting any Javascript calls, so clients don't need to know which renderer is being used
- Provide an easy interface for common calls to the model

Let's take the `TextField` component as an example.

The `TextField` really only has one piece of state, and that is the actual string that is being edited, so the `TextField` section needs to have a `BookmarkEventListener` which stores the value in the bookmark, and it needs a `ParametersEventListener` which restores the value from a
request.

The other main responsibility of the `TextField` Section is to look after it's Javascript interface. There are various things you might want to do to a
`TextField` on the client side via Javascript, for example, you might want to:

- Get the value in the field
- Set the value in the field
- Disable/Enable the field

Which correspond the following:

```
public JSExpression createGetExpression();
public JSFunction createSetFunction();
public JSFunction createDisableFunction();
```

The trouble here is that the Section doesn't necessarily know how the `TextField` will be rendered, and won't know until it is rendered, but those functions and expressions need to be valid at any time, so it returns some "delayed" interfaces which can handle the fact that it doesn't know the
renderer, and will create "stub" functions if need be.

### The Model

The Model classes are Java Beans which contain all the data that is needed for a renderer to do it's work:

- DOM ID
- Style attributes (class and style)
- Event handlers (a map of JSHandler)
- Component specific data (such as the value to display in the `TextField`)
- Optional Label

There is a bit more to it than that, but those are the main areas.

### Renderers

Renderers are the classes responsible for actually outputting the HTML. Most of the standard renderers print out the standard html input tags.
For example the TextFieldRenderer produces output like:

```
<input type="text" value="text" name="_textField" onclick="...">
```

But a `TextField` renderer could render using a div and span, or anything else it wanted. The only requirements for the renderer are to ensure an appropriate input tags to send the value back for the Section to decode, and it must also support the Javascript interface for the component.

How does a `TextField` decide which renderer it will actually use?

There is a factory class called `RendererFactory` which creates a renderer based on a given `HtmlComponentState` which is the base class for all standard components Model classes. The `HtmlComponentState` class has methods called `getDefaultRenderer()` and `getRendererType()` which are used in conjunction with the Model class itself to produce a key into a map of Component renderers.

The Model has a default renderer type, which might be overridden by the Section, but the `getRendererType()` call is affected by the markup that is rendering the component. For example the standard `TextFieldRenderer` can be chosen via the textfield macro in freemarker:

```
<#include "/com.tle.web.sections.standard@/textfield.ftl">

<@textfield section=s.textField maxlength=80 class="ourTextfield"/>

<@render section=s.textField class="ourTextfield"/>
```

Both tags will render the textfield, but the `<@textfield>` tag has extra attributes that only apply to the input type="text" tag.

The advantage of this approach is that you're deciding what your component looks like in freemarker, but how it interacts with your app is still all in java.
