package com.example.testvkreporting.helpers

import java.io.File
import java.sql.ResultSet

data class MemeImage(val id: String, val image: ByteArray, val published: String) {
    companion object {
        fun create(rs: ResultSet) = MemeImage(
            rs.getString("file_id"),
            rs.getBytes("file"),
            rs.getString("published"))
    }
}

data class MemeImageDto(val id: String, val image: File, val published: String)
