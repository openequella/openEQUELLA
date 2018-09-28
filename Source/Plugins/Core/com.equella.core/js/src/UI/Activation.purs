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
import MaterialUI.Enums as E
import MaterialUI.Grid (grid)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField)
import MaterialUI.Typography (typography)
import OEQ.API.Requests (errorOr)
import OEQ.API.Schema (listAllCitations)
import OEQ.Data.Course (CourseEntity)
import OEQ.Data.Error (ErrorResponse)
import OEQ.UI.Common (valueChange)
import OEQ.Utils.Dates (LuxonDate, luxonToDate, parseIsoToLuxon)
import OEQ.Utils.Dates as Dates
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div', div, text)
import React.DOM.Props as RP
import TSComponents (courseSelect)

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

activationParams :: { 
  data :: ActivationData, 
  onChange :: ActivationData -> Effect Unit, 
  errors :: Object String,
  onError :: EffectFn1 ErrorResponse Unit,
  canEditCourse :: Boolean
} -> ReactElement
activationParams = unsafeCreateLeafElement $ withStyles styles $ component "ActivationParams" $ \this -> do
  let 
    _startDate = prop (SProxy :: SProxy "startDate")
    _endDate = prop (SProxy :: SProxy "endDate")
    d = eval >>> affAction this

    render {props:{errors, classes, data:ad, canEditCourse}, state:s} = let disabled = isNothing ad.course
      in div [RP.className classes.container] [
        if canEditCourse 
        then courseSelect {
              maxResults: 4,
              "TextFieldProps": {
                required: true,
                margin: E.dense,
                helperText: fromMaybe " " $ lookup "course" errors,
                error: member "course" errors,
                fullWidth: true
              },
              course: toNullable ad.course,  
              title: "Course", 
              onCourseSelect: mkEffectFn1 $ d <<< SetCourse 
            }
        else typography {} [ text $ maybe "" (\c -> c.code <> " - " <> c.name) ad.course],
        grid {container:true, spacing: 16} [ 
          grid {item:true} [ dc _startDate "startDate" {label: "Start date", disabled, disablePast:false} ],
          grid {item:true} [ dc _endDate "endDate" {label:"End date", disabled, disablePast:true} ]
        ],
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
          fullWidth: true,
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
        let resetFromCourse c = 
              let nd = activationDefaults c
              in modifyData \s -> s {
                  course = Just c, 
                  startDate = nd.startDate <|> s.startDate, 
                  endDate = nd.endDate <|> s.endDate, 
                  citation = nd.citation <|> s.citation
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
        flexDirection: "column",
        minHeight: "16em"
      }, 
      field: {
        marginTop: theme.spacing.unit,
        width: "20em"
      }
    }

activationDefaults :: CourseEntity -> ActivationData
activationDefaults c = let 
  fromIso = toMaybe >>> map (luxonToDate <<< parseIsoToLuxon) 
  in { 
    course: Just c, 
    startDate: fromIso c.from,
    endDate: fromIso c.until, 
    citation: toMaybe c.citation
  }