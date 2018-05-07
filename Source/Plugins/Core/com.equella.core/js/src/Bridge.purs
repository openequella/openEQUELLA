module Bridge where 

import Prelude

import Control.Monad.IOEffFn (IOFn1)
import Data.Foreign (Foreign)
import MaterialUI.Event (Event)
import React (ReactClass)
import Routes (Route(..), routeHref)
import Security.ACLEditor (aclEditorClass)
import Template (templateClass)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    routes :: Foreign,
    router :: Route -> {href::String, onClick :: IOFn1 Event Unit},
    "Template" :: forall p. ReactClass p,
    "AclEditor" :: forall p. ReactClass p
}

tsBridge :: Bridge 
tsBridge = {
    routes : unsafeCoerce $ {
        "CoursesPage": CoursesPage, 
        "CourseEdit": CourseEdit, 
        "SchemaEdit": SchemaEdit, 
        "SchemasPage": SchemasPage},
    router : routeHref,
    "Template" : unsafeCoerce templateClass,
    "AclEditor" : unsafeCoerce aclEditorClass
} 