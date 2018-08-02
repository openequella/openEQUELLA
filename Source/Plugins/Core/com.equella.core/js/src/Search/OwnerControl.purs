module Search.OwnerControl where 

import Prelude

import Common.CommonStrings (commonString)
import Common.Icons (userIcon, userIconName)
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
import EQUELLA.Environment (prepLangStrings)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Ref as Ref
import Effect.Uncurried (mkEffectFn1)
import MaterialUI.Button (button, raised)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Icon (icon)
import MaterialUI.Modal (open)
import MaterialUI.Properties (className, onClick, onClose, variant)
import MaterialUI.Styles (withStyles)
import React (component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)
import Search.SearchControl (Chip(..), Placement(..), SearchControl)
import Search.SearchQuery (_params, singleParam)
import SearchFilters (filterSection)
import Users.SearchUser (UGREnabled(..), userSearch)
import Users.UserLookup (UserDetails(..), UserGroupRoles(..))
import Utils.UI (unsafeWithRef)

data Command = SelectOwner | OwnerSelected (Maybe UserDetails) | CloseOwner
type State = {selectOwner::Boolean, userDetails:: Maybe UserDetails}

ownerControl :: Effect SearchControl
ownerControl = do 
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
            button [ variant raised, onClick $ \_ -> d SelectOwner ] [ 
              icon [className classes.ownerIcon] [text userIconName],
              text commonString.action.select
            ],
            dialog [ open selectOwner, onClose $ \_ -> d CloseOwner] [
              dialogTitle_ [ text string.filterOwner.selectTitle ],
              dialogContent [className classes.ownerDialog  ] [
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
      pure {render: renderer render this, state:{selectOwner:false, userDetails: Nothing}}

  pure $ \{query,updateQuery,results} -> do 
    chipsM <- unsafeWithRef ocRef \cthis -> do
      let mkChip (UserDetails {username}) = Chip {
            label: string.filterOwner.chip <> username, 
            onDelete: affAction cthis $ eval $ OwnerSelected Nothing
          }
      (Array.fromFoldable <<< map mkChip <<< _.userDetails) <$> R.getState cthis
    pure {
      render:[
          Tuple Filters $ ownerComponent {query,updateQuery,results, innerRef: saveRef ocRef}
      ], 
      chips: fromMaybe [] chipsM
    }
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
