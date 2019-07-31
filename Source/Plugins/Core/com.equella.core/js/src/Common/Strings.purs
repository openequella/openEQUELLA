module Common.Strings where 

foreign import languageStrings :: { cp :: { title :: String
        , cloudprovideravailable :: { zero :: String
                                    , one :: String
                                    , more :: String
                                    }
        , newcloudprovider :: { title :: String
                              , label :: String
                              , text :: String
                              , help :: String
                              }
        , deletecloudprovider :: { title :: String
                                 , message :: String
                                 }
        }
, courseedit :: { title :: String
                , newtitle :: String
                , tab :: String
                , name :: { label :: String
                          , help :: String
                          }
                , description :: { label :: String
                                 , help :: String
                                 }
                , code :: { label :: String
                          , help :: String
                          }
                , "type" :: { label :: String
                            , i :: String
                            , e :: String
                            , s :: String
                            }
                , department :: { label :: String
                                }
                , citation :: { label :: String
                              }
                , startdate :: { label :: String
                               }
                , enddate :: { label :: String
                             }
                , version :: { label :: String
                             , default :: String
                             , forcecurrent :: String
                             , forcelatest :: String
                             , defaultcurrent :: String
                             , defaultlatest :: String
                             , help :: String
                             }
                , students :: { label :: String
                              }
                , archived :: { label :: String
                              }
                , saved :: String
                , errored :: String
                }
, courses :: { title :: String
             , sure :: String
             , confirmDelete :: String
             , coursesAvailable :: { zero :: String
                                   , one :: String
                                   , more :: String
                                   }
             , includeArchived :: String
             , archived :: String
             }
, entity :: { edit :: { tab :: { permissions :: String
                               }
                      }
            }
, loginnoticepage :: { title :: String
                     , clear :: { title :: String
                                , confirm :: String
                                }
                     , prelogin :: { label :: String
                                   }
                     , postlogin :: { label :: String
                                    , description :: String
                                    }
                     , notifications :: { saved :: String
                                        , cleared :: String
                                        , cancelled :: String
                                        }
                     , errors :: { permissions :: String
                                 }
                     , scheduling :: { title :: String
                                     , start :: String
                                     , end :: String
                                     , scheduled :: String
                                     , alwayson :: String
                                     , disabled :: String
                                     , endbeforestart :: String
                                     , expired :: String
                                     }
                     }
, template :: { navaway :: { title :: String
                           , content :: String
                           }
              , menu :: { title :: String
                        , logout :: String
                        , prefs :: String
                        }
              }
, "com.equella.core" :: { title :: String
                        , windowtitlepostfix :: String
                        , topbar :: { link :: { notifications :: String
                                              , tasks :: String
                                              }
                                    }
                        }
, searchconfigs :: { title :: String
                   , configsAvailable :: { zero :: String
                                         , one :: String
                                         , more :: String
                                         }
                   }
, newuisettings :: { title :: String
                   , colourschemesettings :: { title :: String
                                             , primarycolour :: String
                                             , menubackgroundcolour :: String
                                             , backgroundcolour :: String
                                             , secondarycolour :: String
                                             , sidebartextcolour :: String
                                             , primarytextcolour :: String
                                             , secondarytextcolour :: String
                                             , sidebariconcolour :: String
                                             }
                   , logosettings :: { title :: String
                                     , imagespeclabel :: String
                                     , current :: String
                                     , nofileselected :: String
                                     }
                   , errors :: { invalidimagetitle :: String
                               , invalidimagedescription :: String
                               , nofiledescription :: String
                               , permissiontitle :: String
                               , permissiondescription :: String
                               }
                   }
, common :: { action :: { save :: String
                        , cancel :: String
                        , undo :: String
                        , add :: String
                        , ok :: String
                        , discard :: String
                        , select :: String
                        , delete :: String
                        , search :: String
                        , clear :: String
                        , close :: String
                        , dismiss :: String
                        , browse :: String
                        , apply :: String
                        , resettodefault :: String
                        , revertchanges :: String
                        , register :: String
                        }
            , users :: String
            , groups :: String
            , roles :: String
            }
, searchpage :: { resultsAvailable :: String
                , refineTitle :: String
                , modifiedDate :: String
                , order :: { relevance :: String
                           , name :: String
                           , datemodified :: String
                           , datecreated :: String
                           , rating :: String
                           }
                , filterOwner :: { title :: String
                                 , chip :: String
                                 , selectTitle :: String
                                 }
                , filterLast :: { label :: String
                                , chip :: String
                                , name :: String
                                , none :: String
                                , month :: String
                                , year :: String
                                , fiveyear :: String
                                , week :: String
                                , day :: String
                                }
                }
, "com.equella.core.searching.search" :: { title :: String
                                         }
, "com.equella.core.comments" :: { anonymous :: String
                                 , commentmsg :: String
                                 , entermsg :: String
                                 }
, uiconfig :: { facet :: { name :: String
                         , path :: String
                         , title :: String
                         }
              , enableNew :: String
              , enableSearch :: String
              , themeSettingsButton :: String
              }
, settings :: { general :: { name :: String
                           , desc :: String
                           }
              , integration :: { name :: String
                               , desc :: String
                               }
              , diagnostics :: { name :: String
                               , desc :: String
                               }
              , ui :: { name :: String
                      , desc :: String
                      }
              }
, aclterms :: { title :: { ugr :: String
                         , ip :: String
                         , referrer :: String
                         , token :: String
                         }
              }
, acleditor :: { privilege :: String
               , privileges :: String
               , selectpriv :: String
               , expression :: String
               , privplaceholder :: String
               , dropplaceholder :: String
               , addpriv :: String
               , addexpression :: String
               , targets :: String
               , new :: { ugr :: String
                        , ip :: String
                        , referrer :: String
                        , token :: String
                        }
               , notted :: String
               , not :: String
               , override :: String
               , revoked :: String
               , revoke :: String
               , required :: String
               , match :: { and :: String
                          , or :: String
                          , notand :: String
                          , notor :: String
                          }
               , convertGroup :: String
               }
}
