package com.mujin.chadak.signal

/**
 * Merges several sources. Later sources overwrite the same signal key, and the
 * latest driving context wins. Debug mocks are registered last so they override
 * the Pleos stub during a demo.
 */
class CompositeSignalSource(
    private val children: List<VehicleSignalSource>,
) : VehicleSignalSource {
    private val signals = linkedMapOf<VehicleSignalSource, List<VehicleSignal>>()
    private val contexts = linkedMapOf<VehicleSignalSource, DrivingContext>()
    private var listener: VehicleSignalSource.Listener? = null
    private var started = false

    override fun start(listener: VehicleSignalSource.Listener) {
        this.listener = listener
        if (started) {
            dispatch()
            return
        }
        started = true
        children.forEach { child ->
            child.start(
                object : VehicleSignalSource.Listener {
                    override fun onSignals(signals: List<VehicleSignal>) {
                        this@CompositeSignalSource.signals[child] = signals
                        dispatch()
                    }

                    override fun onDrivingContext(context: DrivingContext) {
                        contexts[child] = context
                        dispatch()
                    }
                },
            )
        }
    }

    override fun stop() {
        if (!started) {
            listener = null
            return
        }
        children.forEach { it.stop() }
        started = false
        listener = null
        signals.clear()
        contexts.clear()
    }

    private fun dispatch() {
        val merged = linkedMapOf<String, VehicleSignal>()
        children.forEach { child ->
            signals[child].orEmpty().forEach { signal ->
                merged[signal.key()] = signal
            }
        }
        listener?.onSignals(merged.values.toList())
        val context = children.asReversed().firstNotNullOfOrNull { contexts[it] }
        if (context != null) {
            listener?.onDrivingContext(context)
        }
    }
}
