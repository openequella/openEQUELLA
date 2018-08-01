module Search.OrderControl where 

import Prelude

import Data.Lens (_2, _Just, preview, set)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Tuple (Tuple(..))
import EQUELLA.Environment (prepLangStrings)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Properties (mkProp, onChange)
import MaterialUI.Select (select, value)
import React.DOM (text)
import Search.SearchControl (Placement(..), SearchControl)
import Search.SearchQuery (QueryParam(..), _Param, _params, _value, singleParam)
import Utils.UI (valueChange)

data Order = Relevance | DateModified | Name | Rating | DateCreated

orderValue :: Order -> String
orderValue = case _ of 
  Relevance -> "relevance"
  Name -> "name"
  DateModified -> "modified"
  DateCreated -> "created"
  Rating -> "rating"

orderEntries :: Array Order
orderEntries = [Relevance, Name, DateModified, DateCreated ]

orderName :: Order -> String
orderName = case _ of 
    Relevance -> orderString.relevance
    Name -> orderString.name
    DateModified -> orderString.datemodified
    DateCreated -> orderString.datecreated
    Rating -> orderString.rating

orderControl :: SearchControl
orderControl {updateQuery, query} = let 
  _order = at "order"
  order = preview (_order <<< _Just <<< _value <<< _Param <<< _2) query.params
  orderItem o = menuItem [mkProp "value" $ orderValue o] [ text $ orderName o ]
  in pure $ {render:[
    Tuple Filters  $
    select [ 
        -- className classes.ordering, 
        value $ fromMaybe "relevance" order, 
        onChange $ valueChange $ \v -> updateQuery $ set (_params <<< _order) $ Just $ singleParam "order" v
    ] $ (orderItem <$> orderEntries)
  ], chips:[]}

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
