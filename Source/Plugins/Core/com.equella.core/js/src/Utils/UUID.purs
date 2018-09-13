module OEQ.Utils.UUID where 


import Effect (Effect)

foreign import newUUID :: Effect String 
