package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * Friend data model
 */
data class Friend(
    val id: Int,
    val name: String,
    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String?
)

/**
 * Response from /user/friends endpoint
 */
data class FriendsResponse(
    val data: List<Friend>
)

/**
 * Response from /user/search endpoint
 */
data class UserSearchResponse(
    val data: List<Friend>
)

/**
 * Request body for adding a friend
 */
data class AddFriendRequest(
    @SerializedName("friend_id")
    val friendId: Int
)

/**
 * Response from adding a friend
 */
data class AddFriendResponse(
    val message: String,
    val data: FriendIdData
)

data class FriendIdData(
    val id: Int
)

/**
 * Response from removing a friend
 */
data class RemoveFriendResponse(
    val message: String
)
