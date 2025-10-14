package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class RoutesResponse(
    @SerializedName("data")
    val data: List<Route>
)

data class RouteResponse(
    @SerializedName("data")
    val data: Route
)
