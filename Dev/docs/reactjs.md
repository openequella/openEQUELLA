# React JS based front ends

Rather than a mix of server side components / JavaScript plumbing / jQuery, the new architecture takes
the much cleaner approach of creating the UI completely in
[TypeScript](https://www.typescriptlang.org/), which creates
[React](https://reactjs.org/) components to interact with the browser DOM.

To achieve a modern look and feel based on Google's [Material Design](https://material.io/), a
React component library called [Material UI](https://mui.com/) is used.

[Vite](https://vite.dev/) bundles the TypeScript into the JavaScript that is served to browsers.

## Code layout

All the front end code lives in `react-front-end/`, which contains:

- `tsrc/` - TypeScript source code
- `entrypoint/` - The HTML and TypeScript entry points Vite builds (see below)
- `__tests__/` - Jest tests
- `__stories__/` - Storybook stories
- `build-tools/` - Supporting build scripts, e.g. the language bundle generator
- `target/` - Build output, which the SBT task `buildReactFrontEnd` copies into the
  openEQUELLA server as web accessible resources
- `vite.config.mts` - Bundler configuration; the comments there explain the less obvious parts
- `package.json` - NPM dependencies + build tasks

## Entry points

`entrypoint/` holds three HTML files that Vite builds as separate entry points:

- `index.html` - The ["Main UI"](mainui.md) bundle, used when the new UI is turned on
- `AdvancedSearchPage.html` - The advanced search page
- `Settings.html` - Used when the settings page is embedded inside the old UI

`entrypoint/public/` holds page fragments with no `<script src>` of their own. They render inside a
page that has already loaded the main bundle (e.g. within a Selection Session) and call
`window.oEQRender` to mount, so Vite copies them across unbundled.

`entrypoint/scripts/uploadlist.js` is built separately, as a self-contained classic script, because
the wizard's Attachments control injects it with a plain `<script src>` tag.

`RenderNewTemplate.scala` loads each of these by filename and rewrites the asset URLs inside them,
which is why their names have to survive the build unchanged.

## Development cycle

Install dependencies:

```bash
npm ci
```

Build the development bundles, watching for changes and rebuilding (leave it running):

```bash
npm run dev
```

Refreshing the browser after a rebuild loads the new changes into a running openEQUELLA server -
in dev mode the server re-reads the built HTML on every request rather than caching it.

Build the production bundles (minified, what gets deployed):

```bash
npm run build
```

Run the tests, or launch Storybook:

```bash
npm run test
npm run storybook
```

**TROUBLESHOOTING**

Sometimes NPM doesn't do a great job of keeping the `node_modules/` folder up-to-date after
dependency changes. So in the face of strange errors, try cleaning first:

```bash
npm run clean
```
