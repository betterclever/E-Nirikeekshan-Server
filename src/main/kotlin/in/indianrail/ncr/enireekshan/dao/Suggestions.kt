package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.SuggestionModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Suggestions : IntIdTable() {
    val title = text("title")
    val status = text("status")
    val timestamp = long("timestamp")
    val submittedBy = reference("submittedBy", Users)
    val reportID = text("reportID")
}

class Suggestion(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Suggestion>(Suggestions)

    var title by Suggestions.title
    var suggestionID by Suggestions.id
    var status by Suggestions.status
    var reportID by Suggestions.reportID
    var timestamp by Suggestions.timestamp
    val assignees by UserEntity via SuggestionAssignees
    var submittedBy by UserEntity referencedOn Suggestions.submittedBy

    fun getSuggestionModel() = SuggestionModel(
            title = title,
            status = status,
            assignees = assignees.map { it.getUserModel() },
            timestamp = timestamp,
            submittedBy = submittedBy.getUserModel(),
            reportID = reportID
    )
}
