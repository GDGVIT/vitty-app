package com.dscvit.vitty.api
import com.dscvit.vitty.model.EventDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
/**
 * API for getting User list from https://reqres.in/api/users?&page=1
 */
interface APIEvents {
    // https://reqres.in/api/users?page=1
    @GET("event?")
    fun getEvents(@QueryMap options: Map<String, String>): Call<EventDetails>
}
