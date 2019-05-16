module OEQ.MainUI.Template where

import Prelude

import Data.Nullable (Nullable)
import Data.TSCompat.React (unsafeCreateElement)
import Effect (Effect)
import Effect.Uncurried (EffectFn1)
import OEQ.Data.Error (ErrorResponse)
import OEQ.MainUI.Routes (Route)
import OEQ.Utils.Interop (nullAny)
import React (ReactClass, ReactElement, ReactThis)


type RenderData = {
  baseResources::String, 
  newUI::Boolean
}

foreign import renderData :: RenderData

newtype TemplateRef = TemplateRef (ReactThis {|TemplateProps} Unit)

type TemplateProps = (
  title::String, 
  fixedViewPort :: Nullable Boolean, -- Fix the height of the main content, otherwise use min-height
  preventNavigation :: Nullable Boolean, -- Prevent navigation away from this page (e.g. Unsaved data) 
  titleExtra :: Nullable ReactElement, -- Extra part of the App bar (e.g. Search control)
  menuExtra :: Nullable (Array ReactElement), -- Extra menu options
  tabs :: Nullable ReactElement, -- Additional markup for displaying tabs which integrate with the App bar
  footer :: Nullable ReactElement, -- Markup to show at the bottom of the main area. E.g. save/cancel options
  backRoute :: Nullable Route, -- An optional Route for showing a back icon button
  menuMode :: String,
  fullscreenMode :: String,
  hideAppBar :: Boolean, 
  disableNotifications :: Boolean,
  innerRef :: Nullable (EffectFn1 (Nullable TemplateRef) Unit),
  errorResponse :: Nullable ErrorResponse
)

foreign import templateClass :: ReactClass {|TemplateProps}

template :: String -> Array ReactElement -> ReactElement
template title = template' $ templateDefaults title

template' :: {|TemplateProps} -> Array ReactElement -> ReactElement
template' = unsafeCreateElement templateClass

templateDefaults ::  String ->  {|TemplateProps} 
templateDefaults title = {
    title,
    titleExtra:nullAny, 
    fixedViewPort:nullAny,
    preventNavigation:nullAny, 
    menuExtra:nullAny, 
    disableNotifications: false,
    tabs:nullAny, 
    backRoute: nullAny, 
    footer: nullAny, 
    menuMode:"", 
    fullscreenMode:"", 
    hideAppBar: false, 
    innerRef:nullAny, 
    errorResponse: nullAny
}


refreshUser :: TemplateRef -> Effect Unit 
refreshUser (TemplateRef r) = pure unit

