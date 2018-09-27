module ExtUI.MaterialUIPicker.MuiPickersUtilsProvider where


import Data.TSCompat (Any)
import React (ReactClass, ReactElement, unsafeCreateElement)


type MuiPickersUtilsProviderPropsExt r = (
  utils :: Any {-Identifier:Utils-}
  | r
) 

type MuiPickersUtilsProviderProps = MuiPickersUtilsProviderPropsExt (

) 

foreign import luxonUtils :: Any

foreign import muiPickersUtilsProviderClass :: forall props. ReactClass props

muiPickersUtilsProvider :: Any -> Array ReactElement -> ReactElement
muiPickersUtilsProvider utils = unsafeCreateElement muiPickersUtilsProviderClass {utils}
