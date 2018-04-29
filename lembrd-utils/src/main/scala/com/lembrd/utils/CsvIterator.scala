package com.lembrd.utils

import java.io.File

import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}

/**
  *
  * User: lembrd
  * Date: 23/04/2018
  * Time: 11:18
  */
case class CsvIterator(file: File) extends Iterator[Array[String]] {
  private val parser = new CsvParser(new CsvParserSettings())
  parser.beginParsing(file)

  private var line = parser.parseNext()

  override def hasNext: Boolean = line != null

  override def next(): Array[String] = {
    val l = line
    line = parser.parseNext()

    if (line == null) {
      // eof
      parser.stopParsing()
    }

    l
  }
}
