module OEQ.UI.ItemSummary.CopyrightSummary where 

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes, mapWithIndex)
import Data.Date (Date)
import Data.Lens (_Just, set)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe, isNothing, maybe)
import Data.Ord (lessThan)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..))
import Data.Validation.Semigroup (V, invalid, unV)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect.Class (liftEffect)
import Effect.Now (nowDate)
import Effect.Uncurried (EffectFn1)
import Foreign.Object (Object)
import Foreign.Object as Object
import MaterialUI.Button (button)
import MaterialUI.Colors (fade)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent_)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Enums (md, primary, raised)
import MaterialUI.Enums as E
import MaterialUI.Paper (paper)
import MaterialUI.Styles (withStyles)
import MaterialUI.Table (table_)
import MaterialUI.TableBody (tableBody_)
import MaterialUI.TableCell (classTableCell, tableCell_)
import MaterialUI.TableHead (tableHead_)
import MaterialUI.TableRow (tableRow, tableRow_)
import OEQ.API.Course (getCourseByCode)
import OEQ.API.Requests (errorOr, postJsonExpect)
import OEQ.Data.Activation (ActivateReqest, encodeActivateRequset)
import OEQ.Data.Course (CourseEntity)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.Item (ActivationStatus(..), CopyrightAttachment, CopyrightSummary, HoldingSummary(..), ItemRef)
import OEQ.UI.Activation (ActivationData, activationDefaults, activationParams)
import React (ReactElement, component, unsafeCreateElement, unsafeCreateLeafElement)
import React.DOM (div, text)
import React.DOM.Dynamic (a)
import React.DOM.Props (href)
import React.DOM.Props as DP

type CopyrightProps = {
  onError :: EffectFn1 ErrorResponse Unit,
  copyright :: CopyrightSummary,
  courseCode :: Maybe String
}

data Command = Activate CopyrightAttachment 
  | ChangeData ActivationData 
  | Init
  | CancelDialog 
  | FinishActivate

type State = {
  currentActivation :: Maybe {open::Boolean, attachment::CopyrightAttachment, activation :: ActivationData },
  currentCourse :: Maybe CourseEntity,
  errors :: Object String
}

type Error = Tuple String String

errored :: forall a. String -> String -> V (Array Error) a 
errored field msg = invalid [Tuple field msg]

validate :: Date -> ActivationData -> ItemRef -> String ->  V (Array Error) ActivateReqest
validate nowd ad item attachmentUuid = ado 
    from <- validateDate "startDate" ad.startDate
    until <- validateDate "endDate" ad.endDate
    courseUuid <- validateCourse
    if maybe true (lessThan nowd) ad.endDate
       then pure unit else errored "endDate" "Must be in the future"
  in {"type": "cal", item, attachmentUuid, from, until, courseUuid, citation: ad.citation}
  where 
  validateCourse = maybe (errored "course" "You must select a course") (pure <<< _.uuid) ad.course
  validateDate field date = maybe (errored field "You must enter a date") pure date

