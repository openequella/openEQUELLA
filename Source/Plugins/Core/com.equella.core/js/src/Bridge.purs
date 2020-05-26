module Bridge where 


import Prelude

import Effect (Effect)
import React (ReactClass)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {

}

tsBridge :: Bridge 
tsBridge = {

} 

foreign import setupBridge :: Bridge -> Effect Unit 

main :: Effect Unit
main = setupBridge tsBridge
