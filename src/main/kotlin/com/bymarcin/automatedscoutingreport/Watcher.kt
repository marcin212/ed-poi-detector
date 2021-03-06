package com.bymarcin.automatedscoutingreport

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.util.concurrent.TimeUnit

class Watcher(val screenPath: Path, val listener: (Path) -> Unit) : Thread("FILE WATCHER") {
    private val watcher = FileSystems.getDefault().newWatchService()
    private val key = screenPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
    public var isWorking = true
    override fun run() {
        while (isWorking) {
            val key1 = watcher.poll(500, TimeUnit.MILLISECONDS)
            key1?.let { key1 ->
                key1.pollEvents().filter { it.kind() != StandardWatchEventKinds.OVERFLOW }
                        .map { it as WatchEvent<Path> }
                        .map { it.context() }
                        .forEach {
                            println("NEW FILE DETECTED: $it")
                            listener.invoke(screenPath.resolve(it))
                        }
            }
            key1?.reset()
        }
    }
}