# openEQUELLA Typescript REST API SDK

This module provides a code generated SDK for access to the openEQUELLA REST API.

## Local Development

### `npm run build` or `yarn build`

Bundles the package to the `dist` folder.  The package is optimized and bundled with Rollup into an
ES Module).

### `npm run test` or `yarn test`

Will run the Jest tests for the module - requires a local running oEQ which values specified in
`test/TestConfig.ts`. (More work to be done with this in the future.)

## Implementation Considerations

First, why was this done?

1. To reduce coupling between the UI layers and the API calls; so as
2. To ease testing of the UI components, and ease testing of the API interactions; and
3. Provide a unit of stand-alone code that could easily be developed on and tested.

When approaching this a few items were considered:

1. Using codegen from the oEQ generated swagger.yml; and
2. Doing codegen just for the models

However, both of these had issues, and so in the end the code has simply been hand rolled. Here is
an outline of the issues faced with these to help any future efforts.

### Codegen from oEQ's swagger.yml

First and foremost, there were some issues with generating from the current swagger.yml - more so
with the current openapi generator outright failing. But the generated code from such a tool is
rather simplistic and doesn't really reduce coupling between the implementation details of the API
interaction (such as, use of Axios) and so results in limited value.

Further, some of the oEQ endpoints have been written code first, and so the YAML and the resulting
API is not necessarily that user friendly - and definitely lacks documentation.

### Codegen just for the models

This would be a great win, but the swagger/openapi codgen tools don't really provide a means to
do this. But even if they did, again due to the way the oEQ API has been built over time (very much
a code first approach) the resultant models would not necessarily be ideal - especially with a strong
prevalence of the use of maps. (Maps are not really directly supported via swagger/openapi so you
end up with some work arounds.)

Ideally then we could just write our own codegen from the models. However, this has a couple of issues
on its own:

1. The models currently used by the API are not really stand-alone, but are the same as used elsewhere
   in oEQ. That means it's not just a simple case of using reflection targeting one or two packages
   and generating JS/TS representations of the classes (or interfaces) within that package. Instead,
   we'd have to probably maintain a separate list of all the intended targets.
2. The current build tool is SBT, and ideally we'd like to move away from that so adding further
   custom code/tasks is undesirable. However, we'd really have to plug into this so that we could
   have access to the compiled classes on which we could then carry out reflection based codegen.
