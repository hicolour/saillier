package io.findify.fops

object Testcc extends App {

//  type GenericSearchRequest[T] = GenericSearchRequestC[T] with GenericSearchRequestT[T]
//  type BaseSearchRequest = BaseSearchRequestC with BaseSearchRequestT
//
//  sealed trait BaseSearchRequest {
//    def from: Int
//    def count: Int
//    def query: String
//    def copy(query: Option(String)): BaseSearchRequest =
//      this match {
//        case s: Search => s.copy(query = query)
//        case
//      }
//  }
//
//  sealed trait GenericSearchRequest[T <: BaseSearchRequest] {
//    def search: T
//    def meta: String
//  }
//
//  case class Search(from: Int, count: Int, query: String) extends BaseSearchRequest
//
//  val s = Search(1, 1, "").copy
//
//  case class CollectionSearch(from: Int, count: Int, query: String, filters: String) extends BaseSearchRequest
//
//  case class SearchRequest(
//      search: Search,
//      meta: String
//  ) extends GenericSearchRequest[Search]
//
//  case class CollectionSearchRequest(
//      search: CollectionSearch,
//      meta: String
//  ) extends GenericSearchRequest[CollectionSearch]
//
//  def process[SR <: BaseSearchRequest](request: GenericSearchRequest[SR]) =
//    request.search.copy(query = "12345")
//
//  val a = process(SearchRequest(Search(0, 1, "test"), "test"))
//  val b = process(CollectionSearchRequest(CollectionSearch(0, 1, "test", "filters"), "test"))
//  println(a)
//  println(b)

//  type GenericSearchRequest[T] = GenericSearchRequestC[T] with GenericSearchRequestT[T]
//  type BaseSearchRequest = BaseSearchRequestC with BaseSearchRequestT
//
//  abstract class BaseSearchRequestC(from: Int, count: Int, query: String) extends BaseSearchRequestT
//
//  sealed trait BaseSearchRequestT {
//    def from: Int
//    def count: Int
//    def query: String
//  }
//
//  sealed trait GenericSearchRequestT[T <: BaseSearchRequest] {
//    def search: T
//    def meta: String
//  }
//
//  abstract class GenericSearchRequestC[T <: BaseSearchRequest](
//      search: T,
//      meta: String
//  ) extends GenericSearchRequestT[T]
//
//  case class Search(override val from: Int, override val count: Int, override val query: String)
//      extends BaseSearchRequestC(from, count, query)
//      with BaseSearchRequestT
//  case class CollectionSearch(override val from: Int,
//                              override val count: Int,
//                              override val query: String,
//                              filters: String)
//      extends BaseSearchRequestC(from, count, query)
//      with BaseSearchRequestT
//
//  case class SearchRequest(
//      override val search: Search,
//      override val meta: String
//  ) extends GenericSearchRequestC[Search](search, meta)
//      with GenericSearchRequestT[Search]
//
//  case class CollectionSearchRequest(
//      override val search: CollectionSearch,
//      override val meta: String
//  ) extends GenericSearchRequestC[CollectionSearch](search, meta)
//      with GenericSearchRequestT[CollectionSearch]
//
//  def process[SR <: BaseSearchRequest](request: GenericSearchRequest[SR]) =
//    request.search.copy(query = "12345")
//
//  val a = process(SearchRequest(Search(0, 1, "test"), "test"))
//  val b = process(CollectionSearchRequest(CollectionSearch(0, 1, "test", "filters"), "test"))
//  println(a)
//  println(b)

}
