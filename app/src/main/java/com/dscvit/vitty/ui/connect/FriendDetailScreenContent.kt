package com.dscvit.vitty.ui.connect

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.network.api.community.responses.timetable.TimetableResponse
import com.dscvit.vitty.network.api.community.responses.user.ActiveFriendItem
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.schedule.ScheduleViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Quote
import com.dscvit.vitty.widget.parseTimeToTimestamp
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreenContent(
    friend: UserResponse,
    onBackClick: () -> Unit = {},
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var quote by remember { mutableStateOf("") }
    var friendTimetableData by remember {
        mutableStateOf<Map<Int, List<PeriodDetails>>>(emptyMap())
    }
    var isLoadingTimetable by remember { mutableStateOf(false) }
    var hasLoadedData by remember { mutableStateOf(false) }
    var hasUnfriended by remember { mutableStateOf(false) }
    var isSendingRequest by remember { mutableStateOf(false) }
    var hasRequestSent by remember { mutableStateOf(false) }
    var showUnfriendDialog by remember { mutableStateOf(false) }

    var isFriendGhosted by remember { mutableStateOf(false) }
    var isTogglingGhostMode by remember { mutableStateOf(false) }

    val unfriendSuccess by connectViewModel.unfriendSuccess.observeAsState()
    val sendRequestResponse by connectViewModel.sendRequestResponse.observeAsState()
    val activeFriends by connectViewModel.activeFriends.observeAsState()
    val ghostModeResponse by connectViewModel.ghostModeResponse.observeAsState()

    LaunchedEffect(activeFriends) {
        Timber.d("Active friends: $activeFriends")
        activeFriends?.let { activeList ->
            val friendItem = activeList.find { it.friend_username == friend.username }
            isFriendGhosted = friendItem?.hide ?: false
        }
    }

    LaunchedEffect(ghostModeResponse) {
        if (ghostModeResponse != null && isTogglingGhostMode) {
            isTogglingGhostMode = false

            if (ghostModeResponse!!.success) {
                val currentActiveFriends = activeFriends?.toMutableList() ?: mutableListOf()

                val existingFriendIndex = currentActiveFriends.indexOfFirst { it.friend_username == friend.username }

                if (existingFriendIndex != -1) {
                    currentActiveFriends[existingFriendIndex] =
                        currentActiveFriends[existingFriendIndex].copy(hide = isFriendGhosted)
                } else {
                    currentActiveFriends.add(ActiveFriendItem(friend_username = friend.username, hide = isFriendGhosted))
                }

                Toast
                    .makeText(
                        context,
                        if (isFriendGhosted) {
                            "${friend.name} is now ghosted"
                        } else {
                            "${friend.name} is now visible"
                        },
                        Toast.LENGTH_SHORT,
                    ).show()
                connectViewModel.updateActiveFriendsList(currentActiveFriends)
            } else {
                isFriendGhosted = !isFriendGhosted
            }

            connectViewModel.clearGhostModeResponse()
        }
    }

    LaunchedEffect(unfriendSuccess) {
        if (unfriendSuccess == friend.username) {
            hasUnfriended = true
            connectViewModel.clearUnfriendSuccess()
        }
    }

    LaunchedEffect(sendRequestResponse) {
        if (sendRequestResponse != null && isSendingRequest) {
            isSendingRequest = false
            if (sendRequestResponse!!.detail == "Friend request sent successfully") {
                hasRequestSent = true
                connectViewModel.clearSendRequestResponse()

                val sharedPreferences =
                    context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""
                if (token.isNotEmpty() && username.isNotEmpty()) {
                    connectViewModel.refreshFriendList(token, username)
                }
            } else {
                connectViewModel.clearSendRequestResponse()
            }
        }
    }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val currentDay =
        remember {
            when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
        }

    val pagerState =
        rememberPagerState(
            initialPage = currentDay,
            pageCount = { 7 },
        )

    LaunchedEffect(friend.username) {
        if (hasLoadedData) return@LaunchedEffect

        quote = Quote.getLine(context)

        val sharedPreferences =
            context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""

        if (token.isNotEmpty()) {
            isLoadingTimetable = true
            scheduleViewModel.getTimeTable(token, friend.username)
        } else {
            hasLoadedData = true
        }
    }

    val timetableResponse by scheduleViewModel.timetable.observeAsState()

    LaunchedEffect(timetableResponse) {
        timetableResponse?.let { response ->
            scope.launch {
                withContext(Dispatchers.Default) {
                    try {
                        val processedData = processFriendTimetableData(response)
                        withContext(Dispatchers.Main) {
                            friendTimetableData = processedData
                            isLoadingTimetable = false
                            hasLoadedData = true
                        }
                    } catch (e: Exception) {
                        Timber.e("Error processing friend timetable: ${e.message}")
                        withContext(Dispatchers.Main) {
                            isLoadingTimetable = false
                            hasLoadedData = true
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Background),
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Friend Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_chevron_left),
                        contentDescription = "Back",
                        tint = TextColor,
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
        )

        FriendProfileCard(
            friend = friend,
            hasUnfriended = hasUnfriended,
            isSendingRequest = isSendingRequest,
            hasRequestSent = hasRequestSent,
            isFriendGhosted = isFriendGhosted,
            isTogglingGhostMode = isTogglingGhostMode,
            onUnfriendClick = { showUnfriendDialog = true },
            onSendRequestClick = {
                val sharedPreferences =
                    context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                if (token.isNotEmpty()) {
                    isSendingRequest = true
                    connectViewModel.sendRequest(token, friend.username)
                }
            },
            onToggleGhostMode = { newValue ->
                if (!isTogglingGhostMode) {
                    isTogglingGhostMode = true
                    isFriendGhosted = newValue

                    val sharedPreferences =
                        context.getSharedPreferences(
                            Constants.USER_INFO,
                            Context.MODE_PRIVATE,
                        )
                    val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""

                    connectViewModel.toggleGhostMode(token, friend.username, newValue)
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasUnfriended) {
            Text(
                text = "Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                divider = {},
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier =
                                Modifier.tabIndicatorOffset(
                                    tabPositions[pagerState.currentPage],
                                ),
                            color = TextColor,
                        )
                    }
                },
            ) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = day,
                                fontFamily = Poppins,
                                fontWeight =
                                    if (pagerState.currentPage == index) {
                                        FontWeight.Medium
                                    } else {
                                        FontWeight.Normal
                                    },
                                fontSize = 20.sp,
                                lineHeight = (20 * 1.4).sp,
                                color =
                                    if (pagerState.currentPage == index) {
                                        TextColor
                                    } else {
                                        Accent
                                    },
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            Box(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (isLoadingTimetable) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        DayScheduleContent(
                            periods = friendTimetableData[page] ?: emptyList(),
                            dayIndex = page,
                            quote = quote,
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_community_outline),
                        contentDescription = "No Access",
                        modifier = Modifier.size(64.dp),
                        tint = Accent.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "Schedule Not Available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text =
                            "You can no longer view ${friend.name}'s schedule as they are not in your friends list.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }

    if (showUnfriendDialog) {
        AlertDialog(
            onDismissRequest = { showUnfriendDialog = false },
            title = {
                Text(
                    text = "Unfriend ${friend.name}?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text =
                        "Are you sure you want to remove ${friend.name} from your friends list? " +
                            "You won't be able to see their schedule anymore.",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = Accent,
                        ),
                )
            },
            containerColor = Secondary,
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnfriendDialog = false
                        val sharedPreferences =
                            context.getSharedPreferences(
                                Constants.USER_INFO,
                                Context.MODE_PRIVATE,
                            )
                        val token =
                            sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "")
                                ?: ""
                        val username =
                            sharedPreferences.getString(
                                Constants.COMMUNITY_USERNAME,
                                "",
                            )
                                ?: ""
                        if (token.isNotEmpty()) {
                            connectViewModel.unfriend(token, friend.username, sharedPreferences)
                            connectViewModel.getFriendList(token, username)
                        }
                    },
                ) {
                    Text(
                        text = "Unfriend",
                        color = Red,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnfriendDialog = false },
                ) {
                    Text(
                        text = "Cancel",
                        color = TextColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

@Composable
fun FriendProfileCard(
    friend: UserResponse,
    hasUnfriended: Boolean,
    isSendingRequest: Boolean,
    hasRequestSent: Boolean,
    isFriendGhosted: Boolean,
    isTogglingGhostMode: Boolean,
    onUnfriendClick: () -> Unit,
    onSendRequestClick: () -> Unit,
    onToggleGhostMode: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (friend.picture.isNotEmpty()) {
                    AsyncImage(
                        model = friend.picture,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        placeholder = painterResource(R.drawable.ic_gdscvit),
                        error = painterResource(R.drawable.ic_gdscvit),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .size(64.dp)
                                .background(Accent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                friend.name
                                    .take(2)
                                    .map { it.uppercaseChar() }
                                    .joinToString(""),
                            color = TextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "@${friend.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasUnfriended) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text =
                                if (isFriendGhosted) {
                                    "Friend is ghosted"
                                } else {
                                    "Friend is visible"
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color =
                                if (isFriendGhosted) {
                                    Accent.copy(alpha = 0.6f)
                                } else {
                                    TextColor
                                },
                        )
                        Text(
                            text =
                                if (isFriendGhosted) {
                                    "They won't see your activity"
                                } else {
                                    "They can see your activity"
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = Accent.copy(alpha = 0.7f),
                        )
                    }

                    Switch(
                        checked = isFriendGhosted,
                        onCheckedChange = onToggleGhostMode,
                        enabled = !isTogglingGhostMode,
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = Accent,
                                uncheckedThumbColor = Accent,
                                uncheckedTrackColor = Secondary,
                                checkedBorderColor = Accent,
                                uncheckedBorderColor = Accent,
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (hasUnfriended) {
                if (hasRequestSent) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Accent.copy(alpha = 0.1f),
                                contentColor = Accent.copy(alpha = 0.6f),
                            ),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Request Sent",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else {
                    Button(
                        onClick = onSendRequestClick,
                        enabled = !isSendingRequest,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (isSendingRequest) {
                                        Accent.copy(alpha = 0.1f)
                                    } else {
                                        Accent
                                    },
                                contentColor =
                                    if (isSendingRequest) {
                                        Accent.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.onPrimary
                                    },
                            ),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isSendingRequest) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Accent.copy(alpha = 0.7f),
                                )
                                Text(
                                    text = "Sending Request...",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        } else {
                            Text(
                                text = "Send Friend Request",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onUnfriendClick,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Red.copy(alpha = 0.1f),
                            contentColor = Red,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 1.dp,
                                color = Red,
                                shape = RoundedCornerShape(12.dp),
                            ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Unfriend",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayScheduleContent(
    periods: List<PeriodDetails>,
    dayIndex: Int,
    quote: String = "Every day is a new opportunity to learn and grow.",
) {
    if (periods.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_timetable_outline),
                    contentDescription = "No Classes",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No classes today!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
        ) {
            items(
                items = periods,
                key = { period -> period.id },
            ) { period ->
                FriendPeriodCard(
                    period = period,
                    dayIndex = dayIndex,
                )
            }
        }
    }
}

@Composable
private fun FriendPeriodCard(
    period: PeriodDetails,
    dayIndex: Int,
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val startTimeStr =
        remember(period.startTime) { timeFormat.format(period.startTime.toDate()).uppercase() }
    val endTimeStr =
        remember(period.endTime) { timeFormat.format(period.endTime.toDate()).uppercase() }

    val now = Calendar.getInstance()

    val isToday =
        remember(dayIndex) {
            val todayIndex =
                when (now.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    Calendar.SUNDAY -> 6
                    else -> -1
                }
            dayIndex == todayIndex
        }

    val isActive =
        if (!isToday) {
            false
        } else {
            val startTime = Calendar.getInstance().apply { time = period.startTime.toDate() }
            val endTime = Calendar.getInstance().apply { time = period.endTime.toDate() }
            val currentTime = Calendar.getInstance()

            val currentHourMinute =
                currentTime.get(Calendar.HOUR_OF_DAY) * 60 +
                    currentTime.get(Calendar.MINUTE)
            val startHourMinute =
                startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)
            val endHourMinute =
                endTime.get(Calendar.HOUR_OF_DAY) * 60 + endTime.get(Calendar.MINUTE)

            currentHourMinute in startHourMinute..endHourMinute
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        border =
            if (isActive) {
                BorderStroke(
                    1.dp,
                    Accent,
                )
            } else {
                null
            },
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = period.courseName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$startTimeStr - $endTimeStr | ${period.slot}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Accent,
                    fontWeight = FontWeight.Medium,
                )

                if (period.roomNo.isNotEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .border(
                                    width = 1.dp,
                                    color = Accent,
                                    shape = RoundedCornerShape(9999.dp),
                                ).padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_compass),
                                contentDescription = "Compass icon",
                                modifier = Modifier.size(12.dp),
                                alignment = Alignment.Center,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = period.roomNo,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        letterSpacing = (-0.12).sp,
                                        textAlign = TextAlign.Center,
                                    ),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun processFriendTimetableData(friend: TimetableResponse): Map<Int, List<PeriodDetails>> =
    withContext(Dispatchers.Default) {
        val timetableData = friend.data
        val dayNames =
            listOf(
                "monday",
                "tuesday",
                "wednesday",
                "thursday",
                "friday",
                "saturday",
                "sunday",
            )

        val result = mutableMapOf<Int, List<PeriodDetails>>()

        dayNames.forEachIndexed { index, dayName ->
            val courses =
                when (dayName) {
                    "monday" -> timetableData.Monday
                    "tuesday" -> timetableData.Tuesday
                    "wednesday" -> timetableData.Wednesday
                    "thursday" -> timetableData.Thursday
                    "friday" -> timetableData.Friday
                    "saturday" -> timetableData.Saturday
                    "sunday" -> timetableData.Sunday
                    else -> emptyList()
                }

            val periods =
                courses
                    ?.map { course ->
                        PeriodDetails(
                            courseCode = course.code,
                            courseName = course.name,
                            startTime = parseTimeToTimestamp(course.start_time),
                            endTime = parseTimeToTimestamp(course.end_time),
                            slot = course.slot,
                            roomNo = course.venue,
                        )
                    }?.sortedBy { it.startTime.toDate() }
                    ?: emptyList()

            result[index] = periods
        }

        result
    }
