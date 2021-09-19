package com.contest.vezdekod

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.contest.vezdekod.model.VKGroup
import com.contest.vezdekod.requests.VKGroupsGet
import com.contest.vezdekod.ui.theme.VezdekodTheme
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGetResponse

class GroupBrowser : ComponentActivity() {
    var groups: MutableState<List<VKGroup>?> = mutableStateOf(null)
    lateinit var imageLoader: ImageLoader
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLoader = ImageLoader.getInstance()
        imageLoader.init(ImageLoaderConfiguration.createDefault(this))
        setContent {
            VezdekodTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ShowDescription()
                    ListGroups(groupsState = groups)
                }
            }
        }
        VK.execute(
            GroupsService().groupsGet(userId = UserId(VK.getUserId().toLong())),
            //GroupsService().groupsGet(userId = UserId(1332614)),
            callback = object : VKApiCallback<GroupsGetResponse> {
                override fun fail(error: Exception) {
                    error.printStackTrace()
                }

                override fun success(result: GroupsGetResponse) {
                    VK.execute(
                        VKGroupsGet(result.items.toIntArray()),
                        callback = object : VKApiCallback<List<VKGroup>> {
                            override fun fail(error: Exception) {
                                TODO("Not yet implemented")
                            }

                            override fun success(result: List<VKGroup>) {
                                for (group in result) {
                                    imageLoader.loadImage(group.photo_50,
                                        object : SimpleImageLoadingListener() {
                                            override fun onLoadingComplete(
                                                imageUri: String?,
                                                view: View?,
                                                loadedImage: Bitmap?
                                            ) {
                                                group.bitmapPhoto =
                                                    loadedImage!!.asImageBitmap()
                                            }
                                        })
                                }
                                groups.value = result
                            }
                        })
                }
            })
    }
}

@Composable
fun ShowDescription() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(50.dp)) {
        Text(
            text = "Отписаться от сообществ",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp)
        )
        Text(
            text = "Коснитесь и удерживаете, чтобы сменить аватарки",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ListGroups(groupsState: MutableState<List<VKGroup>?>) {
    val groups = remember { groupsState }
    val scrollState = rememberScrollState()
    if (groups.value != null) {
        val groupList = groups.value!!
        val rowNum = groupList.size / 3
        Column(
            Modifier
                .offset(0.dp, 150.dp)
                .verticalScroll(scrollState)
        ) {
            for (i in 0..rowNum step 3) {
                Row {
                    for (j in 0 until (3 - rowNum % 3)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.size(150.dp, 300.dp)
                        ) {
                            if (groupList[i + j].bitmapPhoto != null) {
                                Image(
                                    bitmap = groupList[i + j].bitmapPhoto!!,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(
                                            CircleShape
                                        )
                                )
                            } else {
                                CircularProgressIndicator()
                            }
                            Text(text = groupList[i + j].name!!, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    } else {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .offset(0.dp, 175.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    VezdekodTheme {
        ShowDescription()
    }
}