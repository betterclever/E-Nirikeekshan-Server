package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.sql.Table

object InspectionAssignees : Table() {
    val inspectionID = reference("inspectionID", Inspections).primaryKey(0)
    val userID = reference("userID", Users).primaryKey(1)
}

