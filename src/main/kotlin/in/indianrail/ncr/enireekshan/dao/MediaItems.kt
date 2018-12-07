package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.MediaItemsModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object MediaItems : IntIdTable() {
    val inspectionId = reference("inspectionId", Inspections)
    val filePath = text("filePath")
}

class MediaItem(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<MediaItem>(MediaItems)

    var filePath by MediaItems.filePath
    var inspection by Inspection referencedOn MediaItems.inspectionId

    fun getMediaItemsModel() = MediaItemsModel(
            filepath = filePath
    )
}



