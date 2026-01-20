package equellatests.tests

import java.io.File
import java.lang.System.console

import com.tle.webtests.framework.{PageContext, TestConfig}
import com.tle.webtests.pageobject.searching.SearchPage
import com.tle.webtests.pageobject.wizard.ContributePage
import equellatests.GlobalConfig.{baseFolderForInst, testConfig}
import equellatests.domain._
import equellatests.instgen.workflow._
import equellatests.{GlobalConfig, ShotProperties}
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalacheck._

object SearchQueryProperties extends ShotProperties("Search Query Properties") {
  val instFile               = new File(GlobalConfig.testConfig.getTestFolder, "workflow")
  val testConfig: TestConfig = new TestConfig(instFile, false)
  // This tests the old searching.do page, which is no longer accessible in the new ui
  if (!testConfig.isNewUI) {
    def createItem(context: PageContext, title: String): ItemId = {

      val wizard = new ContributePage(context).load.openWizard("Basic collection for searching")
      wizard.editbox(1, title)
      wizard.save.publish.getItemId
    }

    def onlyResult(context: PageContext, query: String, title: String): Prop = {
      val results      = new SearchPage(context).load.search(query)
      val resultExists = results.doesResultExist(title, 1)
      val oneResult    = results.getResults.size() == 1
      Prop(resultExists).label("result exists") && Prop(oneResult).label("one result")
    }

    property("basic keyword") = forAll { (w1: UniqueRandomWord, w2: RandomWord) =>
      withLogon(adminLogon) { context =>
        val title = s"${w1.word} ${w2.word}"
        val query = s"+${w1.word} ${w2.cased}"
        createItem(context, title)
        onlyResult(context, query, title)
      }
    }

    property("wildcard search") =
      forAllNoShrink(arbitrary[UniqueRandomWord], arbitrary[RandomWord], Gen.chooseNum(0, 6)) {
        (w1, w2, o) =>
          withLogon(adminLogon) { context =>
            val title  = s"${w1.word} ${w2.word}"
            val title2 = s"${w1.word} ${w2.word.reverse}${w2.word}"
            val arr    = w2.cased.toCharArray
            arr.update(1 + (Math.abs(o) % (arr.length - 1)), '?')
            val qstr   = new String(arr, 0, 5)
            val query  = s"+${w1.word} $qstr*"
            createItem(context, title)
            createItem(context, title2)
            onlyResult(context, query, title)
          }
      }

    property("boolean ops") = forAll {
      (
          rm: UniqueRandomWord,
          w1: RandomWord,
          w2: RandomWord,
          w3: RandomWord,
          op1: BooleanOp,
          op2: BooleanOp,
          biasLeft: Boolean
      ) =>
        withLogon(adminLogon) { context =>
          val t1 = QueryParser.titlesForOp(
            Set(w1.word),
            QueryParser.titlesForOp(Set(w2.word), Set(w3.word), op2, biasLeft, true),
            op1,
            biasLeft,
            true
          )
          val t2 = QueryParser.titlesForOp(
            Set(w1.word),
            QueryParser.titlesForOp(Set(w2.word), Set(w3.word), op2, biasLeft, false),
            op1,
            biasLeft,
            false
          )
          val title  = s"${rm.word} ${t1.mkString(" ")}"
          val title2 = s"${rm.word} ${t2.mkString(" ")}"
          val query =
            s"+${rm.word} +(${QueryParser.boolQuery(w1.cased, s"(${QueryParser.boolQuery(w2.cased, w3.cased, op2)})", op1)})"
          createItem(context, title)
          createItem(context, title2)
          onlyResult(context, query, title).label(s"t1='$title' t2='$title2' q='$query'")
        }
    }
  }
}
