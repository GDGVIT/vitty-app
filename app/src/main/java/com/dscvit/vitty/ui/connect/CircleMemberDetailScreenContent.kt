package com.dscvit.vitty.ui.connect

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.schedule.ScheduleViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Quote
import com.dscvit.vitty.util.VITMap
import com.dscvit.vitty.widget.parseTimeToTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleMemberDetailScreenContent(
    member: UserResponse,
    circleId: String,
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var quote by remember { mutableStateOf("") }
    var memberTimetableData by remember { mutableStateOf<Map<Int, List<PeriodDetails>>>(emptyMap()) }
    var isLoadingTimetable by remember { mutableStateOf(false) }
    var hasLoadedData by remember { mutableStateOf(false) }

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

    LaunchedEffect(member.username, circleId) {
        if (hasLoadedData) return@LaunchedEffect

        quote = Quote.getLine(context)

        val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""

        if (token.isNotEmpty()) {
            isLoadingTimetable = true
            scheduleViewModel.getCircleTimeTable(token, circleId, member.username)
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
                            memberTimetableData = processedData
                            isLoadingTimetable = false
                            hasLoadedData = true
                        }
                    } catch (e: Exception) {
                        Timber.e("Error processing member timetable: ${e.message}")
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
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Member Profile",
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

        CircleMemberProfileCard(member = member)

        Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = TextColor,
                    )
                }
            },
        ) {
            days.forEachIndexed { index, day ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = day,
                            fontFamily = Poppins,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 20.sp,
                            lineHeight = (20 * 1.4).sp,
                            color = if (pagerState.currentPage == index) TextColor else Accent,
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
                ) {
                    CircularProgressIndicator()
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    DayScheduleContent(
                        periods = memberTimetableData[page] ?: emptyList(),
                        dayIndex = page,
                        quote = quote,
                    )
                }
            }
        }
    }
}

@Composable
fun CircleMemberProfileCard(member: UserResponse) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (member.picture.isNotEmpty()) {
                    AsyncImage(
                        model = member.picture,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape),
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
                                member.name
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
                        text = member.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "@${member.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
    val context = LocalContext.current

    if (periods.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 32.dp),
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
        ) {
            items(
                items = periods,
                key = { period -> period.id },
            ) { period ->
                CircleMemberPeriodCard(
                    period = period,
                    dayIndex = dayIndex,
                    onLocationClick = { roomNo ->
                        VITMap.openClassMap(context, roomNo)
                    },
                )
            }
        }
    }
}

@Composable
private fun CircleMemberPeriodCard(
    period: PeriodDetails,
    dayIndex: Int,
    onLocationClick: (String) -> Unit,
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val startTimeStr =
        remember(period.startTime) {
            timeFormat.format(period.startTime.toDate()).uppercase()
        }
    val endTimeStr =
        remember(period.endTime) {
            timeFormat.format(period.endTime.toDate()).uppercase()
        }

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

            val currentHourMinute = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
            val startHourMinute = startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)
            val endHourMinute = endTime.get(Calendar.HOUR_OF_DAY) * 60 + endTime.get(Calendar.MINUTE)

            currentHourMinute in startHourMinute..endHourMinute
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Secondary,
            ),
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
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
                                .clickable(
                                    onClick = { onLocationClick(period.roomNo) },
                                ).border(
                                    width = 1.dp,
                                    color = Accent,
                                    shape = RoundedCornerShape(9999.dp),
                                ).padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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

private suspend fun processFriendTimetableData(
    friend: com.dscvit.vitty.network.api.community.responses.timetable.TimetableResponse,
): Map<Int, List<PeriodDetails>> =
    withContext(Dispatchers.Default) {
        val timetableData = friend.data
        val dayNames = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

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
                    }?.sortedBy { it.startTime.toDate() } ?: emptyList()

            result[index] = periods
        }

        result
    }
