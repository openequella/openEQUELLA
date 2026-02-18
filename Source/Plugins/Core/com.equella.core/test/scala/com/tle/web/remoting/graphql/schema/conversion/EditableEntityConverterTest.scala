package com.tle.web.remoting.graphql.schema.conversion

import com.tle.beans.entity.Schema
import com.tle.common.EntityPack
import com.tle.web.remoting.graphql.schema.types._
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime
import java.util.Locale
import scala.jdk.CollectionConverters._

class EditableEntityConverterTest extends AnyFunSpec with Matchers with GivenWhenThen {
  import EditableEntityConverterTest._

  describe("toEntityPack") {
    it("converts an EditableEntity[MetadataSchema] to an EntityPack[Schema]") {
      Given("an EditableEntity containing a MetadataSchema with all fields populated")
      // Test data is defined in companion object

      When("toEntityPack is called with MetadataSchemaConverter.toSchema")
      val result: EntityPack[Schema] =
        EditableEntityConverter.toEntityPack[MetadataSchema, Schema](
          testEditableEntity,
          MetadataSchemaConverter.toSchema
        )

      Then("the EntityPack wrapper fields are correctly set")
      assertEntityPackWrapperFields(result)

      And("the BaseEntity fields are correctly mapped")
      assertBaseEntityFields(result.getEntity)

      And("the Schema-specific fields are correctly mapped")
      assertSchemaSpecificFields(result.getEntity)

      And("the TargetList entries are correctly mapped")
      assertTargetListEntries(result.getTargetList)
    }

    it("handles empty language bundles") {
      Given("an EditableEntity with empty language bundles")
      val entityDetailsWithEmptyBundles = createEntityDetailsWithEmptyLanguageBundles()
      val schema                        = createMinimalMetadataSchema(entityDetailsWithEmptyBundles)
      val editableEntity                = createMinimalEditableEntity(schema)

      When("toEntityPack is called")
      val result: EntityPack[Schema] =
        EditableEntityConverter.toEntityPack[MetadataSchema, Schema](
          editableEntity,
          MetadataSchemaConverter.toSchema
        )

      Then("the entity's language bundles have empty strings maps")
      val entity = result.getEntity
      entity.getName should not be null
      entity.getName.getStrings shouldBe empty
      entity.getDescription should not be null
      entity.getDescription.getStrings shouldBe empty
    }

    it("converts entity without version") {
      Given("an EditableEntity with no version specified")
      val schema         = createMinimalMetadataSchema(createMinimalEntityDetails())
      val editableEntity = createMinimalEditableEntity(schema, version = None)

      When("toEntityPack is called")
      val result: EntityPack[Schema] =
        EditableEntityConverter.toEntityPack[MetadataSchema, Schema](
          editableEntity,
          MetadataSchemaConverter.toSchema
        )

      Then("the EntityPack version is null")
      result.getVersion shouldBe null
    }

    it("handles empty target list") {
      Given("an EditableEntity with an empty target list")
      val schema         = createMinimalMetadataSchema(createMinimalEntityDetails())
      val editableEntity = createMinimalEditableEntity(schema, targetList = List.empty)

      When("toEntityPack is called")
      val result: EntityPack[Schema] =
        EditableEntityConverter.toEntityPack[MetadataSchema, Schema](
          editableEntity,
          MetadataSchemaConverter.toSchema
        )

      Then("the EntityPack has a non-null TargetList with empty entries")
      result.getTargetList should not be null
      result.getTargetList.getEntries shouldBe empty
    }

    it("handles None language bundles") {
      Given("an EntityDetails with None for language bundles")
      val entityDetailsWithNoBundles = createEntityDetailsWithNoLanguageBundles()
      val schema                     = createMinimalMetadataSchema(entityDetailsWithNoBundles)
      val editableEntity             = createMinimalEditableEntity(schema)

      When("toEntityPack is called")
      val result: EntityPack[Schema] =
        EditableEntityConverter.toEntityPack[MetadataSchema, Schema](
          editableEntity,
          MetadataSchemaConverter.toSchema
        )

      Then("the entity's name and description are null")
      val entity = result.getEntity
      entity.getName shouldBe null
      entity.getDescription shouldBe null
    }
  }

  private def assertEntityPackWrapperFields(result: EntityPack[Schema]): Unit = {
    result.getStagingID shouldBe testEditableEntity.stagingId
    result.getVersion shouldBe testEditableEntity.version.orNull
    result.getEntity should not be null
  }

