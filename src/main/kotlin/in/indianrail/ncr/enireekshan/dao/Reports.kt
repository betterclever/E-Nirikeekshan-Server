package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.dao.IntIdTable

object Reports : IntIdTable() {
    val reportID = integer("reportID").autoIncrement().primaryKey()
    val timestamp = long("timestamp")
}