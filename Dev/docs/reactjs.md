# React JS based front ends

Rather than a mix of server side components / JavaScript plumbing / jQuery, the new architecture takes
the much cleaner approach of creating the UI completely in JavaScript based languages which
create [React](https://reactjs.org/) components to interact with the browser DOM.

To catch as many problems as early as possible, two typed languages are used to
compile to JavaScript, rather than raw dynamically typed JavaScript.

- [Typescript](https://www.typescriptlang.org/)
- [Purescript](http://www.purescript.org/) (using Purescript is deprecated in favour of Typescript)

To achieve a modern look and feel based on Google's [Material Design](https://material.io/), a
React component library called [Material UI](https://material-ui.com/) is used.

To produce the smallest JavaScript deployment files, [ParcelJS](https://parceljs.org/) is used
to shrink and bundle the JavaScript to be as concise as possible.

## Code layout

All the JavaScript based code is located in `Source/Plugins/Core/com.equella.core/js/`.
Inside that it contains the following layout:

- `tsrc/` - Typescript source code
- `src/` - Purescript source code
- `target/` - Output files which SBT task `buildJS` will use to copy into openEQUELLA server as web accessible resources
- `package.json` - NPM dependencies + build tasks
- `psc-package.json` - Purescript dependencies for psc-package
- `entrybuild/` - Production entrypoints for parcel to analyze
- `entrydev/` - Development entrypoints for parcel
- `.cache` - Parcel cache folder

Scripts in `package.json` run `parcel` to generate client side resources to be served up from the `com.equella.core` plugin's `resources/web` folder.
The current entry-points are:

- `index.html` - The ["Main UI"](mainui.md) bundle - the application when with the new UI turned on
- `oldsettings.html` - Used for when the settings page is embedded inside the old UI
- `selection.html` - The new selection session page - (currently disabled)

### Development cycle

- First install deps:

```
npm install
```

- Compile just the development bundles (leave it open as it watches for changes and autocompiles):

```
npm run dev
```

- Compile production bundles (minified code, what will be deployed on servers):

```
npm run dev:build
```

Refreshing in the browser after compiling should load the new changes in a running openEQUELLA server.

**TROUBLESHOOTING**

Sometimes NPM doesn't do a great job of keeping the `node_modules/` folder up-to-date after dependency changes. So in the face of strange errors, try cleaning first:

```bash
npm run clean
```

---

## Purescript/Typescript bridge

In order to prevent a cyclic dependency, Typescript must access any Purescript components using a global "bridge" variable.

The bridge's type is declared in [bridge.ts](../../Source/Plugins/Core/com.equella.core/js/tsrc/api/bridge.ts).

## Typescript notes

- Typescript 3.3.3333
- [Axios](https://github.com/axios/axios) is currently the library of choice for a `Promise` based approach to AJAX calls.
- [Redux](https://redux.js.org/introduction) is currently being used to dispatch AJAX calls and do state manipulation.
- [Material UI](https://material-ui.com/) contains Typescript bindings.

Right now none of the pages share any part of the Redux store, but eventually if pages need to share state it will be via the Redux store.

## Purescript notes

- Purescript 0.12.0
- `Affjax` based AJAX
- [purescript-react-mui](https://github.com/doolse/purescript-react-mui) purescript bindings for Material UI.
