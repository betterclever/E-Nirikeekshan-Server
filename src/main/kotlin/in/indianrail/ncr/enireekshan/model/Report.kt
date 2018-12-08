package `in`.indianrail.ncr.enireekshan.model

import org.jetbrains.exposed.dao.EntityID


data class ReportModel(
        val id: Int,
        val submittedBy: Long
)

data class ReportCreateModel(
        val submittedBy: Long
)
