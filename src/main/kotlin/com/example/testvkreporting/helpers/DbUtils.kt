package com.example.testvkreporting.helpers

import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.ResultSet

class DbUtils {
    companion object {
        fun <T : Any> executeAndTransform(sql: String, transform: (ResultSet) -> T): List<T> {
            val result = arrayListOf<T>()
            TransactionManager.current().exec(sql, explicitStatementType = null) { rs ->
                while (rs.next()) {
                    result += transform(rs)
                }
            }
            return result
        }

        fun saveFileAndConvertToDto(memeImages: List<MemeImage>): List<MemeImageDto> {
            return memeImages.map { memeImage ->
                val testPictureFile: File = File.createTempFile(memeImage.id, ".png")
                testPictureFile.writeBytes(memeImage.image)
                MemeImageDto(memeImage.id, testPictureFile, memeImage.published)
            }
        }
    }
}
