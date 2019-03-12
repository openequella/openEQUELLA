# React JS based front ends

Rather than a mix of server side components / JavaScript plumbing / jQuery, the new architecture takes
the much cleaner approach of creating the UI completely in JavaScript based languages which
create [React](https://reactjs.org/) components to interact with the browser DOM.

To catch as many problems as early as possible, two typed languages are used to
compile to JavaScript, rather than raw dynamically typed JavaScript.

- [Purescript](http://www.purescript.org/)
- [Typescript](https://www.typescriptlang.org/)

Interaction between components written in either of the two languages is achieved by
using a compatible subset (plain functions and plain records/objects).

To achieve a modern look and feel based on Google's [Material Design](https://material.io/), a
React component library called [Material UI](https://material-ui.com/) is used.

To produce the smallest JavaScript deployment files, [ParcelJS](https://parceljs.org/) is used
to shrink and bundle the JavaScript to be as concise as possible.

## Code layout

All the JavaScript based code is located in `Source/Plugins/Core/com.equella.core/js/`.
Inside that it contains the following layout:

- `src/` - Purescript source code
- `tsrc/` - Typescript source code
- `target/` - Output files which SBT task `buildJS` will use to copy into openEQUELLA server as web accessible resources
- `package-json` - NPM/Yarn dependencies + build tasks
- `psc-package.json` - Purescript dependencies for psc-package
- `.cache` - Parcel cache folder

Currently there are two JS bundles created by the NPM tasks, one for the "Single page app"
and the other is for the file upload control.

## Main application bundle

All the standard The standard page app is a bundle which is served from a [servlet](../../Source/Plugins/Core/com.equella.core/scalasrc/com/tle/web/template/SinglePageApp.scala) in openEQUELLA registered at `"/page/*"`.

Based on the given path, a [purescript-routing](https://github.com/slamdata/purescript-routing)
based router selects a root React component.

- Single page app entry point - [Main.purs](../../Source/Plugins/Core/com.equella.core/js/src/MainUI/Main.purs)
- Router - [Routes.purs](../../Source/Plugins/Core/com.equella.core/js/src/MainUI/Routes.purs)

### Development cycle

- First install deps:

```
yarn install
```

- Compile just TypeScript and JavaScript:

```
yarn run dev
```

- Compile just PureScript:

```
pulp build
```

- Compile all at once:

```
yarn run dev:build
```

Refreshing in the browser after compiling should load the new changes in a running openEQUELLA server.

---

## Purescript/Typescript bridge

In order to prevent a cyclic dependency, Typescript page components are passed a property
called "bridge" which contains an interface to any Purescript components such as the `Template` and access to
functions for creating `Route`s and turning them into urls and click handlers.

The bridge's type is declared in [bridge.ts](../../Source/Plugins/Core/com.equella.core/js/tsrc/api/bridge.ts).

## Template component

Usually a page within the Single Page app will make use of the `Template` react component which is responsible for
rendering the layout:

- Responsive menu
- App bar with title and user links menu
- Optional extra markup in title (search bar)
- Additional area for tabs
- Error notifications

See `TemplateProps` in [Template.purs](../../Source/Plugins/Core/com.equella.core/js/src/MainUI/Template.purs) or [Template.ts](../../Source/Plugins/Core/com.equella.core/js/tsrc/api/Template.ts)

## Typescript notes

- Typescript 3.0.3
- [Axios](https://github.com/axios/axios) is currently the library of choice for a `Promise` based approach to AJAX calls.
- [Redux](https://redux.js.org/introduction) is currently being used to dispatch AJAX calls and do state manipulation.
- [Material UI](https://material-ui.com/) contains Typescript bindings.

Right now none of the pages share any part of the Redux store, but eventually if pages need to share state it will be via the Redux store.

## Purescript notes

- Purescript 0.12.0
- `Affjax` based AJAX
- [purescript-react-mui](https://github.com/doolse/purescript-react-mui) purescript bindings for Material UI.

## Typescript HelloWorld

Create a Typescript React component which takes the purescript bridge as a property:

```typescript
import { Bridge } from "./api/bridge";
import * as React from "react";
import { Typography } from "@material-ui/core";

interface HelloWorldProps {
  bridge: Bridge;
}

class HelloWorld extends React.Component<HelloWorldProps> {
  render() {
    const { Template } = this.props.bridge;
    return (
      <Template title="Hello">
        <Typography>Hello</Typography>
      </Template>
    );
  }
}

export default HelloWorld;
```

Add a purescript FFI function to TSComponents.purs and .js for accessing the class.

**TSComponents.purs**

```purescript
foreign import helloWorldClass :: forall a. ReactClass a
```

**TSComponents.js**

```JavaScript
exports.helloWorldClass = require("HelloWorld").default;
```

Add a route for the page (in `Routes.purs`)

```purescript
data Route = SearchPage |
    ... |
    HelloWorldPage
```

Add a matcher for the page url:

```purescript
routeMatch :: Match Route
routeMatch =
    SearchPage <$ (lit "search") <|>
    ... <|>
    HelloWorldPage <$ (lit "hello")
```

Add a conversion from Route to URI path in `routeURI`:

```purescript
routeURI :: Route -> String
routeURI r = "/" <> ( case r of
    SearchPage -> "search"
    ...
    HelloWorldPage -> "hello"
```

Make the entry point render the component when it's Route is selected (in `Main.purs`):

```purescript
import TSComponents (helloWorldClass)
...

    render {route:Just r} = case r of
            SearchPage -> searchPage
            ...
            HelloWorldPage -> unsafeCreateLeafElement helloWorldClass {bridge:tsBridge}
```

Now if you build the bundle (see development cycle above) you should be able to go to `http://<insturl>/pages/hello` and see your page.
