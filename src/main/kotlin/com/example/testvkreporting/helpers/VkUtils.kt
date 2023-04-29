package com.example.testvkreporting.helpers

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.objects.photos.responses.SaveWallPhotoResponse
import com.vk.api.sdk.objects.wall.responses.PostResponse
import java.io.File

class VkUtils {
    companion object {
        fun postPhotoToChannel(
            photoList: MutableList<SaveWallPhotoResponse>,
            vk: VkApiClient,
            actor: UserActor,
            groupId: Int?
        ): PostResponse? {
            val photo = photoList[0]
            val attachId = "photo" + photo.ownerId + "_" + photo.id
            val postResponse = vk.wall().post(actor)
                .ownerId(groupId)
                .fromGroup(true)
                .attachments(attachId)
                .execute()
            return postResponse
        }

        fun uploadPhotoToVk(
            vk: VkApiClient,
            actor: UserActor,
            memeFile: File
        ): MutableList<SaveWallPhotoResponse> {
            val serverResponse = vk.photos().getWallUploadServer(actor).execute()
            val uploadResponse = vk.upload().photoWall(serverResponse.uploadUrl.toString(), memeFile).execute()
            return vk.photos().saveWallPhoto(actor, uploadResponse.photo)
                .server(uploadResponse.server)
                .hash(uploadResponse.hash)
                .execute()
        }
    }
}
