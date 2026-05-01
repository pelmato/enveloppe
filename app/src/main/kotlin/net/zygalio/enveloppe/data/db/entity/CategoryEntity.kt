package net.zygalio.enveloppe.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(
        entity = EnvelopeEntity::class,
        parentColumns = ["id"],
        childColumns = ["envelopeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("envelopeId")],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val envelopeId: Long,
    val name: String,
)
