package com.contest.vezdekod

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contest.vezdekod.model.VKUser
import com.contest.vezdekod.requests.VKUsersGet
import com.contest.vezdekod.ui.theme.VezdekodTheme
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.sdk.api.friends.FriendsService
import com.vk.sdk.api.friends.dto.FriendsGetFieldsResponse
import com.vk.sdk.api.users.dto.UsersFields
import com.vk.sdk.api.users.dto.UsersUserFull

class LoggedActivity : ComponentActivity() {
    var userState: MutableState<VKUser?> = mutableStateOf(null)
    val friendUsers: MutableState<List<UsersUserFull>?> = mutableStateOf(null)

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VezdekodTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ShowMyInfo(
                        imgState = this.userState,
                        friendsState = this.friendUsers,
                        {
                            startActivity(Intent(this, GroupBrowser::class.java))
                        })
                }
            }
        }
        val activity = this
        val imgLoader = ImageLoader.getInstance()
        imgLoader.init(ImageLoaderConfiguration.createDefault(this))

        VK.execute(VKUsersGet(), callback = object : VKApiCallback<List<VKUser>> {
            override fun fail(error: Exception) {
                Toast.makeText(activity, "Произошла ошибка загрузки данных", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun success(result: List<VKUser>) {
                imgLoader
                    .loadImage(result[0].photo_200, object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(
                            imageUri: String?,
                            view: View?,
                            loadedImage: Bitmap?
                        ) {
                            result[0].bitmapPhoto = loadedImage!!.asImageBitmap()
                            userState.value = result[0]
                            finally()
                        }
                    })
            }

            fun finally() {
                VK.execute(
                    FriendsService().friendsGet(
                        fields = listOf(
                            UsersFields.FIRST_NAME_NOM,
                            UsersFields.LAST_NAME_NOM
                        )
                    ),
                    callback = object : VKApiCallback<FriendsGetFieldsResponse> {
                        override fun fail(error: Exception) {
                            Toast.makeText(
                                activity,
                                "Ошибка получения списка друзей",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        override fun success(result: FriendsGetFieldsResponse) {
                            friendUsers.value = result.items
                        }
                    }
                )
            }
        })
    }
}


//class ProfileViewModel: ViewModel(){
//    private val _name = MutableLiveData(null as Bitmap?)
//    val name: LiveData<Bitmap?> = _name
//}

@ExperimentalMaterialApi
@Composable
fun DisplayPage(onSwipe: () -> Unit) {
    val SwipeableState = rememberSwipeableState(0)
    if (SwipeableState.currentValue >= 1) {
        onSwipe()
    }
    val squareSize = 48.dp
    val sizePx = with(LocalDensity.current) { squareSize.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1);
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .swipeable(
                SwipeableState,
                orientation = Orientation.Vertical,
                thresholds = { _, _ -> FractionalThreshold(0.1f) },
                anchors = anchors
            )
    ) {
    }
}

@Composable
fun ShowMyInfo(
    imgState: MutableState<VKUser?>,
    friendsState: MutableState<List<UsersUserFull>?>,
    groupsOnClick: () -> Unit
) {
    val user = remember { imgState }
    val friends = remember { friendsState }
    Row {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Друзей ",
                Modifier.padding(15.dp, 25.dp, 20.dp, 0.dp)
            )
            if (user.value?.counters != null) {
                Text(user.value!!.counters!!.friends.toString())
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally
            //               .padding(20.dp)
        ) {
            if (user.value?.bitmapPhoto != null) {
                Image(
                    bitmap = user.value!!.bitmapPhoto!!,
                    contentDescription = "Ваш аватар",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                CircularProgressIndicator()
            }
            if (user.value?.first_name != null) {
                Text(
                    user.value?.first_name!! + " " + user.value?.last_name!!
                )
            }

            if (friends.value != null) {
                Text(text = "Ваши друзья", Modifier.padding(0.dp, 30.dp, 0.dp, 8.dp))
                LazyColumn(
                    Modifier
                        .background(Color.LightGray)
                        .size(400.dp)
                ) {
                    items(friends.value!!) { item ->
                        Text(
                            item.firstName + " " + item.lastName,
                            modifier = Modifier.padding(10.dp, 0.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Сообществ ",
                Modifier
                    .padding(0.dp, 25.dp, 10.dp, 0.dp)
            )
            if (user.value?.counters != null) {
                Text(user.value!!.counters!!.groups.toString())
                Button(
                    onClick = {
                        groupsOnClick();
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .size(110.dp, 50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2787F5)),

                    ) {
                    Text(
                        "Мои сообщества",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    VezdekodTheme {

    }
}