# Unexpected error handling

Handling of unexpected errors such as network problems & server errors can be painful,
so the `Template` component has a property for displaying them:

`errorResponse` in `TemplateProps` in [Template.purs](../../Source/Plugins/Core/com.equella.core/js/src/MainUI/Template.purs) or [Template.ts](../../Source/Plugins/Core/com.equella.core/js/tsrc/api/Template.ts)

Exceptions thrown from REST API's are converted into `ErrorResponse` JSON objects and HTTP status codes 
by the [RestEasyExceptionMapper](../../Source/Plugins/Core/com.equella.core/src/com/tle/web/remoting/resteasy/RestEasyExceptionMapper.java) 

Bear in mind that this is only for **unexpected errors**, if the error is something that would be reasonable for 
a user to generate, it should be handled by displaying error text next to the location of the field which caused the 
error. 

In the context of REST server / JS client this means the server should return a 400 Bad Request with some JSON 
which contains error codes/ids for relevant fields. For example you might return a JSON response for an invalid image upload 
such as:

```json
{
  "logo": "invalidformat"
}
```

* TODO - create and document function which can take an axios response and turn it into an `ErrorResponse`

# Notifications

Some user actions require confirmation and/or acknowledgement. The [material design guidelines](https://material.io/design/communication/confirmation-acknowledgement.html#acknowledgement) 
provides great docs for deciding which UI component is appropriate for each situation. 

One of the prescribed components is the [SnackBar](https://material.io/design/components/snackbars.html),
which can display a small notification message which will automatically disappear after a set amount of time. 
There is a small wrapper component in openEQUELLA called [MessageInfo](../../Source/Plugins/Core/com.equella.core/js/tsrc/components/MessageInfo.tsx) 
which decorates the standard `SnackBar` with an Icon and colours based on the variant chosen: `success`, `warning`, `error`, `info`.
