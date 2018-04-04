module DragNDrop.Beautiful where 

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.IOEffFn (IOFn1(..))
import Data.Function.Uncurried (mkFn2)
import Data.Nullable (Nullable)
import React (ReactClass, ReactElement, Ref, createElement)
import React.DOM.Props (Props)
import Unsafe.Coerce (unsafeCoerce)

foreign import dragDropContextClass :: forall a. ReactClass a 

foreign import draggableClass :: forall a. ReactClass a

foreign import droppableClass :: forall a. ReactClass a

type DroppableProvided = {
    innerRef :: String,
    droppableProps :: Props,
    placeholder :: ReactElement
}
type DroppableStateSnapshot = {

}

type DraggableProvided = {
    innerRef :: String,
    draggableProps :: Props,
    dragHandleProps :: Props,
    placeholder :: ReactElement
}
type DraggableStateSnapshot = {

}

type DropResult = {
  draggableId :: String,
  type :: String,
  source :: {droppableId::String, index::Int},
  destination :: Nullable {droppableId::String, index::Int},
  reason :: String 
}

dragDropContext :: {onDragEnd :: IOFn1 DropResult Unit} -> Array ReactElement -> ReactElement
dragDropContext = createElement dragDropContextClass

droppable :: forall r. {droppableId::String| r} -> (DroppableProvided -> DroppableStateSnapshot -> ReactElement) -> ReactElement
droppable a c = createElement droppableClass a (unsafeCoerce $ mkFn2 c)

draggable :: forall r. {draggableId::String, index::Int | r} -> (DraggableProvided -> DraggableStateSnapshot -> ReactElement) -> ReactElement
draggable a c = createElement draggableClass a (unsafeCoerce $ mkFn2 c)