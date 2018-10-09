module OEQ.Search.OrderControl where 

import Prelude

import Data.Argonaut (_String)
import Data.Lens (Lens', _Just, preview, set)
import Data.Lens.At (at)
import Data.Map (Map)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Tuple (Tuple(..))
import Effect.Uncurried (mkEffectFn2, runEffectFn1)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import OEQ.Data.Searches (Order(..), SortConfig, orderValue)
import OEQ.Environment (prepLangStrings)
import OEQ.Search.SearchControl (Placement(..), SearchControl)
import OEQ.Search.SearchQuery (ParamDataLens, Query, _data, _params, singleParam)
import OEQ.UI.Common (valueChange)
import React (statelessComponent, unsafeCreateLeafElement)
import React.DOM (text)

orderEntries :: Array Order
orderEntries = [Relevance, Name, DateModified, DateCreated ]

orderName :: Order -> String
orderName = case _ of 
    Relevance -> orderString.relevance
    Name -> orderString.name
    DateModified -> orderString.datemodified
    DateCreated -> orderString.datecreated
    Rating -> orderString.rating

_order :: ParamDataLens
_order = at "order"

setSort :: Order -> Query -> Query
setSort = setSortStr <<< orderValue

setSortStr :: String -> Query -> Query
setSortStr v = set (_params <<< _order) $ Just $ singleParam v "order" v

orderControl :: Placement -> SearchControl
orderControl placement = let 
  orderItem o = menuItem {value: orderValue o} [ text $ orderName o ]
  render {classes,updateQuery,query} = let 
    order = preview (_order <<< _Just <<< _data <<< _String) query.params
    updateOrder v = updateQuery $ setSortStr v
    in select { 
          className: classes.ordering, 
          value: fromMaybe "relevance" order, 
          onChange: mkEffectFn2 \e _ -> runEffectFn1 (valueChange updateOrder) e
      } $ (orderItem <$> orderEntries)
  orderSelect = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
  renderer {updateQuery, query} = pure { 
    render:[Tuple placement $ orderSelect {query, updateQuery}], 
    chips:[]
  }
  in renderer
  where 
  styles theme = {
    ordering: {
      width: "10em"
    }
  }

rawStrings :: { prefix :: String
, strings :: { order :: { relevance :: String
                        , name :: String
                        , datemodified :: String
                        , datecreated :: String
                        , rating :: String
                        }
             }
}
rawStrings = {prefix: "searchpage", 
  strings: {
    order: {
      relevance: "Relevance",
      name: "Name",
      datemodified: "Date modifed",
      datecreated: "Date created",
      rating: "Rating"
    }
  }
}

orderString :: { relevance :: String
, name :: String
, datemodified :: String
, datecreated :: String
, rating :: String
}
orderString = (prepLangStrings rawStrings).order 
