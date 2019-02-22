module Bridge where 

import Prelude

import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Foreign (Foreign)
import OEQ.MainUI.Routes (Route(..), forcePushRoute, routeHref)
import OEQ.MainUI.Template (templateClass)
import OEQ.UI.Security.ACLEditor (aclEditorClass)
import React (ReactClass)
import React.SyntheticEvent (SyntheticMouseEvent)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    routes :: Foreign,
    router :: Route -> {href::String, onClick :: EffectFn1 SyntheticMouseEvent Unit},
    forcePushRoute :: EffectFn1 Route Unit, 
    "Template" :: forall p. ReactClass p,
    "AclEditor" :: forall p. ReactClass p
}

tsBridge :: Bridge 
tsBridge = {
    routes : unsafeCoerce $ {
        "CoursesPage": CoursesPage, 
        "CourseEdit": CourseEdit, 
        "NewCourse": NewCourse
        },
    router : routeHref,
    forcePushRoute: mkEffectFn1 forcePushRoute,
    "Template" : unsafeCoerce templateClass,
    "AclEditor" : unsafeCoerce aclEditorClass
} 