# The Main UI

When the new UI is turned on, openEQUELLA runs in a Single Page App (SPA) style with the initial HTML markup being supplied by [react.ftl](../../Source/Plugins/Core/com.equella.core/resources/view/layouts/outer/react.ftl).

The entrypoint for the SPA is located at [mainui/index.tsx](../../Source/Plugins/Core/com.equella.core/js/tsrc/mainui/index.tsx).

The route component uses [react-router](https://reacttraining.com/react-router/web/guides/quick-start) for routing
with pages declared inside [mainui/routes.ts](../../Source/Plugins/Core/com.equella.core/js/tsrc/mainui/routes.tsx)

## Template

The new SPA uses a root level template component which supports the following:

- Responsive menu
- App bar with title and user links menu
- Optional extra markup in title (search bar)
- Additional area for tabs
- Error notifications

In order to maintain a smooth navigation experience, individual pages no longer render their own top level `<Template>` components as this would cause a
whole page DOM refresh which is usually visible to the user.

Instead, pages need to update the properties of the `<Template>` using a callback which is passed
into their root component. Usually this will be done once in the `componentDidMount()` method or with an effect hook `React.useEffect()`.

See the available properties at: [mainui/Template.tsx](../../Source/Plugins/Core/com.equella.core/js/tsrc/mainui/Template.tsx)

## HelloWorld page example (hooks and component)

openEQUELLA pages are React-Router compatible components which
are passed a few extra properties for interacting with the template and page as a whole:

```typescript
export type TemplateUpdate = (
  templateProps: Readonly<TemplateProps>
) => TemplateProps;

export interface OEQRouteComponentProps<T> extends RouteComponentProps<T> {
  updateTemplate(edit: TemplateUpdate): void;
  redirect(to: LocationDescriptor): void;
  setPreventNavigation(b: boolean): void;
  refreshUser(): void;
}
```

So a simple "Hello World" page can be declared like this:

```typescript
import * as React from "react";
import { OEQRouteComponentProps } from "../mainui/routes";
import { Typography } from "@material-ui/core";
import { templateDefaults } from "../mainui/Template";

interface HelloWorldProps extends OEQRouteComponentProps {
  // if your page has url props they would go in here
}

export function HelloWorldHooks(props: HelloWorldProps) {
  React.useEffect(
    () => props.updateTemplate(templateDefaults("Hello world")),
    // templateDefaults(title) is a function which returns a template update function
    // which resets the template to have sensible defaults and the given title
    []
  );
  return <Typography variant="h2">Hello World with react hooks</Typography>;
}

export class HelloWorldComponent extends React.Component<HelloWorldProps> {
  render() {
    return (
      <Typography variant="h2">Hello World with a react component</Typography>
    );
  }

  componentDidMount() {
    this.props.updateTemplate(templateDefaults("Hello world"));
  }
}
```

In order to access this page you need to declare it in the `routes.tsx`:

```typescript
export const routes = {
  Courses: { path: "/page/course", exact: true, component: SearchCourse },
  NewCourse: { path: "/page/course/new", exact: true, component: EditCourse },
  EditCourse: {
    path: "/page/course/:uuid",
    to: function(uuid: string) {
      return "/page/course/" + uuid;
    },
    render: (p: OEQRouteComponentProps<any>) => (
      <EditCourse {...p} uuid={p.match.params.uuid} />
    )
  },
  // the other routes are here ...
  HelloWorldHooks: {
    path: "/helloworld/hooks",
    component: HelloWorldHooks
  },
  HelloWorldComponent: {
    path: "/helloworld/component",
    component: HelloWorldComponent
  }
};
```

Now you should be able to see the page by going to:

`http://{insturl}/helloworld/hooks` or `http://{insturl}/helloworld/component`

Please see [react-router](https://reacttraining.com/react-router/web/guides/quick-start)
for examples on how to use route specific functionaility usage
(e.g. linking to other pages and extracting parameters for urls).
