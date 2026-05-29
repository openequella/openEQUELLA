# FreeMarker Nested Content Detection — Why Reflection Is Required

## Context

In `AbstractRenderDirective.java`, we use Java reflection to access FreeMarker's
package-private `getCurrentMacroContext()` method and the `callPlace` field on
`Macro.Context`. This document explains why this reflection is necessary and why
public FreeMarker APIs cannot achieve the same result.

## The Problem

The `<@render>` FreeMarker macro is defined as:

```ftl
<#macro render section ...>
  <@_render section=section ...><#nested/></@_render>
</#macro>
```

`_render` is the Java directive (`AbstractRenderDirective`). When it executes, it
needs to know whether the **outer** `<@render>` macro was called with nested
content:

```ftl
<@render section=x/>                     ← self-closing, NO nested content
<@render section=x>some stuff</@render>  ← HAS nested content
```

If `_render` wraps the body when there's no nested content, the `<#nested/>`
instruction executes against a null macro context, corrupting FreeMarker's
internal instruction stack and causing `ArrayIndexOutOfBoundsException`.

## Why Public APIs Don't Work

### `Environment.getCurrentDirectiveCallPlace()` (public, since 2.3.22)

This returns the `DirectiveCallPlace` for the **current directive** — i.e., the
`<@_render>` call, not the outer `<@render>` macro call. The `<@_render>` call
**always** has a child (`<#nested/>`), so `getChildCount() > 0` is always true
regardless of whether the user provided content to `<@render>`.

| API target                        | Returns                        | Useful? |
| --------------------------------- | ------------------------------ | ------- |
| `getCurrentDirectiveCallPlace()`  | `<@_render>` UnifiedCall       | ❌ No   |
| `getCurrentMacroContext()` (refl) | `<@render>` Macro.Context      | ✅ Yes  |

### `TemplateDirectiveBody` nullness

FreeMarker passes `body=null` when a directive is called self-closing. But
`_render` is called from within the `render` macro's body, so its `body`
parameter is always non-null (it wraps the `<#nested/>` instruction). The body
nullness tells us about the `_render` call, not the `render` macro call.

### `DirectiveCallPlace.isNestedOutputCacheable()`

Returns whether the nested content is static. For `<@_render><#nested/></@_render>`,
this is always `false` because `<#nested/>` is dynamic. Not useful for detecting
presence/absence of content.

## What We Reflect On

In FreeMarker 2.3.34:

1. **`Environment.getCurrentMacroContext()`** (package-private method) — returns
   the `Macro.Context` for the currently executing macro (`render`)
2. **`Macro.Context.callPlace`** (package-private field) — the `UnifiedCall`
   element for the outer `<@render ...>` call
3. **`TemplateElement.getChildCount()`** (public method) — returns `0` for
   self-closing calls, `>0` for calls with nested content

Only steps 1 and 2 require reflection. Step 3 is public API.

### Historical note

In FreeMarker 2.3.23, `Macro.Context` had a `nestedContent` field (a direct
`TemplateElement` reference, null when no nested content). In 2.3.34, this was
replaced with `callPlace` (a reference to the `UnifiedCall` element), and nested
content is determined via `getChildCount()`.

## Risk Assessment

| Risk                                          | Likelihood | Mitigation                                       |
| --------------------------------------------- | ---------- | ------------------------------------------------ |
| Method/field renamed in future FreeMarker      | Low        | Graceful fallback with logging; field names have  |
|                                                |            | been stable across 2.3.x for 10+ years           |
| Java module system blocks reflection           | Moderate   | Would affect all FreeMarker internal usage across |
|                                                |            | the codebase; broader upgrade concern             |
| FreeMarker 3.x changes structure               | N/A        | FM3 is a different package namespace entirely;    |
|                                                |            | would require a full migration regardless         |

## Alternatives Considered

1. **Remove the guard, add error recovery** — This was attempted in MR !548.
   Failed because the issue isn't recoverable; bodies that shouldn't execute
   corrupt the instruction stack before any recovery can happen.

2. **Restructure FTL macros** to avoid `<#nested/>` — Would require changes
   across dozens of templates and plugin modules. Disproportionate effort for
   legacy code.

3. **Move to FreeMarker 3** — Different package namespace, breaking migration,
   currently in alpha. Not viable.

## Conclusion

Reflection is the pragmatically correct choice. It's minimal (2 reflective
accesses), isolated to one method, and has a graceful fallback. The public API
genuinely cannot provide the information needed due to FreeMarker's structural
separation of directive and macro contexts.
