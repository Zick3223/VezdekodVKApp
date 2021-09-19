package com.contest.vezdekod.requests

import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.contest.vezdekod.model.VKGroup
import com.contest.vezdekod.model.VKUser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder

class VKUsersGet(private val uids: IntArray = intArrayOf()) : ApiCommand<List<VKUser>>() {
    override fun onExecute(manager: VKApiManager): List<VKUser> {
        if (uids.isEmpty()) {
            // if no uids, send user's data
            val call = VKMethodCall.Builder()
                .method("users.get")
                .args("fields", "photo_200,first_name,last_name,counters")
                .version(manager.config.version)
                .build()
            return manager.execute(call, ResponseApiParser())
        } else {
            val result = ArrayList<VKUser>()
            val chunks = uids.toList().chunked(CHUNK_LIMIT)
            for (chunk in chunks) {
                val call = VKMethodCall.Builder()
                    .method("users.get")
                    .args("user_ids", chunk.joinToString(","))
                    .args("fields", "photo_200,first_name,last_name,counters")
                    .version(manager.config.version)
                    .build()
                result.addAll(manager.execute(call, ResponseApiParser()))
            }
            return result
        }
    }

    companion object {
        const val CHUNK_LIMIT = 900
    }

    private class ResponseApiParser : VKApiResponseParser<List<VKUser>> {
        override fun parse(response: String): List<VKUser> {
            try {
                val ja = JSONObject(response).getJSONArray("response")
                val r = ArrayList<VKUser>(ja.length())
                for (i in 0 until ja.length()) {
                    val user = ja.getJSONObject(i).toString()
                    val res = Klaxon().parse<VKUser>(user)!!
                    r.add(res)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            } catch (ex: Exception) {
                throw Exception()
            }
        }
    }
}

class VKGroupsGet(private val uids: IntArray = intArrayOf()) : ApiCommand<List<VKGroup>>() {
    override fun onExecute(manager: VKApiManager): List<VKGroup> {
        val result = ArrayList<VKGroup>()
        val chunks = uids.toList().chunked(CHUNK_LIMIT)
        for (chunk in chunks) {
            val call = VKMethodCall.Builder()
                .method("groups.getById")
                .args("group_ids", chunk.joinToString(","))
                .args("fields", "photo_50,name")
                .version(manager.config.version)
                .build()
            result.addAll(manager.execute(call, ResponseApiParser()))
        }
        return result
    }

    companion object {
        const val CHUNK_LIMIT = 900
    }

    private class ResponseApiParser : VKApiResponseParser<List<VKGroup>> {
        override fun parse(response: String): List<VKGroup> {
            try {
                val ja = JSONObject(response).getJSONArray("response")
                val r = ArrayList<VKGroup>(ja.length())
                for (i in 0 until ja.length()) {
                    val user = ja.getJSONObject(i).toString()
                    val res = Klaxon().parse<VKGroup>(user)!!
                    r.add(res)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            } catch (ex: Exception) {
                throw Exception()
            }
        }
    }
}