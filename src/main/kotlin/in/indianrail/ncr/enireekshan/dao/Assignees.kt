package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.sql.Table

object ObservationAssignees : Table() {
    val observationID = reference("observationID", Observations).primaryKey(0)
    val userID = reference("userID", Users).primaryKey(1)
}

object SuggestionAssignees : Table() {
    val suggestionID = reference("suggestionID", Suggestions).primaryKey(0)
    val userID = reference("userID", Users).primaryKey(1)
}

