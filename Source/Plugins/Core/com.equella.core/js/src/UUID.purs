module UUID where 


import Effect (Effect)

foreign import newUUID :: Effect String 
