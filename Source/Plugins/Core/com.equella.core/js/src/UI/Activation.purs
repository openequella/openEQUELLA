module OEQ.UI.Activation (createActivation)
where 

import Prelude

import Control.Alt ((<|>))
import Control.Monad.Trans.Class (lift)
import Data.Date (Date)
import Data.Lens (Lens', set, view)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..))
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, stateRenderer)
import Effect.Uncurried (mkEffectFn1)
import ExtUI.MaterialUIPicker.DatePicker (datePicker, mkProps)
import MaterialUI.Button (button)
import MaterialUI.Properties (onClick)
import OEQ.API.Requests (postJson)
import OEQ.Data.Activation (encodeActivateRequset)
import OEQ.Data.Item (ItemRef)
import OEQ.Utils.Dates (LuxonDate, luxonToDate, parseIsoToLuxon)
import OEQ.Utils.Dates as Dates
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div', text)
import TSComponents (CourseEntity, courseSelectClass)

type State = {
  startDate :: Maybe Date, 
  endDate :: Maybe Date, 
  course :: Maybe CourseEntity
}
data Command = SetDate (Maybe Date -> State -> State) (Nullable LuxonDate) | SetCourse CourseEntity | Activate

createActivation :: { attachmentUuid :: String, item :: ItemRef } -> ReactElement
createActivation = unsafeCreateLeafElement $ component "CreateActivation" $ \this -> do
  let 
    _startDate = prop (SProxy :: SProxy "startDate")
    _endDate = prop (SProxy :: SProxy "endDate")
    d = eval >>> affAction this

    render s = div' [
        dc _startDate,
        dc _endDate, 
        button [onClick $ \_ -> d $ Activate] [ text "Activate"],
        unsafeCreateLeafElement courseSelectClass {
          course: toNullable s.course,  
          onCourseSelect: mkEffectFn1 $ d <<< SetCourse }
    ]
      where 
      dc :: Lens' State (Maybe Date) -> ReactElement
      dc l = datePicker [
        mkProps {clearable: true, value: toNullable $ Dates.dateToLocalJSDate <$> view l s, 
        onChange: mkEffectFn1 $ d <<< SetDate (set l)}]

    eval = case _ of 
      SetDate f jsd -> modifyState (f $ Dates.luxonToDate <$> toMaybe jsd)
      SetCourse c -> 
        let fromIso = toMaybe >>> map (luxonToDate <<< parseIsoToLuxon) 
        in modifyState \s -> s {
        course = Just c, 
        startDate = fromIso c.from <|> s.startDate, 
        endDate = fromIso c.until <|> s.endDate
      }
      Activate -> do 
        {attachmentUuid, item} <- getProps
        getState >>= case _ of 
          {startDate: Just from, endDate: Just until, course: Just {uuid:courseUuid}} -> do
            _ <- lift $ postJson "api/activation" $ 
              encodeActivateRequset {"type": "cal", item, attachmentUuid, from, until, courseUuid}
            pure unit 
          _ -> pure unit
  pure {render:stateRenderer render this, state:{
    startDate: Nothing, 
    endDate: Nothing, 
    course : Nothing
  } :: State}