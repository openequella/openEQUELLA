# Patches

This directory contains patches applied to `node_modules` dependencies using `patch-package`. These patches are automatically applied after every `npm install` thanks to the `postinstall` script in `package.json`.

## `jsdom+26.1.0.patch`

### Purpose

This patch modifies `jsdom` to allow the `window.location` property to be configurable. This is essential for our unit and integration tests where we need to mock parts of the `location` object, such as `window.location.href` or `window.location.search`.

### The Problem

By default, `jsdom` defines `window.location` with `configurable: false`. This prevents test frameworks like Jest from modifying it, leading to errors such as `TypeError: Cannot redefine property: location`.

See this related [jsdom issue](https://github.com/jsdom/jsdom/issues/3492) for more context.

### The Solution

This patch changes the property descriptor for `window.location` in the `jsdom` source code to set `configurable: true`.

### For Future Developers

If you upgrade `jsdom` and this patch fails to apply:

1. **Remove the patch file:** `rm ./patches/jsdom+26.1.0.patch`
2. **Run the installation:** `npm install`
3. **Find the unmodified jsdom source:** Open `node_modules/jsdom/lib/jsdom/browser/Window.js` (the path might have changed).
4. **Apply the change manually:** Locate where `location` is defined and set `configurable: true`.
5. **Re-create the patch:** `npx patch-package jsdom`

This will generate a new patch file for the updated version of `jsdom`. Remember to update this README if the reason for the patch changes.
