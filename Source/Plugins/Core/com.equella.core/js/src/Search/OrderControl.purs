module OEQ.Search.OrderControl where 

import Prelude

import Data.Argonaut (_String)
import Data.Lens (_Just, preview, set)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Tuple (Tuple(..))
import Effect.Uncurried (mkEffectFn2, runEffectFn1)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import OEQ.Environment (prepLangStrings)
import OEQ.UI.Common (valueChange)
import React (statelessComponent, unsafeCreateLeafElement)
import React.DOM (text)
import OEQ.Search.SearchControl (Placement(..), SearchControl)
import OEQ.Search.SearchQuery (_data, _params, singleParam)

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
  orderItem o = menuItem {value: orderValue o} [ text $ orderName o ]
  renderer {updateQuery, query} = do 
    let 
      order = preview (_order <<< _Just <<< _data <<< _String) query.params
      updateOrder v = updateQuery $ set (_params <<< _order) $ Just $ singleParam v "order" v
      render {classes} = select { 
              className: classes.ordering, 
              value: fromMaybe "relevance" order, 
              onChange: mkEffectFn2 \e _ -> runEffectFn1 (valueChange updateOrder) e
          } $ (orderItem <$> orderEntries)
      orderSelect = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
    pure $ { 
      render:[Tuple ResultHeader $ orderSelect {}], chips:[]
    }
  in {renderer, initQuery: identity}
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