copyrightSummary :: CopyrightProps -> ReactElement
copyrightSummary = unsafeCreateLeafElement $ withStyles styles $ component "CopyrightSummary" $ \this -> do
  let
    copyrightCell = unsafeCreateElement $ withStyles (\theme -> { 
        head: {
          backgroundColor: fade theme.palette.primary.main 0.05
          -- height: "2em"
        }
    }) classTableCell
    
    emptyErrors :: Object String
    emptyErrors = Object.empty
    
    _currentActivation = prop (SProxy :: SProxy "currentActivation")
    _activation = prop (SProxy :: SProxy "activation")
    _open = prop (SProxy :: SProxy "open")
    
    d = eval >>> affAction this

    render {props: {classes, onError, copyright: {holding}}, state:s@{currentActivation:ca, errors}} = let 
      attachmentLink {title,href:h} = a [href $ fromMaybe "" h] [ text title ]
      headerCell className t = copyrightCell {variant: E.head, className } [t] 
      attachmentCell attachment = tableCell_ [ case attachment.href of 
          Nothing -> text attachment.title
          Just h -> a [href h] [ text attachment.title ]
      ]
      statusCell attachment = tableCell_ [ text $ statusString attachment.status]
      activateCell attachment = tableCell_ [
          button {color: E.primary, variant: E.raised, onClick: d $ Activate attachment} [ text "Activate" ]
      ]
      bookSection {pageCount,attachment} = tableRow {key:attachment.uuid} [
        attachmentCell attachment,
        tableCell_ [ text $ show pageCount ], 
        statusCell attachment,
        activateCell attachment
      ]
      bookChapter i {title, sections} = let ft t = if i == 0 then t else ""
        in table_ [
          tableHead_ [
            tableRow_ [
              headerCell classes.resourceCell $ text $ "Chapter : " <> title,
              headerCell classes.pageCell $ text $ ft "Pages",
              headerCell classes.statusCell $ text $ ft "Status",
              headerCell classes.actionCell $ text "Actions"
            ]
          ],
          tableBody_ $ bookSection <$> sections
        ]

      journalSection {attachment} = tableRow {key:attachment.uuid} [
        attachmentCell attachment,
        statusCell attachment,
        activateCell attachment
      ]
      journalPortion {title, sections} = [ 
        table_ [
          tableHead_ [
            tableRow_ [
              headerCell classes.resourceCell $ text title,
              headerCell classes.statusCell $ text "Status",
              headerCell classes.actionCell $ text "Actions"
            ]
          ],
          tableBody_ $ journalSection <$> sections
        ]
      ]
      
      renderHolding (BookSummary {totalPages,chapters}) = paper {className:classes.tablePaper} $ mapWithIndex bookChapter chapters
      renderHolding (JournalSummary {portions}) = paper {className:classes.tablePaper} $ journalPortion =<< portions 

      in div [DP.className classes.copyrightTable] $ catMaybes [
          Just $ renderHolding holding,
          ca <#> (\a ->
            dialog {
              open: a.open, 
              onClose: d CancelDialog, 
              maxWidth: md,
              fullWidth: true
             } [
              dialogTitle_ [ text "Activate" ],
              dialogContent_ [
                activationParams {
                  "data": a.activation, errors, 
                  onChange: d <<< ChangeData, onError, 
                  canEditCourse: isNothing s.currentCourse
                }
              ], 
              dialogActions_ [
                button {color: primary, onClick: d CancelDialog } [text "Cancel"],
                button {color: primary, variant: raised, onClick: d FinishActivate } [text "Activate"]
              ]
            ])
      ] 
    eval = case _ of 
      Activate a -> modifyState \s -> s {errors = emptyErrors, currentActivation = Just {
          attachment:a, 
          open:true,
          activation: s.currentCourse # maybe {startDate:Nothing, course:Nothing, 
                                endDate:Nothing, citation:Nothing} activationDefaults 
        }
      }
      ChangeData ad -> modifyState $ set (_currentActivation <<< _Just <<< _activation) ad
      CancelDialog -> modifyState $ set (_currentActivation <<< _Just <<< _open) false
      Init -> do 
        {courseCode} <- getProps
        courseCode # maybe (pure unit) \cc -> do
          (lift $ getCourseByCode cc) >>= 
            errorOr (\c -> modifyState _ {currentCourse=Just c})
      FinishActivate -> do 
        {currentActivation} <- getState
        nowd <- liftEffect $ nowDate
        case currentActivation of 
          Just { attachment:a, activation } -> do
            let doReq actreq = errorOr (\_ -> eval CancelDialog) <=< 
                    lift $ postJsonExpect 201 "api/activation" $ encodeActivateRequset actreq
            validate nowd activation a.item a.uuid # 
              unV (\e -> modifyState _ {errors = Object.fromFoldable e}) doReq
          _ -> pure unit 

  pure {
    render: renderer render this, 
    state:{currentActivation:Nothing, errors:emptyErrors, currentCourse: Nothing}::State,
    componentDidMount: d Init
  }
  where 
  styles theme = {
    resourceCell: {
      width: "50%"
    },
    pageCell: {
      width: "30%"
    },
    actionCell: {
      width: "10%"
    },
    statusCell: {
      width: "10%"
    },
    copyrightTable: {
      maxWidth: 1024
    }, 
    tablePaper: {
      width: "100%",
      overflowX: "auto"
    }
  }

statusString :: ActivationStatus -> String 
statusString = case _ of 
  Active -> "Active"
  Inactive -> "Inactive"
  Pending -> "Pending"