package `in`.indianrail.ncr.enireekshan.model

data class SuggestionModel(
        val title : String,
        val status : String,
        val reportID : String,
        val timestamp: Long,
        val submittedBy: UserModel,
        val assignees : List<UserModel>
)


data class SuggestionCreateModel(
        val title : String,
        val submitterID: Long,
        val assigneeRoles: List<AssigneeRole>
)