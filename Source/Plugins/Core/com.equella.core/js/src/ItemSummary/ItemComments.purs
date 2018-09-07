module ItemSummary.ItemComments where 

import Prelude hiding (div)

import AjaxRequests (ErrorResponse, errorOr, getJson, postJsonExpect)
import Control.Monad.Except (ExceptT(..), runExceptT, throwError)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Argonaut (Json, decodeJson, jsonEmptyObject, (:=), (~>))
import Data.Argonaut.Encode ((~>?))
import Data.Array (catMaybes, findMap)
import Data.Array as Array
import Data.Int (fromString)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect.Uncurried (EffectFn1)
import Global.Unsafe (unsafeEncodeURIComponent)
import ItemSummary (ItemComment, decodeComment)
import ItemSummary.Api (itemApiPath, uuidHeader)
import MaterialUI.Button (button, raised)
import MaterialUI.Color as Color
import MaterialUI.FormControl (formControl)
import MaterialUI.FormControlLabel (control, formControlLabel)
import MaterialUI.FormGroup (formGroup_)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Input (fullWidth, multiline, placeholder, rowsMax, value)
import MaterialUI.InputLabel (inputLabel_)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Properties (className, color, mkProp, onChange, onClick, variant)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.Switch (switch)
import MaterialUI.SwitchBase (checked)
import MaterialUI.TextField (label, textField)
import Network.HTTP.Affjax as Ajax
import QueryString (queryString)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', em', text)
import React.DOM.Props as RP
import Utils.UI (checkChange, textChange)
import Utils.UI as Utils

data Command = CommentText String 
  | AnonymousFlag Boolean 
  | SetRating String
  | MakeComment 
  | DeleteComment String 
  | LoadComments

type ItemCommentProps = {
  uuid :: String, 
  version :: Int, 
  onError :: EffectFn1 ErrorResponse Unit, 
  canAdd :: Boolean, 
  canDelete :: Boolean,
  anonymousOnly :: Boolean,
  allowAnonymous :: Boolean
}

type State = {
  commentText :: String, 
  anonymous :: Boolean,
  rating :: Maybe Int,
  comments :: Array ItemComment
}

encodeComment :: {comment::String, anonymous::Boolean, rating::Maybe Int} -> Json
encodeComment {comment, anonymous, rating} = 
  "comment" := comment ~> 
  "anonymous" := anonymous ~> 
  ((:=) "rating" <$> rating) ~>? 
  jsonEmptyObject

itemComments :: ItemCommentProps -> ReactElement
itemComments = unsafeCreateLeafElement $ withStyles styles $ component "ItemComments" $ \this -> do
  let 
    d = eval >>> affAction this

    renderComment canDelete c = listItem [disableGutters true] $ catMaybes [
            -- listItemIcon_ [ userIcon ],
            Just $ listItemText [primary c.comment, secondary $ c.date <> " - " <> show c.rating ], 
            guard canDelete $> listItemSecondaryAction_ [ 
                                  iconButton [onClick \_ -> d $ DeleteComment c.uuid] 
                                  [ icon_ [ text "delete" ] ] 
                                ]
          ]

    ratingItem v = let sv = show v in menuItem [mkProp "value" $ show v] [ text $ show v ]

    render {props:{classes,canAdd,canDelete,anonymousOnly,allowAnonymous}, state:{rating,commentText,comments,anonymous}} = let 
      renderAdd = [
        textField [
          textChange d CommentText, value commentText, 
          label "Enter comment", 
          placeholder "Enter comment", 
          rowsMax 3, 
          multiline true, 
          fullWidth true], 
        div [RP.className classes.commentButtons] $ catMaybes [ 
          Just $ formControl [className classes.ratingField ] [
            inputLabel_ [ text "Rating" ], 
            select [ value $ fromMaybe "" (show <$> rating), onChange $ Utils.valueChange (d <<< SetRating) ] $ 
              [ menuItem [mkProp "value" ""] [ em' [ text "No rating" ] ] ] 
                <> (ratingItem <$> Array.range 1 5)
          ],
          (guard $ not anonymousOnly && allowAnonymous) $> formGroup_ [
              formControlLabel [
                label "Anonymous", 
                control $ switch [
                  checked anonymous, 
                  onChange $ checkChange $ d <<< AnonymousFlag] 
              ]
            ],
          Just $ button [onClick \_ -> d MakeComment, variant raised, color Color.primary] [text "Comment"]
        ]
      ]
     in div' $ guard canAdd *> renderAdd <> 
      [ list [ disablePadding true ] $ renderComment canDelete <$> comments ]
    
    eval = case _ of 
      SetRating r -> 
        modifyState _ {rating = fromString r}
      LoadComments -> do 
        {uuid,version,onError} <- getProps
        (lift $ getJson (itemApiPath uuid version <> "/comment") (decodeJson >=> traverse decodeComment)) >>= 
          errorOr (\c -> modifyState _ {comments = c})
      CommentText t -> modifyState _ {commentText = t}
      AnonymousFlag b -> modifyState _ {anonymous = b}
      MakeComment -> do 
        {commentText,comments,anonymous:anon,rating} <- getState
        {uuid,version,anonymousOnly,allowAnonymous} <- getProps
        res <- lift $ runExceptT $ do
          let anonymous = anonymousOnly || (allowAnonymous && anon)
              commentJson = encodeComment {comment:commentText, anonymous,rating}
          {headers} <- ExceptT $ postJsonExpect 201 (itemApiPath uuid version <> "/comment")  commentJson
          case findMap uuidHeader headers of 
              Nothing -> throwError {code:500,description:Nothing,error:"No UUID header for comment"}
              Just commentUuid -> do 
                  ExceptT $ getJson (itemApiPath uuid version <> "/comment/" <> unsafeEncodeURIComponent commentUuid) (decodeComment)
        errorOr (\c -> modifyState \s -> s {commentText = "", comments = Array.cons c s.comments}) res
        
      DeleteComment commentUuid -> do 
        {uuid,version} <- getProps
        void $ lift $ Ajax.delete_ (itemApiPath uuid version <> "/comment?" <> queryString [Tuple "commentuuid" commentUuid])
        modifyState \s -> s {comments = Array.filter (_.uuid >>> notEq commentUuid) s.comments}
  pure {
    render:renderer render this, 
    state: { commentText:"", comments:[], anonymous:false, rating: Nothing} :: State, 
    componentDidMount: d LoadComments
  }
  where
  styles t = {
    commentButtons: {
      marginTop: t.spacing.unit,
      display: "flex", 
      justifyContent: "flex-end"
    }, 
    ratingField: {
      width: "5em", 
      marginRight: t.spacing.unit
    }
  }