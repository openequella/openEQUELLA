module OEQ.Search.CollectionControl where 

import Prelude

import Control.Monad.Trans.Class (lift)
import Data.Argonaut (_String, decodeJson)
import Data.Array (head)
import Data.Either (either, hush)
import Data.Lens (_Just, preview, set)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.String (joinWith)
import Data.Tuple (Tuple(..))
import Debug.Trace (spy, traceM)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, renderer)
import Effect.Uncurried (mkEffectFn2)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField)
import OEQ.API.Collection (listSearchCollections)
import OEQ.Data.SearchResults (SearchResults(..))
import OEQ.Search.SearchControl (Placement, SearchControl)
import OEQ.Search.SearchQuery (ParamDataLens, Query, _data, _params, singleParam)
import OEQ.UI.Common (valueChange)
import OEQ.UI.SearchFilters (filterSection)
import React (component, unsafeCreateLeafElement)
import React.DOM (text)

_collections :: ParamDataLens
_collections = at "collections"


filterByCollections :: Array String -> Query -> Query 
filterByCollections cols = let v = joinWith "," cols in set (_params <<< _collections) $ Just $ singleParam cols "collections" v


collectionControl :: Placement -> SearchControl
collectionControl placement = let 
  colItem {name,uuid} = menuItem {value: uuid} [ text $ name ]
  render {props:{classes,updateQuery,query}, state:{collections}} = let 
    selected = preview  (_collections <<< _Just <<< _data) query.params >>= hush <<< decodeJson >>= head
    allVal = "<ALL>"
    blankOrOne "<ALL>" = set (_params <<< _collections) Nothing
    blankOrOne a = filterByCollections [a]
    in filterSection {name: "Collection", icon:text "" } [
        textField { 
          select: true,
          value: fromMaybe allVal selected,
          label: "Filter by collection", 
          placeholder: "Select...",
          className: classes.collections, 
          onChange: valueChange (updateQuery <<< blankOrOne)
      } $ [menuItem {value:allVal} [ text "All" ]] <> (colItem <$> collections)
    ]
  collectionSelect = unsafeCreateLeafElement $ withStyles styles $ component "CollectionSelect" $ \this -> do 
    pure {render: renderer render this, state:{collections:[]}, 
        componentDidMount: affAction this $ do 
            cr <- lift $ listSearchCollections
            cr # either (traceM) \(SearchResults {results}) -> do 
               modifyState _ {collections=results}
        }
  in \{updateQuery, query} -> pure { 
    render:[Tuple placement $ collectionSelect {query, updateQuery}], 
    chips:[]
  }
  where 
  styles theme = {
    collections: {
      width: "15em"
    }
  }
