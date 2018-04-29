package com.lembrd.utils

import gnu.trove.map.hash.{THashMap, TIntDoubleHashMap, TIntIntHashMap, TLongObjectHashMap}
import gnu.trove.procedure.{TIntDoubleProcedure, TIntIntProcedure, TLongObjectProcedure, TObjectObjectProcedure}

import scala.collection.mutable.ArrayBuffer

/**
  *
  * User: lembrd
  * Date: 24/03/2018
  * Time: 20:53
  */

object TroveImplicits {

  /*
    def objectProcedure[X, Y]( handler : (X) => Option[Y] ) : TObjectProcedure[X] = {
      new TObjectProcedure[X] {
        override def execute(x: X): Boolean = {
          val r = handler(x)

        }
      }
    }
  */

  implicit class RichTroveLongObjectMap[X](map: TLongObjectHashMap[X]) {
    def collectFirst[Y]()(handler: PartialFunction[(Long, X), Y]): Option[Y] = {
      var result: Option[Y] = None

      map.forEachEntry(new TLongObjectProcedure[X] {
        override def execute(a: Long, b: X): Boolean = {
          if (handler.isDefinedAt(a, b)) {
            result = Some(handler.apply(a, b))
            false
          } else {
            true
          }
        }
      })

      result
    }

    def seq: Seq[(Long, X)] = {
      val buf = ArrayBuffer[(Long, X)]()
      map.forEachEntry(new TLongObjectProcedure[X] {
        override def execute(a: Long, b: X): Boolean = {
          buf += (a -> b)
          true
        }
      })
      buf

    }

    def seqFlatMap[Y](handler: (Long, X) => Y): Seq[Y] = {
      val buf = ArrayBuffer[Y]()
      map.forEachEntry(new TLongObjectProcedure[X] {
        override def execute(a: Long, b: X): Boolean = {
          val r = handler(a, b)
          buf += r
          true
        }
      })
      buf
    }
  }

  implicit class RichTroveIntIntMap[X](map: TIntIntHashMap) {
    def collectFirst[Y]()(handler: PartialFunction[(Int, Int), Y]): Option[Y] = {
      var result: Option[Y] = None

      map.forEachEntry(new TIntIntProcedure {
        override def execute(a: Int, b: Int): Boolean = {
          if (handler.isDefinedAt(a, b)) {
            result = Some(handler.apply(a, b))
            false
          } else {
            true
          }
        }
      })

      result
    }

    def seq: Seq[(Int, Int)] = {
      val buf = ArrayBuffer[(Int, Int)]()
      map.forEachEntry(new TIntIntProcedure {
        override def execute(a: Int, b: Int): Boolean = {
          buf += (a -> b)
          true
        }
      })
      buf

    }

    def seqFlatMap[Y](handler: (Int, Int) => Y): Seq[Y] = {
      val buf = ArrayBuffer[Y]()
      map.forEachEntry(new TIntIntProcedure {
        override def execute(a: Int, b: Int): Boolean = {
          val r = handler(a, b)
          buf += r
          true
        }
      })
      buf
    }
  }

  implicit class RichTroveIntDoubleMap[X](map: TIntDoubleHashMap) {
    def collectFirst[Y]()(handler: PartialFunction[(Int, Double), Y]): Option[Y] = {
      var result: Option[Y] = None

      map.forEachEntry(new TIntDoubleProcedure {
        override def execute(a: Int, b: Double): Boolean = {
          if (handler.isDefinedAt(a, b)) {
            result = Some(handler.apply(a, b))
            false
          } else {
            true
          }
        }
      })

      result
    }

    def seq: Seq[(Int, Double)] = {
      val buf = ArrayBuffer[(Int, Double)]()
      map.forEachEntry(new TIntDoubleProcedure {
        override def execute(a: Int, b: Double): Boolean = {
          buf += (a -> b)
          true
        }
      })
      buf

    }

    def seqFlatMap[Y](handler: (Int, Double) => Y): Seq[Y] = {
      val buf = ArrayBuffer[Y]()
      map.forEachEntry(new TIntDoubleProcedure {
        override def execute(a: Int, b: Double): Boolean = {
          val r = handler(a, b)
          buf += r
          true
        }
      })
      buf
    }
  }

  implicit class RichTroveObjectMap[K, X](map: THashMap[K, X]) {
    def collectFirst[Y](handler: PartialFunction[(K, X), Y]): Option[Y] = {
      var result: Option[Y] = None

      map.forEachEntry(new TObjectObjectProcedure[K, X] {
        override def execute(a: K, b: X): Boolean = {
          if (handler.isDefinedAt(a, b)) {
            result = Some(handler.apply(a, b))
            false
          } else {
            true
          }
        }
      })

      result
    }

    def seqFlatMap[Y](handler: (K, X) => Y): Seq[Y] = {
      val buf = ArrayBuffer[Y]()
      map.forEachEntry(new TObjectObjectProcedure[K, X] {
        override def execute(a: K, b: X): Boolean = {
          val r = handler(a, b)
          buf += r
          true
        }
      })
      buf
    }
  }


  //  def flatMap( map : )

  /*
  type H[X] = Object {
    def forEachValue(obj : TObjectProcedure[_ <: X]) : Boolean
  }

//  var x: H = _

  implicit class RichTroveMap[X](map : H[X]) {
    def map[Y]( handler : (X) => Option[Y]) : Seq[Y] = {
      val buf = ArrayBuffer[Y]
      map.forEachValue( new TObjectProcedure[X] {
        override def execute(v: X): Boolean = {
          val r = handler(v)
          r match {
            case Some(vv) =>
              buf += vv
          }
          r.isDefined
        }
      })

      buf
    }
  }*/
}
