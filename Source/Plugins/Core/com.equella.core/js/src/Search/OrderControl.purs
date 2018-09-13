module Search.OrderControl where 

import Prelude

import Data.Argonaut (_String)
import Data.Lens (_2, _Just, preview, set)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Tuple (Tuple(..))
import OEQ.Environment (prepLangStrings)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Properties (className, mkProp, onChange)
import MaterialUI.Select (select, value)
import MaterialUI.Styles (withStyles)
import React (statelessComponent, unsafeCreateLeafElement)
import React.DOM (text)
import Search.SearchControl (Placement(..), SearchControl)
import Search.SearchQuery (QueryParam(..), _data, _params, _value, singleParam)
import OEQ.UI.Common (valueChange)

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
orderControl = let 
  _order = at "order"
  orderItem o = menuItem [mkProp "value" $ orderValue o] [ text $ orderName o ]
  in \{updateQuery, query} -> do 
    let 
      order = preview (_order <<< _Just <<< _data <<< _String) query.params
      updateOrder v = updateQuery $ set (_params <<< _order) $ Just $ singleParam v "order" v
      render {classes} = select [ 
              className classes.ordering, 
              value $ fromMaybe "relevance" order, 
              onChange $ valueChange $ updateOrder
          ] $ (orderItem <$> orderEntries)
      orderSelect = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
    pure $ { 
      render:[Tuple ResultHeader $ orderSelect {}], chips:[]
    }
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
