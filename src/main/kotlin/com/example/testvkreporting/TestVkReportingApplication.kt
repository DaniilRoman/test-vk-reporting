package com.example.testvkreporting

import com.vk.api.sdk.client.TransportClient
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import java.io.File

class TestClass

fun main() {
    val userId = Integer.valueOf(System.getenv("VK_USER_ID"))
    val groupId = Integer.valueOf(System.getenv("VK_GROUP_ID"))
    val accessToken = System.getenv("VK_ACCESS_TOKEN")

    val pictureAsBytes = TestClass::class.java.getResourceAsStream("/test.png")!!.readAllBytes()
    val testPictureFile: File = File.createTempFile("test", ".png")
    testPictureFile.writeBytes(pictureAsBytes)

    val transportClient: TransportClient = HttpTransportClient()
    val vk = VkApiClient(transportClient)
    val actor = UserActor(userId, accessToken)


    val serverResponse = vk.photos().getWallUploadServer(actor).execute()
    val uploadResponse = vk.upload().photoWall(serverResponse.uploadUrl.toString(), testPictureFile).execute()
    val photoList = vk.photos().saveWallPhoto(actor, uploadResponse.photo)
        .server(uploadResponse.server)
        .hash(uploadResponse.hash)
        .execute()

    val photo = photoList[0]
    val attachId = "photo" + photo.ownerId + "_" + photo.id
    val postResponse = vk.wall().post(actor)
        .ownerId(groupId)
        .fromGroup(true)
        .attachments(attachId)
        .execute()

    println("+++++++++++++++++++++++++++++")
    println(postResponse)
}


