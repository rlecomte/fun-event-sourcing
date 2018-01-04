package io.es.infra

case class UnprocessableEventException(eventType: String) extends RuntimeException
