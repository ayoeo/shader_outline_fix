package com.twoandahalfdevs.shaderoutlinefix

import kotlin.reflect.KClass

typealias Listener = (Event) -> Unit

val listeners = mutableListOf<EventTarget>()

abstract class Event {
  fun call() {
    listeners
      .filter { it.type == this::class && it.filter() }
      .forEach {
        it.block(this)
      }
  }
}

sealed class EventTarget(val type: KClass<out Event>, val block: Listener) {
  open fun filter() = true

  class GenericEventTarget(type: KClass<out Event>, block: Listener) : EventTarget(type, block)
}

abstract class EventCancellable : Event() {
  private var cancelled = false

  fun cancel() {
    this.cancelled = true
  }

  fun cancelled() = this.cancelled
}

enum class Order { First, Default, Last }

@Suppress("UNCHECKED_CAST")
inline fun <reified E : Event> listenFor(
  order: Order = Order.Default,
  noinline block: (E) -> Unit
) {
  listeners.add(EventTarget.GenericEventTarget(E::class, block as Listener))
}
