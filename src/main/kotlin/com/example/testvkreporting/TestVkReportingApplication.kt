package com.example.testvkreporting

import com.example.testvkreporting.helpers.DbUtils
import com.example.testvkreporting.helpers.MemeImage
import com.example.testvkreporting.helpers.MemeImageDto
import com.example.testvkreporting.helpers.VkUtils
import com.vk.api.sdk.client.TransportClient
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


val log = KotlinLogging.logger {}

val userId = Integer.valueOf(System.getenv("VK_USER_ID"))
val groupId = Integer.valueOf(System.getenv("VK_GROUP_ID"))
val accessToken = System.getenv("VK_ACCESS_TOKEN")!!

val dbUrl = System.getenv("DB_URL")!!
val dbUsername = System.getenv("DB_USERNAME")!!
val dbPassword = System.getenv("DB_PASSWORD")!!

fun main() = runBlocking {
    log.info { "======== Start reposting ========" }
    Database.connect(
        url =dbUrl,
        user = dbUsername,
        password = dbPassword,
        driver = "org.postgresql.Driver")

    val memeFiles: List<MemeImageDto> = getLatestMemeFiles()
    if (memeFiles.isEmpty()) {
        log.info { "Nothing to publish" }
        return@runBlocking
    }

    val transportClient: TransportClient = HttpTransportClient()
    val vk = VkApiClient(transportClient)
    val actor = UserActor(userId, accessToken)

    memeFiles.forEach { memeFile ->
        val photoList = VkUtils.uploadPhotoToVk(vk, actor, memeFile.image)
        val postResponse = VkUtils.postPhotoToChannel(photoList, vk, actor, groupId)
        log.info { "Successfully reposted image with file id ${memeFile.id} and response post id $postResponse" }
    }
    log.info("======== Done reposting ========")
}

private fun getLatestMemeFiles(): List<MemeImageDto> = transaction {
    val sql = """
                select image.file_id, file 
                from meme join image on meme.file_id = image.file_id
                         where published >=(date_trunc('hour',NOW()::timestamp) - INTERVAL '3 hour')
                """.trimIndent()
    val memeImages = DbUtils.executeAndTransform(sql) { rs -> MemeImage.create(rs) }
    return@transaction DbUtils.saveFileAndConvertToDto(memeImages)
}
