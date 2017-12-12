package io.es.domain.turtle

sealed trait Rotation
case object ToLeft extends Rotation
case object ToRight extends Rotation
