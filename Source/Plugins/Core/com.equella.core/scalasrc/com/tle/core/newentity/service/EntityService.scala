package com.tle.core.newentity.service

import com.tle.beans.Institution
import com.tle.beans.newentity.Entity

import java.time.Instant

trait EntityService {

  /**
    * Get all the Entity of an Institution.
    *
    * @param institution The Institution from where to get the list of Entity.
    */
  def getAllEntity(institution: Institution): java.util.List[Entity]

  /**
    * Create a new Entity or update an existing entity.
    *
    * @param uuid               The unique ID of an Entity.
    * @param typeId             Unique ID indicating what type the Entity is. (e.g. CloudProvider)
    * @param name               Name of the Entity.
    * @param nameStrings        Name of the Entity including locale in Json format (e.g. {"en-GB": "name"}
    *                           ).
    * @param description        Description of the Entity.
    * @param descriptionStrings Description of the Entity including locale in Json format (e.g.
    *                           {"en-GB": "desc"}).
    * @param created            The time when the Entity was created.
    * @param modified           The time when the Entity was lastly modified.
    * @param owner              Owner of the Entity.
    * @param data               Custom data of the Entity in Json format.
    * @param institution        Institution which the Entity belongs to.
    */
  def createOrUpdate(uuid: String,
                     typeId: String,
                     name: String,
                     nameStrings: String,
                     description: String,
                     descriptionStrings: String,
                     created: Instant,
                     modified: Instant,
                     owner: String,
                     data: String,
                     institution: Institution): Unit

  /**
    * Delete all the Entity of an Institution.
    *
    * @param institution The Institution for which to delete Entity.
    */
  def deleteAllEntity(institution: Institution): Unit
}
