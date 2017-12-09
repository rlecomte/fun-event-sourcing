package io.es.turtle

sealed trait Rotation
case object ToLeft extends Rotation
case object ToRight extends Rotation
