/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.graphql.api

import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.test.TestHelper
import io.github.openequella.graphql.test.TestHelper.specialCharacters
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{BeforeAndAfter, EitherValues, GivenWhenThen}

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer

class TleGroupApiTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfter
    with GivenWhenThen
    with EitherValues {
  // Used to force pagination processing
  private val SMALL_PAGE_SIZE = 2
  // To get as much in one go as possible - ideally skipping pagination
  private val LARGE_PAGE_SIZE = 100

  // A known group in the Rest institution to test with. The values are those found in the
  // institution export - and imported at test time.
  private val knownGroup: TleGroupView = TleGroupView(
    uniqueId = "d72eb802-0ea6-4384-907a-341ee60628c0",
    parentId = None,
    name = "AutoGroup1",
    description = None,
    hasGroups = false,
    hasUsers = true
  )

  private implicit val cfg: ClientConfiguration = TestHelper.loginToRestInstitution()

  // Keep track of groups created during the tests so they can be cleaned up
  // at the end via `after`.
  private val createdGroups = new ListBuffer[String]

  after {
    cleanupGroups()
  }

  describe("getByName") {
    it("should be able to retrieve a known group by name") {
      val g = TleGroupApi.getByName(knownGroup.name)
      g shouldBe Right(Some(knownGroup))
    }

    it("should return None for an unknown group") {
      val g = TleGroupApi.getByName("unknown")
      g shouldBe Right(None)
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.getByName(knownGroup.name)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getByUniqueId") {
    it("should be able to retrieve a known group by uniqueId") {
      val g = TleGroupApi.getByUniqueId(knownGroup.uniqueId)
      g shouldBe Right(Some(knownGroup))
    }

    it("should return None for an unknown group") {
      val g = TleGroupApi.getByUniqueId("unknown")
      g shouldBe Right(None)
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.getByUniqueId(knownGroup.uniqueId)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("getGroupsByIds") {
    val unknownGroupId = "unknown"

    it("should be able to retrieve known groups by uniqueId") {
      val groups = getGroupsByIds(Set(knownGroup.uniqueId))
      groups shouldBe List(knownGroup)
    }

    it("should silently ignore unknown group Ids") {
      When("A known and unknown group are requested")
      val groups = getGroupsByIds(Set(knownGroup.uniqueId, unknownGroupId))

      Then("Only the known group should be returned")
      groups shouldBe List(knownGroup)
    }

    it("should be able to retrieve multiple known groups by uniqueId") {
      val groupIdSelectGroup0    = "276eaccc-59bf-4ac0-a50b-a007e0390ccd"
      val groupIdSelectSubGroup1 = "f975419d-4d58-4848-b3a5-fd85d1d08c41"
      val groupIds = Set(knownGroup.uniqueId, groupIdSelectGroup0, groupIdSelectSubGroup1)

      val groups = getGroupsByIds(groupIds, SMALL_PAGE_SIZE)
      groups.map(_.uniqueId).sorted shouldBe groupIds.toList.sorted
    }

    it("should return an empty set for unknown groups") {
      val groups = getGroupsByIds(Set(unknownGroupId))
      groups shouldBe List()
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.getGroupsByIds(ForwardPagination(LARGE_PAGE_SIZE), Set(knownGroup.uniqueId))(
          unauthenticated
        )
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("createGroup") {
    it("should be able to create a group") {
      val groupName = "createGroupTest"
      val response  = addGroup(groupName)
      response shouldBe a[Right[_, _]]
      TleGroupApi.getByName(groupName).value match {
        case Some(newGroup) => newGroup.name shouldBe groupName
        case None           => fail("Group not found")
      }
    }

    it("should be able to create a group with a parent") {
      Given("A parent group with a single child group")
      val parentGroupName = "createGroupTestParent"
      val childGroupName  = "createGroupTestChild"
      val newGroupIds     = for {
        parentGroup <- addGroup(parentGroupName)
        childGroup  <- addGroup(childGroupName, Some(parentGroup.uniqueId))
      } yield (parentGroup.uniqueId, childGroup.uniqueId)
      val (parentId, childId) = newGroupIds.value

      When("The child group is retrieved")
      val childGroup = TleGroupApi.getByUniqueId(childId).value match {
        case Some(group) => group
        case None        => fail("Child group not found")
      }

      And("All groups in the parent group are retrieved")
      val childGroups = getAllGroupsInGroup(Some(parentId))

      Then("The child group should have the correct parent ID and be in the list of groups")
      childGroup.parentId shouldBe Some(parentId)
      childGroups should contain(childGroup)
    }

    it("handles the creation of users with special characters in their names") {
      forAll(specialCharacters) { specialChar =>
        val groupName = s"createGroupSpecialCharacterTest$specialChar"
        val response  = addGroup(groupName)
        response shouldBe a[Right[_, _]]
        TleGroupApi.getByName(groupName).value match {
          case Some(newGroup) => newGroup.name shouldBe groupName
          case None           => fail("Group not found")
        }
      }
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.createGroup("createGroupTest")(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("deleteGroup") {
    it("should delete a known group and all its children") {
      val groupName = "deleteGroupTest"
      val childName = s"$groupName-child"

      val response = for {
        group <- addGroup(groupName)
        _     <- addGroup(childName, Some(group.uniqueId))
        uniqueId = group.uniqueId
      } yield TleGroupApi.deleteGroup(uniqueId)

      response shouldBe a[Right[_, _]]
      TleGroupApi.getByName(groupName).value shouldBe None
      TleGroupApi.getByName(childName).value shouldBe None
    }

    it("should delete a known group without deleting its children") {
      val groupName = "deleteGroupTest"
      val childName = s"$groupName-child"

      val response = for {
        group <- addGroup(groupName)
        _     <- addGroup(childName, Some(group.uniqueId))
        uniqueId = group.uniqueId
      } yield TleGroupApi.deleteGroupOnly(uniqueId)

      response shouldBe a[Right[_, _]]
      TleGroupApi.getByName(groupName).value shouldBe None
      TleGroupApi.getByName(childName).value should not be None
    }

    it("should return None for an unknown group") {
      val response = TleGroupApi.deleteGroup("no such group")
      TestHelper.checkApiError(response) shouldBe a[NotFoundError]
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.deleteGroupOnly(knownGroup.uniqueId)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("updateGroup") {
    it("should be able to update a group's name and description") {
      val groupName    = "updateGroupTest"
      val newGroupName = "newName"
      val newGroupDesc = "newDescription"
      val blankGroup   = TleGroupView(
        uniqueId = "",
        parentId = None,
        name = "",
        description = None,
        hasGroups = false,
        hasUsers = false
      )

      Given("A new group is created and subsequently its name is changed")
      val updatedGroupId = for {
        group <- addGroup(groupName)
        _     <- TleGroupApi.updateGroup(group.uniqueId, Some(newGroupName), Some(newGroupDesc))
      } yield group.uniqueId

      When("The updated group is retrieved")
      val updatedGroup = TleGroupApi.getByUniqueId(updatedGroupId.value).value match {
        case Some(group) => group
        case None        => fail("Updated group not found")
      }

      Then("The group should have the new name")
      updatedGroup shouldBe blankGroup.copy(
        uniqueId = updatedGroupId.value,
        name = newGroupName,
        description = Some(newGroupDesc)
      )
    }

    it("should be possible to change the parent group of a group") {
      val parentGroupName = "updateGroupTestParent"
      val childGroupName  = "updateGroupTestChild"

      Given("A pair of groups are created")
      val groupIds = for {
        parentGroup <- addGroup(parentGroupName)
        childGroup  <- addGroup(childGroupName)
      } yield (parentGroup.uniqueId, childGroup.uniqueId)
      val (parentGroupId, childGroupId) = groupIds.value

      And("One of the pair is moved to be the child of the other")
      setGroupParent(childGroupId, parentGroupId) shouldBe a[Right[_, _]]

      When("The child group is retrieved")
      val childGroup = TleGroupApi.getByUniqueId(childGroupId).value match {
        case Some(group) => group
        case None        => fail("Child group not found")
      }

      And("All groups in the parent group are retrieved")
      val childGroups = getAllGroupsInGroup(Some(parentGroupId))

      Then("The child group should have the correct parent ID and be in the list of groups")
      childGroup.parentId shouldBe Some(parentGroupId)
      childGroups.map(_.uniqueId) should contain(childGroupId)
    }

    it("should be possible to modify the users in a group") {
      val userModifications = Table(
        ("description", "startList", "updatedList"),
        ("add a user", List("user1"), List("user1", "user2")),
        ("remove a user", List("user1", "user2"), List("user1")),
        ("replace all users", List("user1", "user2"), List("user3", "user4")),
        ("no change", List("user1", "user2"), List("user1", "user2")),
        ("add and remove", List("user1", "user2"), List("user2", "user3")),
        ("removal of all users", List("user1", "user2"), List())
      )
      val groupName = "updateGroupTestUsers"
      val counter   = new AtomicInteger(0)

      forAll(userModifications) { (_, startList, updatedList) =>
        val groupId = for {
          group <- addGroup(groupName + counter.getAndIncrement)
          _     <- setGroupUsers(group.uniqueId, startList)
          _     <- setGroupUsers(group.uniqueId, updatedList)
        } yield group.uniqueId

        val groupUsers = getAllUsersInGroup(groupId.value)
        groupUsers shouldBe updatedList
      }
    }

    it("can handle adding users to a group with special characters in their names") {
      val groupName = "updateGroupSpecialCharacterTest"
      val users     = specialCharacters.toList.map { special =>
        s"user${special}character"
      }
      val groupId = for {
        group <- addGroup(groupName)
        _     <- setGroupUsers(group.uniqueId, users)
      } yield group.uniqueId

      val groupUsers = getAllUsersInGroup(groupId.value)
      groupUsers.sorted shouldBe users.sorted
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.updateGroup(knownGroup.uniqueId, Some("newName"))(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("listGroups") {
    // There are this many groups in the target institution
    val TOTAL_GROUPS = 4

    it("should be able to list all groups") {
      val response = getAllGroupsInGroup(None, SMALL_PAGE_SIZE)
      response should not be empty
    }

    it("should be able to list all groups with a parent") {
      val parentGroupName = "listGroupsParent"
      val childGroupName  = "listGroupsChild"

      Given("A parent group with a single child group")
      val parentId = for {
        parentGroup <- addGroup(parentGroupName)
        _           <- addGroup(childGroupName, Some(parentGroup.uniqueId))
      } yield parentGroup.uniqueId

      Then("The child group should be in the list of groups")
      val groups = getAllGroupsInGroup(Some(parentId.value))
      groups.map(_.name) should contain(childGroupName)

    }

    it("supports forward and backward pagination") {
      TestHelper.testPagination(pageSize = 3, totalExpectedItems = TOTAL_GROUPS) {
        TleGroupApi.listGroups(_, None)
      }
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.listGroups(ForwardPagination(LARGE_PAGE_SIZE), None)(unauthenticated)
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("listGroupUsers") {
    it("should be able to list all users in a group") {
      val groupName = "listGroupUsers"
      val users     = List("user1", "user2")

      Given("A group with a set of users")
      val groupId = for {
        group <- addGroup(groupName)
        _     <- setGroupUsers(group.uniqueId, users)
      } yield group.uniqueId

      Then("All users in the group should be returned")
      val groupUsers = getAllUsersInGroup(groupId.value)
      groupUsers shouldBe users
    }

    it("supports forward and backward pagination") {
      Given("A group with a large number of users")
      val totalUsers = 100
      val users      = for (i <- 1 to totalUsers) yield s"user$i"
      val groupId    = for {
        group <- addGroup("listGroupUsers")
        _     <- setGroupUsers(group.uniqueId, users.toList)
      } yield group.uniqueId

      Then("All users in the group should be returned")
      TestHelper.testPagination(pageSize = 33, totalExpectedItems = totalUsers) {
        TleGroupApi.listGroupUsers(_, groupId.value)
      }
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.listGroupUsers(ForwardPagination(LARGE_PAGE_SIZE), knownGroup.uniqueId)(
          unauthenticated
        )
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  describe("searchGroups") {
    // There are this many groups in the target institution
    val TOTAL_GROUPS     = 5
    val QUERY_ALL_GROUPS = ""

    it("supports searching for all groups") {
      val groups =
        TleGroupApi.searchGroups(ForwardPagination(TOTAL_GROUPS + 1), QUERY_ALL_GROUPS).value
      groups.items should not be empty
      groups.continue shouldBe empty
    }

    it("supports forward and backward pagination") {
      TestHelper.testPagination(pageSize = 2, totalExpectedItems = TOTAL_GROUPS) {
        (pagination: Pagination) => TleGroupApi.searchGroups(pagination, QUERY_ALL_GROUPS)
      }
    }

    it("supports searching for a specific group") {
      val groups = TleGroupApi.searchGroups(ForwardPagination(1), knownGroup.name).value
      groups.items should have size 1
      groups.items.head shouldBe knownGroup
    }

    it("returns all partially matching groups") {
      val commonTerm           = "group"
      val groupsWithCommonTerm = 5
      val groups = TleGroupApi.searchGroups(ForwardPagination(LARGE_PAGE_SIZE), commonTerm).value

      groups.items should have size groupsWithCommonTerm
      groups.items.forall { _.name.toLowerCase.contains(commonTerm) } shouldBe true
    }

    it("should return an empty list if the query matches no groups") {
      val groups =
        TleGroupApi.searchGroups(ForwardPagination(LARGE_PAGE_SIZE), "gobbledygook").value
      groups.items shouldBe empty
      groups.continue shouldBe empty
    }

    it("handles special characters in the query string") {
      forAll(specialCharacters) { char =>
        val response = TleGroupApi.searchGroups(ForwardPagination(1), s"test$char")
        // Basically, the server doesn't blow up - not testing search validity
        response shouldBe a[Right[_, _]]
      }
    }

    it("should return an AccessDeniedError if not authenticated") {
      val response = TestHelper.asUnauthenticatedUser { unauthenticated =>
        TleGroupApi.searchGroups(ForwardPagination(LARGE_PAGE_SIZE), knownGroup.name)(
          unauthenticated
        )
      }

      TestHelper.checkApiError(response) shouldBe a[AccessDeniedError]
    }
  }

  private def addGroup(name: String, parentId: Option[String] = None) =
    for {
      group <- TleGroupApi.createGroup(name, parentId)
      _ = createdGroups += group.uniqueId
    } yield group

  private def cleanupGroups(): Unit = {
    createdGroups.foreach { uniqueId =>
      TleGroupApi.deleteGroup(uniqueId)
    }
    createdGroups.clear()
  }

  private def setGroupUsers(groupId: String, users: List[String]) =
    TleGroupApi.updateGroup(groupId, users = Some(users))

  private def setGroupParent(groupId: String, parentId: String) =
    TleGroupApi.updateGroup(groupId, parentId = Some(parentId))

  private def getAllUsersInGroup(groupId: String, pageSize: Int = LARGE_PAGE_SIZE) =
    TestHelper.paginateForward(pageSize) {
      TleGroupApi.listGroupUsers(_, groupId)
    }

  private def getAllGroupsInGroup(groupId: Option[String], pageSize: Int = LARGE_PAGE_SIZE) =
    TestHelper.paginateForward(pageSize) { pagination =>
      TleGroupApi.listGroups(pagination, groupId)
    }

  private def getGroupsByIds(ids: Set[String], pageSize: Int = LARGE_PAGE_SIZE) =
    TestHelper.paginateForward(pageSize) { pagination =>
      TleGroupApi.getGroupsByIds(pagination, ids)
    }
}
