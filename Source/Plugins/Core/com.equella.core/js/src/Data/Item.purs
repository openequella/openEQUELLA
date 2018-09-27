module OEQ.Data.Item where 

import Prelude

import Data.Argonaut (class DecodeJson, class EncodeJson, Json, decodeJson, getField, jsonEmptyObject, (.?), (.??), (:=), (~>))
import Data.Either (Either(..))
import Data.Maybe (Maybe, fromMaybe)
import Data.Traversable (traverse)
import Foreign.Object (Object)

data ItemRef = ItemRef String Int 

instance irDec :: DecodeJson ItemRef where 
  decodeJson = decodeJson >=> (\o -> ItemRef <$> o .? "uuid" <*>  o .? "version")

instance irEnc :: EncodeJson ItemRef where 
  encodeJson (ItemRef uuid version) = "uuid" := uuid ~> "version" := version ~> jsonEmptyObject

data MetaType = Text | HTML | Date | URL

type MetaDisplay = {
  title :: String,
  value :: String,
  fullWidth :: Boolean,
  metaType :: MetaType
}

data AttachmentNode = Attachment AttachmentView (Array AttachmentNode)

type AttachmentView = {
  title:: String,
  uuid :: String, 
  href :: String,
  thumbnailHref :: String, 
  viewers :: Object String,
  details :: Array MetaDisplay
}

type ItemComment = {
  uuid :: String,
  comment :: String, 
  rating :: Number, 
  date :: String,
  anonymous :: Boolean,
  user :: Maybe String
}

data ItemSummarySection = 
      BasicDetails {title::String, description::Maybe String} 
    | DisplayNodes {sectionTitle::String, meta::Array MetaDisplay}
    | Attachments {sectionTitle::String, attachments::Array AttachmentNode}
    | HtmlSummarySection {sectionTitle::String, showTitle:: Boolean, html::String}
    | CommentsSummarySection {sectionTitle::String, canAdd::Boolean, 
          canDelete::Boolean, anonymousOnly :: Boolean, hideUsername :: Boolean, allowAnonymous :: Boolean }

type CopyrightAttachment = {
  item :: ItemRef, 
  href :: Maybe String, 
  title :: String, 
  uuid :: String, 
  status :: String
}
type JournalSection = {
  attachment :: CopyrightAttachment
}

type JournalPortion = {
  title :: String, 
  sections :: Array JournalSection
}

type BookSection = {
  attachment :: CopyrightAttachment, 
  range :: String, 
  pageCount :: Int, 
  illustration :: Boolean
}

type BookChapter = {
  title :: String, 
  chapterName :: String, 
  canActivate :: Boolean, 
  sections :: Array BookSection
}

data HoldingSummary = 
    BookSummary {totalPages:: Int, chapters:: Array BookChapter } 
  | JournalSummary { volume :: Maybe String, issueNumber:: Maybe String, portions:: Array JournalPortion}

type CopyrightSummary = {
  holdingItem :: ItemRef, 
  holding :: HoldingSummary
}

type ItemSummary = {
  title :: String,
  sections :: Array ItemSummarySection, 
  copyright :: Maybe CopyrightSummary
}

decodeBasic :: Object Json -> Either String ItemSummarySection
decodeBasic o = do 
  title <- o .? "title"
  description <- o .?? "description"
  pure $ BasicDetails {title,description}

decodeMeta :: Object Json -> Either String MetaDisplay
decodeMeta o = do 
  title <- o .? "title"
  value <- o .? "value"
  fullWidth <- o .? "fullWidth"
  metaType <- o .? "type" >>= case _ of 
    "text" -> pure Text 
    "html" -> pure HTML 
    "date" -> pure Date 
    "url" -> pure URL
    nd -> Left $ "Unknown display node type: '" <> nd <> "'"
  pure {title,value,fullWidth,metaType}

decodeAttachmentView :: Object Json -> Either String AttachmentView
decodeAttachmentView o = do 
  title <- o .? "title"
  uuid <- o .? "uuid"
  href <- o .? "href"
  thumbnailHref <- o .? "thumbnailHref"
  viewers <- o .? "viewers"
  details <- o .? "details" >>= traverse decodeMeta
  pure {title,uuid,href,thumbnailHref,viewers,details}

decodeAttachment :: Object Json -> Either String AttachmentNode
decodeAttachment o = do 
  view <- decodeAttachmentView o 
  pure $ Attachment view []

decodeAttachments :: Object Json -> Either String ItemSummarySection
decodeAttachments o = do 
  sectionTitle <- o .? "sectionTitle"
  attachments <- o .? "attachments" >>= traverse decodeAttachment
  pure $ Attachments {sectionTitle, attachments}

