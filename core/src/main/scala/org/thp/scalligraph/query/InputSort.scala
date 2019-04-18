package org.thp.scalligraph.query

import scala.reflect.runtime.{universe ⇒ ru}

import gremlin.scala.{__, Element, GremlinScala, OrderBy}
import org.apache.tinkerpop.gremlin.process.traversal.Order
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.scalactic.Accumulation._
import org.scalactic.{Bad, Good, One}
import org.thp.scalligraph.InvalidFormatAttributeError
import org.thp.scalligraph.auth.AuthContext
import org.thp.scalligraph.controllers.{FSeq, FString, FieldsParser}
import org.thp.scalligraph.models.ScalliSteps

case class InputSort(fieldOrder: (String, Order)*) extends InputQuery {
  def orderby[A, F, T](f: GremlinScala[F] ⇒ GremlinScala[T], order: Order): OrderBy[A] = new OrderBy[A] {
    override def apply[End](traversal: GraphTraversal[_, End]): GraphTraversal[_, End] =
      traversal.by(f(__[F]).traversal, order)
  }
  override def apply[S <: ScalliSteps[_, _, _]](
      publicProperties: List[PublicProperty[_ <: Element, _, _]],
      stepType: ru.Type,
      step: S,
      authContext: Option[AuthContext]): S = {
    val orderBys = fieldOrder.flatMap {
      case (fieldName, order) ⇒
        getProperty(publicProperties, stepType, fieldName)
          .get(authContext)
          .map(f ⇒ orderby(f, order))
    }
    step
      .asInstanceOf[ScalliSteps[_, _, S]]
      .sort(orderBys: _*)
  }
}

object InputSort {
  implicit val fieldsParser: FieldsParser[InputSort] = FieldsParser("sort-f") {
    case (_, FObjOne("_fields", FSeq(f))) ⇒
      f.validatedBy {
          case FObjOne(name, FString(order)) ⇒
            try Good(new InputSort(name → Order.valueOf(order)))
            catch {
              case _: IllegalArgumentException ⇒
                Bad(One(InvalidFormatAttributeError("order", "order", Seq("field: 'incr", "field: 'decr", "field: 'shuffle"), FString(order))))
            }
        }
        .map(x ⇒ new InputSort(x.flatMap(_.fieldOrder): _*))
  }
}