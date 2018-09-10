package io.es.algebra

import io.es.infra.data.{RawEvent, Tag}

trait Bus[F[_]] {

  sealed trait PointerStrategy
  case object FromStart extends PointerStrategy
  case object FromLast  extends PointerStrategy

  sealed trait RetryStrategy
  case object AlwaysRetry extends RetryStrategy
  case object NoRetry     extends RetryStrategy

  sealed trait Filter
  case object NilFilter                extends Filter
  case class TagFilter(tags: Set[Tag]) extends Filter

  case class StreamFilter(filter: Filter, pointerStrategy: PointerStrategy, retryStrategy: RetryStrategy)

  case class HandlerTag(value: String)
  case class BusHandler[S](
                            tag: HandlerTag,
                            filter: StreamFilter,
                            load: RawEvent => F[S],
                            run: (RawEvent, S) => S,
                            register: S => F[Unit],
                            onError: Throwable => F[Unit]
                          )

  case class HandlerState(map: List[(RetryState, BusHandler[_])]) {

    def minThreshold: Option[Long] = {
      map.map(_._1).collect { case Continue(threshold, _) => threshold }.sorted.headOption
    }


  }

  sealed trait RetryState
  case class RetryAfterError(threshold: Long, nbError: Int, lastError: Throwable, filter: StreamFilter) extends RetryState
  case class StopAfterError(threshold: Long, lastError: Throwable, filter: StreamFilter)                extends RetryState
  case class Continue(threshold: Long, filter: StreamFilter)                                            extends RetryState

  case class FilterState(retryState: RetryState, threshold: Long)




  /*
      <Source> ----> <Filter1> ----> <Threshold> ----> <Handler1>
                               ----> <Threshold> ----> <Handler2>
                               ----> <Threshold> ----> <Handler3>

               ----> <Filter2> ----> <Threshold> ----> <Handler4>
   */
}
