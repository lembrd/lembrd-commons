package com.lembrd.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
  *
  * User: lembrd
  * Date: 02/11/2017
  * Time: 15:32
  */
object UtcFormat {
  val df = ISODateTimeFormat.dateTime()

  def apply(dt : DateTime) = {
    dt.toString(df)
  }

/*
  implicit val format: Format[DateTime] = Format(
    Reads[DateTime](x => JsSuccess(UtcFormat.df.parseDateTime(x.as[String]))),
    Writes[DateTime](x => JsString(UtcFormat(x)))
  )
*/
}
