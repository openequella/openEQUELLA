module Bridge where 

import Prelude

import Control.Monad.IOEffFn (IOFn1)
import Data.Foreign (Foreign)
import MaterialUI.Event (Event)
import React (ReactClass)
import Routes (Route(..), routeHref)
import Security.ACLEditor (aclEditorClass)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    routes :: Foreign,
    router :: Route -> {href::String, onClick :: IOFn1 Event Unit},
    "AclEditor" :: forall p. ReactClass p
}

tsBridge :: Bridge 
tsBridge = {
    routes : unsafeCoerce $ {
        "CoursesPage": {value:CoursesPage}, 
        "CourseEdit": CourseEdit, 
        "SchemaEdit": SchemaEdit, 
        "SchemasPage": {value: SchemasPage}},
    router : routeHref,
    "AclEditor" : unsafeCoerce aclEditorClass
} 