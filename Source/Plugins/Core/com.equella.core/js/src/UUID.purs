module UUID where 


import Control.Monad.IOSync (IOSync(..))

foreign import newUUID :: IOSync String 
