package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.currentTimeStamp
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.STATUS_UNSEEN
import `in`.indianrail.ncr.enireekshan.model.SuggestionCreateModel
import `in`.indianrail.ncr.enireekshan.model.SuggestionModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class SuggestionController {

    fun getSuggestionAssignedTo(userID: Long): List<SuggestionModel> = transaction {
        (Suggestions innerJoin SuggestionAssignees)
                .select { SuggestionAssignees.userID eq userID }
                .groupBy { SuggestionAssignees.suggestionID }
                .map {
                    it.value.map {
                        SuggestionModel(
                                title = it[Suggestions.title],
                                status = it[Suggestions.status],
                                reportID = it[Suggestions.reportID],
                                timestamp = it[Suggestions.timestamp],
                                assignees = Suggestion[it[Suggestions.id]].assignees.map(UserEntity::getUserModel),
                                submittedBy = UserEntity[it[Suggestions.submittedBy]].getUserModel())
                    }
                }.flatten()
    }

    fun getSuggestionsSubmittedBy(userID: Long): List<SuggestionModel> = transaction {
        Suggestions.select { Suggestions.submittedBy eq userID }
                .map {
                    SuggestionModel(
                            title = it[Suggestions.title],
                            status = it[Suggestions.status],
                            reportID = it[Suggestions.reportID],
                            timestamp = it[Suggestions.timestamp],
                            assignees = Suggestion[it[Suggestions.id]].assignees.map(UserEntity::getUserModel),
                            submittedBy = UserEntity[it[Suggestions.submittedBy]].getUserModel()
                    )
                }
    }

    fun addSuggestion(suggestionModel: SuggestionCreateModel) = transaction {
        val newSuggestionID = Suggestions.insertAndGetId {
            it[title] = suggestionModel.title
            it[status] = STATUS_UNSEEN
            it[reportID] = "abc"
            it[timestamp] = currentTimeStamp
            it[submittedBy] = EntityID(suggestionModel.submitterID, Users)
        }
        suggestionModel.assigneeRoles.forEach {
            /*val messaging = FirebaseMessaging.getInstance()
            val message = Message.builder()
                    .putData("sender", )
            */
            val res = Users.slice(Users.id).select {
                (Users.location eq it.location) and
                        (Users.designation eq it.designation) and
                        (Users.department eq it.department)
            }
            res.forEach { row ->
                SuggestionAssignees.insert {
                    it[suggestionID] = newSuggestionID
                    it[userID] = row[Users.id]
                }
            }
        }
    }

    fun getSuggestionsWithTitleLike(searchQuery: String): List<SuggestionModel> = transaction {
        Suggestions.select { Suggestions.title like searchQuery }
                .limit(15)
                .map {
                    SuggestionModel(
                            title = it[Suggestions.title],
                            status = it[Suggestions.status],
                            reportID = it[Suggestions.reportID],
                            timestamp = it[Suggestions.timestamp],
                            assignees = Suggestion[it[Suggestions.id]].assignees.map(UserEntity::getUserModel),
                            submittedBy = UserEntity[it[Suggestions.submittedBy]].getUserModel()
                    )
                }
    }
}
