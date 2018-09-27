module OEQ.UI.Activation where 

import Prelude hiding (div)

import Control.Alt ((<|>))
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Date (Date)
import Data.Lens (Lens', set, view)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe, isNothing, maybe)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import ExtUI.MaterialUIPicker.DatePicker (datePicker)
import Foreign.Object (Object, lookup, member)
import MaterialUI.Styles (withStyles)
import OEQ.API.Requests (errorOr)
import OEQ.API.Schema (listAllCitations)
import OEQ.Data.Error (ErrorResponse)
import OEQ.UI.Common (valueChange)
import OEQ.Utils.Dates (LuxonDate, luxonToDate, parseIsoToLuxon)
import OEQ.Utils.Dates as Dates
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div', div, text)
import React.DOM.Props as RP
import MaterialUI.Enums as E
import MaterialUI.MenuItem (menuItem)
import MaterialUI.TextField (textField)
import TSComponents (CourseEntity, courseSelect)

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
  | SetCourse (Nullable CourseEntity)
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

    render {props:{errors, classes, data:ad}, state:s} = let disabled = isNothing ad.course
      in div [RP.className classes.container] [
        courseSelect {
          maxResults: 4,
          "TextFieldProps": {
            className: classes.field, 
            required: true,
            margin: E.dense,
            helperText: fromMaybe " " $ lookup "course" errors,
            error: member "course" errors,
            fullWidth: true
          },
          course: toNullable ad.course,  
          title: "Course", 
          onCourseSelect: mkEffectFn1 $ d <<< SetCourse },
        dc _startDate "startDate" {label: "Start date", disabled, disablePast:false},
        dc _endDate "endDate" {label:"End date", disabled, disablePast:true},
        div' [
          textField { 
            label: "Citation",  
            className: classes.field, 
            select: true,  
            margin: E.dense,
            value: fromMaybe "" ad.citation,
            disabled,
            onChange: valueChange (d <<< SetCitation) } $ 
                [menuItem {value: ""} [text "Default"]] <>  
                  ((\c -> menuItem {value: c} [text c]) <$> s.citations)
        ]
    ]
      where 
      dc :: Lens' ActivationData (Maybe Date) -> String -> {label:: String, disabled :: Boolean, disablePast::Boolean} -> ReactElement
      dc l field {label, disabled, disablePast} = div [RP.className classes.field] [ 
        datePicker {
          label: label <> " *",
          clearable: true, 
          disabled,
          keyboard: true,
          format: "dd/MM/yyyy",
          disablePast,
          margin: E.dense,
          value: toNullable $ Dates.dateToLocalJSDate <$> view l ad, 
          helperText: fromMaybe " " $ lookup field errors,
          error: member field errors,
          onChange: mkEffectFn1 $ d <<< SetDate (set l)
        }
      ]

    modifyData f = do 
      p <- getProps 
      liftEffect $ p.onChange $ f p."data"
    eval = case _ of 
      Init -> (lift $ listAllCitations) >>= errorOr (\c -> modifyState _ {citations=c}) 
      SetDate f jsd -> modifyData (f $ Dates.luxonToDate <$> toMaybe jsd)
      SetCitation c -> modifyData _ {citation = (guard $ c /= "") $> c}
      SetCourse cm -> 
        let fromIso = toMaybe >>> map (luxonToDate <<< parseIsoToLuxon) 
            resetFromCourse c = 
              modifyData \s -> s {
                  course = Just c, 
                  startDate = fromIso c.from <|> s.startDate, 
                  endDate = fromIso c.until <|> s.endDate, 
                  citation = toMaybe c.citation <|> s.citation
                }
        in maybe (modifyData _ { course = Nothing}) resetFromCourse $ toMaybe cm
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
        marginTop: theme.spacing.unit,
        width: "20em"
      }
    }