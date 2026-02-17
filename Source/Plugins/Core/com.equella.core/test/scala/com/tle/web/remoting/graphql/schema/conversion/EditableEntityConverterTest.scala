package com.tle.web.remoting.graphql.schema.conversion

import com.tle.beans.entity.Schema
import com.tle.common.EntityPack
import com.tle.web.remoting.graphql.schema.types._
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime
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
  // Test data - Language strings
  val testNameStrings: List[LanguageString] = List(
    LanguageString(id = 1L, priority = 1, locale = "en", text = "Test Schema Name"),
    LanguageString(id = 2L, priority = 2, locale = "fr", text = "Nom du schéma de test")
  )

  val testDescriptionStrings: List[LanguageString] = List(
    LanguageString(id = 3L, priority = 1, locale = "en", text = "Test Schema Description")
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
    itemNamePath = "/xml/item/name",
    itemDescriptionPath = "/xml/item/description",
    definition = "<root><item><name/><description/></item></root>",
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
}
