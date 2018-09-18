module OEQ.UI.Activation where 

import Prelude hiding (div)

import Control.Alt ((<|>))
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Date (Date)
import Data.Lens (Lens', set, view)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import ExtUI.MaterialUIPicker.DatePicker (datePicker, mkProps)
import Foreign.Object (Object)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField as OldMui
import OEQ.API.Requests (errorOr)
import OEQ.API.Schema (listAllCitations)
import OEQ.Data.Error (ErrorResponse)
import OEQ.UI.Common (valueChange)
import OEQ.Utils.Dates (LuxonDate, luxonToDate, parseIsoToLuxon)
import OEQ.Utils.Dates as Dates
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div', div, text)
import React.DOM.Props as RP
import ReactMUI.MenuItem (menuItem)
import ReactMUI.TextField (textField)
import TSComponents (CourseEntity, courseSelectClass)

type ActivationData = {
  startDate :: Maybe Date, 
  endDate :: Maybe Date, 
  course :: Maybe CourseEntity, 
  citation :: Maybe String
}

type State = {
  citations :: Array String
}
data Command = SetDate (Maybe Date -> ActivationData -> ActivationData) (Nullable LuxonDate) 
  | SetCourse CourseEntity 
  | SetCitation String
  | Init 


activationParams :: { data :: ActivationData, 
  onChange :: ActivationData -> Effect Unit, 
  errors :: Object String,
  onError :: EffectFn1 ErrorResponse Unit } -> ReactElement
activationParams = unsafeCreateLeafElement $ withStyles styles $ component "ActivationParams" $ \this -> do
  let 
    _startDate = prop (SProxy :: SProxy "startDate")
    _endDate = prop (SProxy :: SProxy "endDate")
    d = eval >>> affAction this

    render {props:{classes, data:ad}, state:s} = div [RP.className classes.container] [
        unsafeCreateLeafElement courseSelectClass {
          course: toNullable ad.course,  
          required: true,
          title: "Course",
          onCourseSelect: mkEffectFn1 $ d <<< SetCourse },
        dc _startDate "Start date",
        dc _endDate "End date", 
        div' [
          textField { 
            label: "Citation",  
            className: classes.field, 
            select: true,  
            value: fromMaybe "" ad.citation,
            onChange: mkEffectFn1 $ valueChange (d <<< SetCitation) } $ 
                [menuItem {value: ""} [text "Default"]] <>  
                  ((\c -> menuItem {value: c} [text c]) <$> s.citations)
        ]
    ]
      where 
      dc :: Lens' ActivationData (Maybe Date) -> String -> ReactElement
      dc l lab = div [RP.className classes.field] [ 
        datePicker [
          OldMui.label $ lab <> " *",
          mkProps {
            clearable: true, value: toNullable $ Dates.dateToLocalJSDate <$> view l ad, 
            onChange: mkEffectFn1 $ d <<< SetDate (set l)}
        ]
      ]

    modifyData f = do 
      p <- getProps 
      liftEffect $ p.onChange $ f p."data"
    eval = case _ of 
      Init -> (lift $ listAllCitations) >>= errorOr (\c -> modifyState _ {citations=c}) 
      SetDate f jsd -> modifyData (f $ Dates.luxonToDate <$> toMaybe jsd)
      SetCitation c -> modifyData _ {citation = (guard $ c /= "") $> c}
      SetCourse c -> 
        let fromIso = toMaybe >>> map (luxonToDate <<< parseIsoToLuxon) 
        in modifyData \s -> s {
        course = Just c, 
        startDate = fromIso c.from <|> s.startDate, 
        endDate = fromIso c.until <|> s.endDate, 
        citation = toMaybe c.citation <|> s.citation
      }
  pure {
    render:renderer render this, 
    componentDidMount: d Init,
    state:{
      citations : []
    } :: State}
  where 
    styles theme = {
      container: {
        display: "flex", 
        flexDirection: "column"
      }, 
      field: {
        margin: show theme.spacing.unit <> "px 0px",
        width: "15em"
      }
    }