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

package com.tle.web.api.tokenisation

import com.tle.freetext.{LuceneConstants, TLEAnalyzer}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse.badRequest
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.apache.lucene.analysis.{CharArraySet, WordlistLoader}
import org.jboss.resteasy.annotations.cache.NoCache
import org.apache.lucene.queryParser.{ParseException, QueryParser}
import org.apache.lucene.search.BooleanQuery
import java.io.{File, FileReader}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces, QueryParam}
import scala.util.{Failure, Success, Try}

case class Tokens(tokens: Array[String]);

@NoCache
@Path("search/token")
@Produces(Array("application/json"))
@Api("Search V2")
class TokenisationResource {
  @GET
  @ApiOperation(
    value = "Tokenise text by Lucene",
    notes =
      "This endpoint is used to tokenise plain texts by Lucene. It supports stemming and stop words.",
    response = classOf[Tokens],
  )
  def tokenise(@ApiParam("The text to be tokenised") @QueryParam("text") text: String): Response = {
    val stopWordsFile: File = LegacyGuice.freetextIndex.getStopWordsFile
    val stopSet: CharArraySet = WordlistLoader.getWordSet(
      new FileReader(stopWordsFile),
      new CharArraySet(LuceneConstants.LATEST_VERSION, 0, true))

    // Use empty string as the Field because we just need tokens.
    val parser: QueryParser =
      new QueryParser(LuceneConstants.LATEST_VERSION, "", new TLEAnalyzer(stopSet, true))

    Try {
      parser.parse(text) match {
        // If the result is a BooleanQuery get all of its clauses and convert to strings.
        // For others like TermQuery and PhraseQuery, just call `toString`.
        case booleanQuery: BooleanQuery => booleanQuery.getClauses.map(_.toString)
        case other                      => Array(other.toString)
      }
    } match {
      case Success(tokens) => Response.ok().entity(Tokens(tokens)).build()
      case Failure(e: ParseException) =>
        badRequest(s"The provided text: ${text} cannot be tokenised due to error: ${e.getMessage}")
    }
  }
}