  private def assertBaseEntityFields(entity: Schema): Unit = {
    entity.getId shouldBe testEntityDetails.id
    entity.getUuid shouldBe testEntityDetails.uuid
    entity.getOwner shouldBe testEntityDetails.owner
    entity.isDisabled shouldBe testEntityDetails.disabled
    entity.getAttributes.asScala shouldBe testEntityDetails.attributes

    assertLanguageBundleMatches(
      entity.getName,
      testEntityDetails.nameBundle.get,
      testNameStrings
    )
    assertLanguageBundleMatches(
      entity.getDescription,
      testEntityDetails.descriptionBundle.get,
      testDescriptionStrings
    )
  }

  private def assertLanguageBundleMatches(
      actual: com.tle.beans.entity.LanguageBundle,
      expectedBundle: LanguageBundle,
      expectedStrings: List[LanguageString]
  ): Unit = {
    actual should not be null
    actual.getId shouldBe expectedBundle.id

    val actualStrings = actual.getStrings.asScala.values.toList
    actualStrings should have size expectedStrings.size

    expectedStrings.foreach { expected =>
      val actualString = actual.getStrings.get(expected.locale)
      actualString should not be null
      actualString.getId shouldBe expected.id
      actualString.getPriority shouldBe expected.priority
      actualString.getLocale shouldBe expected.locale
      actualString.getText shouldBe expected.text
    }
  }

  private def assertSchemaSpecificFields(entity: Schema): Unit = {
    entity.getItemNamePath shouldBe testMetadataSchema.itemNamePath
    entity.getItemDescriptionPath shouldBe testMetadataSchema.itemDescriptionPath
    entity.getSerialisedDefinition shouldBe testMetadataSchema.definition

    assertTransformsMatch(
      entity.getExportTransforms.asScala.toList,
      testMetadataSchema.exportTransforms
    )
    assertTransformsMatch(
      entity.getImportTransforms.asScala.toList,
      testMetadataSchema.importTransforms
    )
    assertCitationsMatch(entity.getCitations.asScala.toList, testMetadataSchema.citations)
  }

  private def assertTransformsMatch(
      actual: List[com.tle.beans.entity.SchemaTransform],
      expected: List[MetadataSchemaTransform]
  ): Unit = {
    actual should have size expected.size
    actual.zip(expected).foreach { case (actualTransform, expectedTransform) =>
      actualTransform.getFilename shouldBe expectedTransform.filename
      actualTransform.getType shouldBe expectedTransform.schemaType
    }
  }

  private def assertCitationsMatch(
      actual: List[com.tle.beans.entity.schema.Citation],
      expected: List[Citation]
  ): Unit = {
    actual should have size expected.size
    actual.zip(expected).foreach { case (actualCitation, expectedCitation) =>
      actualCitation.getName shouldBe expectedCitation.name
      actualCitation.getTransformation shouldBe expectedCitation.transformation
    }
  }

  private def assertTargetListEntries(
      targetList: com.tle.common.security.TargetList
  ): Unit = {
    targetList should not be null

    val actualEntries = targetList.getEntries.asScala.toList
    actualEntries should have size testEditableEntity.targetList.size

    actualEntries.zip(testEditableEntity.targetList).foreach { case (actual, expected) =>
      actual.isGranted shouldBe expected.granted
      actual.isOverride shouldBe expected.overridden
      actual.getPrivilege shouldBe expected.privilege
      actual.getWho shouldBe expected.who
      actual.getPostfix shouldBe expected.postfix
    }
  }
}

object EditableEntityConverterTest {
  private val BASIC_SCHEMA                  = "<xml><item><name/><description/></item></xml>"
  private val BASIC_SCHEMA_PATH_NAME        = "/xml/item/name"
  private val BASIC_SCHEMA_PATH_DESCRIPTION = "/xml/item/description"

  // Test data - Language strings
  val testNameStrings: List[LanguageString] = List(
    LanguageString(
      id = 1L,
      priority = 1,
      locale = Locale.ENGLISH.toString,
      text = "Test Schema Name"
    ),
    LanguageString(
      id = 2L,
      priority = 2,
      locale = Locale.FRENCH.toString,
      text = "Nom du schéma de test"
    )
  )

  val testDescriptionStrings: List[LanguageString] = List(
    LanguageString(
      id = 3L,
      priority = 1,
      locale = Locale.ENGLISH.toString,
      text = "Test Schema Description"
    )
  )