decodeDisplayNodes :: Object Json -> Either String ItemSummarySection
decodeDisplayNodes o = do 
  sectionTitle <- o .? "sectionTitle"
  meta <- o .? "meta" >>= traverse decodeMeta
  pure $ DisplayNodes {meta,sectionTitle}

decodeHtmlSection :: Object Json -> Either String ItemSummarySection
decodeHtmlSection o = do 
  sectionTitle <- o .? "sectionTitle"
  html <- o .? "html"
  showTitle <- o .? "showTitle"
  pure $ HtmlSummarySection {sectionTitle,showTitle,html}

decodeComment :: Json -> Either String ItemComment 
decodeComment v = do 
  o <- decodeJson v
  uuid <- o .? "uuid"
  anonymous <- o .? "anonymous"
  comment <- o .?? "comment"
  rating <- o .? "rating"
  date <- o .? "postedDate"
  user <- o .?? "postedBy" >>= traverse (flip getField "id")
  pure {uuid,comment: fromMaybe "" comment,rating,date,user,anonymous}

decodeCommentsSection :: Object Json -> Either String ItemSummarySection
decodeCommentsSection o = do 
  sectionTitle <- o .? "sectionTitle"
  canAdd <- o .? "canAdd"
  canDelete <- o .? "canDelete"
  anonymousOnly <- o .? "anonymousOnly"
  hideUsername <- o .? "hideUsername"
  allowAnonymous <- o .? "allowAnonymous"
  pure $ CommentsSummarySection {sectionTitle,canAdd,canDelete,anonymousOnly,hideUsername,allowAnonymous}

decodeSection :: Json -> Either String ItemSummarySection
decodeSection v = do 
  o <- decodeJson v
  t <- o .? "type" >>= case _ of 
          "basic" -> decodeBasic o 
          "displayNodes" -> decodeDisplayNodes o 
          "attachments" -> decodeAttachments o
          "html" -> decodeHtmlSection o
          "comments" -> decodeCommentsSection o 
          st -> Left $ "Unknown section type '" <> st <> "'"
  pure t

decodeItemSummary :: Json -> Either String ItemSummary 
decodeItemSummary v = do 
  o <- decodeJson v
  title <- o .? "title"
  sections <- o .? "sections" >>= traverse decodeSection
  copyright <- o .?? "copyright" >>= traverse decodeCopyrightSummary
  pure {title, sections, copyright}

decodeCopyrightAttachment :: Object Json -> Either String CopyrightAttachment
decodeCopyrightAttachment o = do 
  item <- o .? "item"
  title <- o .? "title"
  href <- o .?? "href"
  status <- o .? "status"
  uuid <- o .? "uuid"
  pure {item, title, href, status, uuid}

decodeBookChapter :: Object Json -> Either String BookChapter
decodeBookChapter o = do 
  title <- o .? "title"
  chapterName <- o .? "chapterName"
  canActivate <- o .? "canActivate"
  sections <- o .? "sections" >>= traverse decodeBookSection
  pure {title,chapterName,canActivate,sections}
  where 
  decodeBookSection b = do 
    attachment <- b .? "attachment" >>= decodeCopyrightAttachment
    illustration <- b .? "illustration" 
    pageCount <- b .? "pageCount" 
    range <- b .? "range"
    pure {attachment,illustration,pageCount,range}

decodeJournalSection :: Object Json -> Either String JournalSection
decodeJournalSection s = {attachment: _} <$> (s .? "attachment" >>= decodeCopyrightAttachment)

decodeJournalPortion :: Object Json -> Either String JournalPortion
decodeJournalPortion o = do 
  title <- o .? "title"
  sections <- o .? "sections" >>= traverse decodeJournalSection
  pure {title,sections}

decodeHolding :: Object Json -> Either String HoldingSummary 
decodeHolding o = o .? "type" >>= case _ of 
  "book" -> do 
    totalPages <- o .? "totalPages"
    chapters <- o .? "chapters" >>= traverse decodeBookChapter
    pure $ BookSummary {totalPages,chapters}
  "journal" -> do 
    volume <- o .?? "volume"
    issueNumber <- o .?? "issueNumber"
    portions <- o .? "portions" >>= traverse decodeJournalPortion
    pure $ JournalSummary {volume, issueNumber, portions}
  t -> Left $ "Unknown holding type: " <> t

decodeCopyrightSummary :: Json -> Either String CopyrightSummary
decodeCopyrightSummary v = do 
  o <- decodeJson v
  holdingItem <- o .? "holdingItem"
  holding <- o .? "holding" >>= decodeHolding
  pure $ {holdingItem,holding}

