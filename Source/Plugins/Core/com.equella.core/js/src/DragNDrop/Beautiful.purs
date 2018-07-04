module DragNDrop.Beautiful where 

import Prelude

import Data.Function.Uncurried (mkFn2)
import Data.Nullable (Nullable)
import Effect.Uncurried (EffectFn1)
import React (ReactClass, ReactElement, ReactRef, unsafeCreateElement)
import React.DOM.Props (Props)
import Unsafe.Coerce (unsafeCoerce)

foreign import dragDropContextClass :: forall a. ReactClass a 

foreign import draggableClass :: forall a. ReactClass a

foreign import droppableClass :: forall a. ReactClass a

type DroppableProvided = {
    innerRef :: EffectFn1 (Nullable ReactRef) Unit,
    droppableProps :: Props,
    placeholder :: ReactElement
}
type DroppableStateSnapshot = {
    isDraggingOver :: Boolean
}

type DraggableProvided = {
    innerRef :: EffectFn1 (Nullable ReactRef) Unit,
    draggableProps :: Props,
    dragHandleProps :: Props,
    placeholder :: ReactElement
}
type DraggableStateSnapshot = {
    isDragging :: Boolean
}

type DropResult = {
  draggableId :: String,
  type :: String,
  source :: {droppableId::String, index::Int},
  destination :: Nullable {droppableId::String, index::Int},
  reason :: String 
}

dragDropContext :: {onDragEnd :: EffectFn1 DropResult Unit} -> Array ReactElement -> ReactElement
dragDropContext = unsafeCreateElement dragDropContextClass

droppable :: forall r. {droppableId::String| r} -> (DroppableProvided -> DroppableStateSnapshot -> ReactElement) -> ReactElement
droppable a c = unsafeCreateElement droppableClass a (unsafeCoerce $ mkFn2 c)

draggable :: forall r. {draggableId::String, index::Int | r} -> (DraggableProvided -> DraggableStateSnapshot -> ReactElement) -> ReactElement
draggable a c = unsafeCreateElement draggableClass a (unsafeCoerce $ mkFn2 c)