  // Test data - EntityDetails
  val testEntityDetails: EntityDetails = EntityDetails(
    id = 42L,
    uuid = "test-uuid-1234",
    owner = "admin-user",
    dateCreated = Some(LocalDateTime.of(2025, 1, 15, 10, 30, 0)),
    dateModified = Some(LocalDateTime.of(2025, 2, 20, 14, 45, 30)),
    nameBundle = Some(LanguageBundle(id = 100L, strings = testNameStrings)),
    descriptionBundle = Some(LanguageBundle(id = 101L, strings = testDescriptionStrings)),
    attributes = Map("attr1" -> "value1", "attr2" -> "value2"),
    disabled = true
  )

  // Test data - Schema transforms
  val testExportTransforms: List[MetadataSchemaTransform] = List(
    MetadataSchemaTransform(filename = "export1.xsl", schemaType = "OAI_DC"),
    MetadataSchemaTransform(filename = "export2.xsl", schemaType = "HARVESTER")
  )

  val testImportTransforms: List[MetadataSchemaTransform] = List(
    MetadataSchemaTransform(filename = "import1.xsl", schemaType = "OAI_Identity")
  )

  // Test data - Citations
  val testCitations: List[Citation] = List(
    Citation(name = "APA Style", transformation = "apa.xsl"),
    Citation(name = "MLA Style", transformation = "mla.xsl")
  )

  // Test data - MetadataSchema
  val testMetadataSchema: MetadataSchema = MetadataSchema(
    details = testEntityDetails,
    exportTransforms = testExportTransforms,
    importTransforms = testImportTransforms,
    itemNamePath = BASIC_SCHEMA_PATH_NAME,
    itemDescriptionPath = BASIC_SCHEMA_PATH_DESCRIPTION,
    definition = BASIC_SCHEMA,
    citations = testCitations
  )

  // Test data - Target list entries
  val testTargetListEntries: List[TargetListEntry] = List(
    TargetListEntry(
      granted = true,
      overridden = false,
      privilege = "VIEW_SCHEMA",
      who = "U:testuser"
    ),
    TargetListEntry(
      granted = false,
      overridden = true,
      privilege = "EDIT_SCHEMA",
      who = "G:editors",
      postfix = "suffix"
    )
  )

  // Test data - EditableEntity
  val testEditableEntity: EditableEntity[MetadataSchema] = EditableEntity(
    entity = testMetadataSchema,
    stagingId = "staging-abc-123",
    version = Some("2026.1.0"),
    targetList = testTargetListEntries
  )

  // Factory methods for edge case tests
  def createMinimalEntityDetails(
      nameBundle: Option[LanguageBundle] = Some(
        LanguageBundle(id = 1L, strings = List(LanguageString(1L, 1, "en", "Test")))
      ),
      descriptionBundle: Option[LanguageBundle] = Some(
        LanguageBundle(id = 2L, strings = List(LanguageString(2L, 1, "en", "Description")))
      )
  ): EntityDetails = EntityDetails(
    id = 1L,
    uuid = "minimal-uuid",
    owner = "test-owner",
    dateCreated = None,
    dateModified = None,
    nameBundle = nameBundle,
    descriptionBundle = descriptionBundle,
    attributes = Map.empty,
    disabled = false
  )

  def createEntityDetailsWithEmptyLanguageBundles(): EntityDetails =
    createMinimalEntityDetails(
      nameBundle = Some(LanguageBundle(id = 1L, strings = List.empty)),
      descriptionBundle = Some(LanguageBundle(id = 2L, strings = List.empty))
    )

  def createEntityDetailsWithNoLanguageBundles(): EntityDetails =
    createMinimalEntityDetails(
      nameBundle = None,
      descriptionBundle = None
    )

  def createMinimalMetadataSchema(details: EntityDetails): MetadataSchema =
    MetadataSchema(
      details = details,
      exportTransforms = List.empty,
      importTransforms = List.empty,
      itemNamePath = BASIC_SCHEMA_PATH_NAME,
      itemDescriptionPath = BASIC_SCHEMA_PATH_DESCRIPTION,
      definition = BASIC_SCHEMA,
      citations = List.empty
    )

  def createMinimalEditableEntity(
      schema: MetadataSchema,
      version: Option[String] = Some("2026.1.0"),
      targetList: List[TargetListEntry] = List.empty
  ): EditableEntity[MetadataSchema] =
    EditableEntity(
      entity = schema,
      stagingId = "staging-test",
      version = version,
      targetList = targetList
    )
}
