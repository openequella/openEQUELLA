module OEQ.Search.OwnerControl where 

import Prelude

import Common.CommonStrings (commonString)
import Data.Array (head)
import Data.Array as Array
import Data.Lens (set)
import Data.Lens.At (at)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer, saveRef)
import Dispatcher.React as DR
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Ref as Ref
import Effect.Uncurried (mkEffectFn1)
import MaterialUI.Button (button)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Enums (raised)
import MaterialUI.Icon (icon)
import MaterialUI.Styles (withStyles)
import OEQ.Data.User (UserDetails(..), UserGroupRoles(..))
import OEQ.Environment (prepLangStrings)
import OEQ.Search.SearchControl (Chip(..), Placement(..), SearchControl)
import OEQ.Search.SearchQuery (_params, singleParam)
import OEQ.UI.Common (unsafeWithRef)
import OEQ.UI.Icons (userIcon, userIconName)
import OEQ.UI.SearchFilters (filterSection)
import OEQ.UI.SearchUser (UGREnabled(..), userSearch)
import React (component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)

data Command = SelectOwner | OwnerSelected (Maybe UserDetails) | CloseOwner
type State = {selectOwner::Boolean, userDetails:: Maybe UserDetails}

ownerControl :: Placement -> Effect SearchControl
ownerControl placement = do 
  ocRef <- Ref.new Nothing
  let 
    _userDetails = prop (SProxy :: SProxy "userDetails")
    _owner = at "owner"
    eval = case _ of 
      CloseOwner -> modifyState _{selectOwner=false}
      SelectOwner -> modifyState _{selectOwner=true}
      OwnerSelected o -> do 
        modifyState _{selectOwner=false, userDetails = o}
        {updateQuery} <- getProps
        let ownerParam (UserDetails {id}) = singleParam id "owner" id
        liftEffect $ updateQuery $ set (_params <<< _owner) $ ownerParam <$> o
           
    ownerComponent = unsafeCreateLeafElement $ 
          withStyles styles $ component "OwnerControl" $ \this -> do 
      let 
        d = eval >>> affAction this 
        render {props:{classes,updateQuery}, state:{selectOwner}} = 
          filterSection {name:string.filterOwner.title, icon: userIcon} $ [
            button {variant: raised, onClick: d SelectOwner } [ 
              icon {className: classes.ownerIcon} [text userIconName],
              text commonString.action.select
            ],
            dialog {open: selectOwner, onClose: d CloseOwner} [
              dialogTitle_ [ text string.filterOwner.selectTitle ],
              dialogContent {className: classes.ownerDialog} [
                userSearch {
                    onSelect: mkEffectFn1 $ d <<< ownerSelected, 
                    onCancel: d CloseOwner, 
                    clickEntry: true,
                    enabled: UGREnabled {users:true, groups:false, roles:false}
                }
              ]
            ]
          ]
          where 
          ownerSelected (UserGroupRoles {users}) = OwnerSelected $ head users
      pure {render: DR.renderer render this, state:{selectOwner:false, userDetails: Nothing}}

    renderer {query,updateQuery,results} = do 
      chipsM <- unsafeWithRef ocRef \cthis -> do
        let mkChip (UserDetails {username}) = Chip {
              label: string.filterOwner.chip <> username, 
              onDelete: affAction cthis $ eval $ OwnerSelected Nothing
            }
        (Array.fromFoldable <<< map mkChip <<< _.userDetails) <$> R.getState cthis
      pure {
        render:[
            Tuple placement $ ownerComponent {query,updateQuery,results, innerRef: saveRef ocRef}
        ], 
        chips: fromMaybe [] chipsM
      }
  pure renderer
  where 
  styles theme = {
    ownerIcon: {
      marginRight: theme.spacing.unit
    }, 
    ownerDialog: {
      width: 600,
      height: 600
    } 
  }

rawStrings :: { prefix :: String
, strings :: { filterOwner :: { title :: String
                              , chip :: String
                              , selectTitle :: String
                              }
             }
}
rawStrings = {prefix: "searchpage", 
  strings: {
    filterOwner: {
      title: "Owner",
      chip: "Owner: ",
      selectTitle: "Select user to filter by"
    }
  }
}

string :: { filterOwner :: { title :: String
                 , chip :: String
                 , selectTitle :: String
                 }
}
string = prepLangStrings rawStrings
