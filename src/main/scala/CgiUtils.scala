package io.dinosaur
import scalanative.native._

object CgiUtils {
  def env(key: String): String = {
    Zone { implicit z =>
      val lookup = stdlib.getenv(toCString(key))
      if (lookup == null) {
        ""
      } else {
        fromCString(lookup)
      }
    }
  }

  private def dumbSplit(inputString: String, delim: String): Seq[String] = {
    var workingString = inputString
    var matches: List[String] = List()
    var pos = 0
    while (workingString != "" && workingString.contains(delim)) {
      val newpos = workingString.indexOf(delim)
      val token = workingString.slice(pos,newpos)
      matches = matches ++ List(token)
      workingString = workingString.slice(newpos + 1, workingString.size)
    }
    if (workingString != "") {
      matches = matches ++ List(workingString)
    }
    return matches
  }

  def parsePathInfo(pathInfo: String): Seq[String] = {
    dumbSplit(pathInfo,"/").filter( _ != "" )
  }

  def parseQueryString(queryString: String): Map[String, Seq[String]] = {
    val pairs = dumbSplit(queryString,"&").map( pair =>
      dumbSplit(pair,"=") match {
        case Seq(key, value) => (key,value)
      }
    ).groupBy(_._1).toSeq
    val groupedValues = for ( (k,v) <- pairs;
           values = v.toSeq.map(_._2) ) yield (k -> values)
    groupedValues.toMap
  }
}
