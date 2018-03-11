package `in`.indianrail.ncr.enireekshan.model

data class UserModel(
        val phone: Long,
        val name: String,
        val designation: String,
        val department: String,
        val location: String,
        val assignable: Boolean
)