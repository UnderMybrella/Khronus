package dev.brella.khronus

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import net.minecraft.util.concurrent.ITaskExecutor
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class ThreadListenerDispatcher(val listener: WeakReference<ITaskExecutor<Runnable>>): CoroutineDispatcher() {
    constructor(listener: ITaskExecutor<Runnable>): this(WeakReference(listener))

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        listener.get()?.enqueue(block)
    }
}