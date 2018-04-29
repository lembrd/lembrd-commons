package com.lembrd.utils

import java.io._

import com.lembrd.util.PrimitiveBits
import com.univocity.parsers.csv.{CsvWriter, CsvWriterSettings}

import scala.util.Try

/**
  *
  * User: lembrd
  * Date: 13/04/2018
  * Time: 12:29
  */

class FileWriterCsv(f: File) {

  val settings = new CsvWriterSettings()
  val fos = new FileOutputStream(f)
  val bfw = new BufferedWriter(new OutputStreamWriter(fos, PrimitiveBits.UTF8_ENCODING))

  val writer = new CsvWriter(bfw, settings)

  def append(csv: Seq[_]) = {
    writer.writeRow(csv.map(_.toString): _*)
  }

  def close(): Unit = {
    Try {
      writer.close()
    }
    Try {
      fos.close()
    }
  }

  def flush() = {
    writer.flush()
  }

}
