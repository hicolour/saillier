package io.findify.fops

import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras.semiauto.deriveEnumerationCodec
import io.circe.parser._
import io.circe.syntax._

object Parsing extends App {

  sealed trait Operator
  case object AND extends Operator
  case object OR extends Operator

  sealed trait Operand

  case class Filters(operator: Operator, operands: List[Operand]) extends Operand
  case class Filter(value: String, field: String, action: String) extends Operand

  object Operand {
    implicit val decodeOperand: Decoder[Operand] =
      Decoder[Filter].map[Operand](identity).or(Decoder[Filters].map[Operand](identity))
    implicit val operatorCodec: Codec[Operator] = deriveEnumerationCodec[Operator]
  }

  val complexFilter = """{
                  |   "operator":"AND",
                  |   "operands":[
                  |      {
                  |         "operator":"AND",
                  |         "operands":[
                  |            {
                  |               "value":"option1",
                  |               "field":"title",
                  |               "action":"pin"
                  |            }
                  |         ]
                  |      },
                  |      {
                  |         "value":"option1",
                  |         "field":"title",
                  |         "action":"bottom"
                  |      }
                  |   ]
                  |}""".stripMargin

  decode[Operand](complexFilter) match {
    case Right(r) => println(r)
    case Left(l)  => println(l)
  }

//  implicit val encodeData: Encoder[Data] = Encoder.instance {
//    case options @ OptionsData(_) => options.asJson
//    case text @ TextData(_)       => text.asJson
//  }

}
