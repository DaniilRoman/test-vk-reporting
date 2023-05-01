package com.example.testvkreporting

import com.example.testvkreporting.helpers.DbUtils
import com.example.testvkreporting.helpers.MemeImage
import com.example.testvkreporting.helpers.MemeImageDto
import com.example.testvkreporting.helpers.VkUtils
import com.vk.api.sdk.client.TransportClient
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneOffset


val log = KotlinLogging.logger {}

val userId = Integer.valueOf(System.getenv("VK_USER_ID"))
val groupId = Integer.valueOf(System.getenv("VK_GROUP_ID"))
val accessToken = System.getenv("VK_ACCESS_TOKEN")!!

val dbUrl = System.getenv("DB_URL")!!
val dbUsername = System.getenv("DB_USERNAME")!!
val dbPassword = System.getenv("DB_PASSWORD")!!

fun main() {
    log.info { "======== Start reposting ========" }
    Database.connect(
        url =dbUrl,
        user = dbUsername,
        password = dbPassword,
        driver = "org.postgresql.Driver")

    val transportClient: TransportClient = HttpTransportClient()
    val vk = VkApiClient(transportClient)
    val actor = UserActor(userId, accessToken)


    val memeFiles: List<MemeImageDto> = getLatestMemeFiles().let { it.ifEmpty { restoreOldMeme() } }
    memeFiles.forEach { memeFile ->
        val photoList = VkUtils.uploadPhotoToVk(vk, actor, memeFile.image)
        val postResponse = VkUtils.postPhotoToChannel(photoList, vk, actor, groupId)
        log.info { "Successfully reposted image with file id `${memeFile.id}` published at ${memeFile.published} and got response $postResponse" }
    }
    log.info("======== Done reposting ========")
}

private fun getLatestMemeFiles(): List<MemeImageDto> = transaction {
    val checkInternalInMinutes = Instant.now().atZone(ZoneOffset.UTC).minute + 5
    val sql = """
                select image.file_id, file, meme.published 
                from meme join image on meme.file_id = image.file_id
                         where published >=(date_trunc('hour',NOW()::timestamp) - INTERVAL '$checkInternalInMinutes minute')
                """.trimIndent()
    val memeImages = DbUtils.executeAndTransform(sql) { rs -> MemeImage.create(rs) }
    return@transaction DbUtils.saveFileAndConvertToDto(memeImages)
}

private fun restoreOldMeme(): List<MemeImageDto> {
    val currentCounter = getOldMemeRestoreCounter()
    log.info { "Restoring old meme number $currentCounter" }
    return getOldMemeFileToRestore(currentCounter)
}

private fun getOldMemeFileToRestore(counter: Int): List<MemeImageDto> = transaction {
    val sql = """
                select image.file_id, file, meme.published, meme.created
                    from meme join image on meme.file_id = image.file_id
                    where meme.created < TO_DATE('2023-04-29','YYYY-MM-DD')
                    order by meme.created desc limit 1 offset $counter;
                """.trimIndent()
    val memeImages = DbUtils.executeAndTransform(sql) { rs -> MemeImage.create(rs) }
    return@transaction DbUtils.saveFileAndConvertToDto(memeImages)
}

private fun getOldMemeRestoreCounter(): Int = transaction {
    val sql = """
                SELECT nextval('vk_meme_restore_sequence');
                """.trimIndent()
    return@transaction DbUtils.executeAndTransform(sql) { rs -> rs.getInt("nextval") }.first()
}
