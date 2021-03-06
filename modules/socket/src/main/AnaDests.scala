package lila.socket

import play.api.libs.json._

import chess.format.FEN
import chess.opening._
import chess.variant.Variant
import lila.tree.Node.openingWriter

case class AnaDests(
    variant: Variant,
    fen: FEN,
    path: String,
    chapterId: Option[String]
) {

  def isInitial =
    variant.standard && fen.value == chess.format.Forsyth.initial && path == ""

  val dests: String =
    if (isInitial) AnaDests.initialDests
    else {
      val sit = chess.Game(variant.some, fen.value.some).situation
      sit.playable(false) ?? {
        sit.destinations map {
          case (orig, dests) => s"${orig.piotr}${dests.map(_.piotr).mkString}"
        } mkString " "
      }
    }

  lazy val opening = Variant.openingSensibleVariants(variant) ?? {
    FullOpeningDB findByFen fen.value
  }

  def json = Json.obj(
    "dests" -> dests,
    "path" -> path
  ).add("opening" -> opening).add("ch", chapterId)
}

object AnaDests {

  private val initialDests = "iqy muC gvx ltB bqs pxF jrz nvD ksA owE"

  def parse(o: JsObject) = for {
    d ← o obj "d"
    variant = chess.variant.Variant orDefault ~d.str("variant")
    fen ← d str "fen"
    path ← d str "path"
    chapterId = d str "ch"
  } yield AnaDests(variant = variant, fen = FEN(fen), path = path, chapterId = chapterId)
}
