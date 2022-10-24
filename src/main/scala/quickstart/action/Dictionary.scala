package quickstart.action

import slick.driver.SQLiteDriver.api._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Success, Failure}
import xitrum.{RequestVar, Action, FutureAction}
import xitrum.annotation.{GET, POST, DELETE}
import quickstart.action.DefaultLayout


object RVWords extends RequestVar[Vector[(Int, String)]]


@GET("dict")
class DictionaryView extends DefaultLayout{
  def execute(): Unit = {
    val res = Dictionary.getAll()
    RVWords.set(Await.result(res, 1.second))
    respondView()
  }
}


@POST("add")
class AddWord extends Action {
  def execute(): Unit = {
    Dictionary.insert(param("word").toLowerCase())
    redirectTo[DictionaryView]()
  }
}

// DELETE
@POST("del")
class DelWord extends Action {
  def execute(): Unit = {
    Dictionary.remove(param("word").toLowerCase())
    redirectTo[DictionaryView]()
  }
}


object Dictionary {

  val db = Database.forConfig("keyword")

  def insert(word: String): Future[Seq[(Int, String)]] = {
    db.run(sql"""
      INSERT INTO keyword (name)
      SELECT '#${word}' WHERE NOT EXISTS 
      (SELECT * FROM keyword WHERE name='#${word}')""".as[(Int, String)])
    }

  def remove(word: String): Future[Seq[(Int, String)]] = {
    db.run(sql"DELETE FROM keyword WHERE name='#${word}'".as[(Int, String)])
  }

  def getAll(): Future[Seq[(Int, String)]] = {
    db.run(sql"SELECT * FROM keyword ORDER BY name".as[(Int, String)])
  }
}
