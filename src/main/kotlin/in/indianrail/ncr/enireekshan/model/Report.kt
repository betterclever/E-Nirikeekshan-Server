package `in`.indianrail.ncr.enireekshan.model

import org.jetbrains.exposed.dao.EntityID


data class ReportModel(
        val id: Int,
        val submittedBy: Long,
        val inspections: List<InspectionModel>
)

data class ReportCreateModel(
        val submittedBy: Long,
        val inspections: List<InspectionCreateModel>
)
