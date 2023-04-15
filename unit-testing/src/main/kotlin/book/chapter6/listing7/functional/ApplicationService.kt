package book.chapter6.listing7.functional

import java.time.LocalDateTime

class ApplicationService(
    private val directoryName: String,
    private val auditManager: AuditManager,
) {
    private val persister by lazy { Persister() }

    fun addRecord(visitorName: String, timeOfVisit: LocalDateTime) {
        val files = persister.readDirectory(directoryName)
        val update = auditManager.addRecord(
            files = files,
            visitorName = visitorName,
            timeOfVisit = timeOfVisit,
            )
        persister.applyUpdate(directoryName, update)
    }
}