package com.dscvit.vitty.ui.schedule

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.dscvit.vitty.activity.SettingsActivity
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Quote
import com.dscvit.vitty.util.UtilFunctions
import com.dscvit.vitty.util.VITMap
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
fun ScheduleScreenContent(
    onOpenDrawer: () -> Unit = {},
    onCardClick: (String, String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val scope = rememberCoroutineScope()

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

    var allDaysData by remember { mutableStateOf<Map<Int, List<PeriodDetails>>>(emptyMap()) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var quote by remember { mutableStateOf("") }
    var hasLoadedData by remember { mutableStateOf(false) }

    var showExamModeAlert by remember { mutableStateOf(prefs.getBoolean(Constants.EXAM_MODE, false)) }
    var satModeSettingKey by remember { mutableStateOf(UtilFunctions.getSatModeCode()) }

    DisposableEffect(context) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    Constants.EXAM_MODE -> {
                        showExamModeAlert = prefs.getBoolean(Constants.EXAM_MODE, false)
                    }
                    satModeSettingKey -> {
                        scope.launch {
                            withContext(Dispatchers.Default) {
                                val cachedData = prefs.getString(Constants.CACHE_COMMUNITY_TIMETABLE, null)
                                if (cachedData != null) {
                                    try {
                                        val response = Gson().fromJson(cachedData, UserResponse::class.java)
                                        val processedData = processAllDaysData(response, prefs)
                                        withContext(Dispatchers.Main) {
                                            allDaysData = processedData
                                        }
                                    } catch (e: Exception) {
                                        Timber.e("Error refreshing schedule data: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(hasLoadedData) {
        if (hasLoadedData) return@LaunchedEffect
        withContext(Dispatchers.Default) {
            quote =
                try {
                    Quote.getLine(context)
                } catch (e: Exception) {
                    Constants.DEFAULT_QUOTE
                }

            val cachedData = prefs.getString(Constants.CACHE_COMMUNITY_TIMETABLE, null)
            if (cachedData != null) {
                try {
                    val response = Gson().fromJson(cachedData, UserResponse::class.java)
                    val processedData = processAllDaysData(response, prefs)
                    withContext(Dispatchers.Main) {
                        allDaysData = processedData
                        isInitialLoading = false
                    }
                } catch (e: Exception) {
                    Timber.e("Error loading cached data: ${e.message}")
                }
            }
        }

        val token = prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
        val username = prefs.getString(Constants.COMMUNITY_USERNAME, "") ?: ""

        if (token.isNotEmpty() && username.isNotEmpty()) {
            scheduleViewModel.getUserWithTimeTable(token, username)
        } else {
            isInitialLoading = false
        }

        hasLoadedData = true
    }

    val userResponse by scheduleViewModel.user.observeAsState()
    LaunchedEffect(userResponse) {
        userResponse?.let { response ->
            withContext(Dispatchers.Default) {
                val responseJson = Gson().toJson(response)
                prefs.edit { putString(Constants.CACHE_COMMUNITY_TIMETABLE, responseJson) }

                val processedData = processAllDaysData(response, prefs)
                withContext(Dispatchers.Main) {
                    allDaysData = processedData
                    isInitialLoading = false
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            actions = {
                val profilePicture = prefs.getString(Constants.COMMUNITY_PICTURE, "")
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile Picture",
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onOpenDrawer() },
                        placeholder = painterResource(R.drawable.ic_gdscvit),
                        error = painterResource(R.drawable.ic_gdscvit),
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
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
            if (isInitialLoading) {
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
                    DayContent(
                        periods = allDaysData[page] ?: emptyList(),
                        quote = quote,
                        dayIndex = page,
                        onCardClick = onCardClick,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showExamModeAlert,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }.background(Secondary),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_snooze_notifications),
                        contentDescription = "Exam Mode",
                        tint = Accent,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "No Classes Mode Turned On",
                        color = Accent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayContent(
    periods: List<PeriodDetails>,
    quote: String,
    dayIndex: Int,
    onCardClick: (String, String) -> Unit = { _, _ -> },
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
            contentPadding = PaddingValues(top = 16.dp, bottom = 136.dp),
        ) {
            items(
                items = periods,
                key = { period -> period.id },
            ) { period ->
                PeriodCard(
                    period = period,
                    dayIndex = dayIndex,
                    onLocationClick = { roomNo ->
                        VITMap.openClassMap(context, roomNo)
                    },
                    onCardClick = onCardClick,
                )
            }
        }
    }
}

@Composable
private fun PeriodCard(
    period: PeriodDetails,
    dayIndex: Int,
    onLocationClick: (String) -> Unit,
    onCardClick: (String, String) -> Unit = { _, _ -> },
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
        onClick = {
            onCardClick(period.courseName, period.courseCode)
        },
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

private fun processAllDaysData(
    userResponse: UserResponse,
    prefs: SharedPreferences,
): Map<Int, List<PeriodDetails>> {
    val timetableData = userResponse.timetable?.data ?: return emptyMap()
    val dayNames = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    val result = mutableMapOf<Int, List<PeriodDetails>>()

    dayNames.forEachIndexed { index, dayName ->
        val actualDayName =
            if (dayName == "saturday") {
                prefs.getString(UtilFunctions.getSatModeCode(), "saturday") ?: "saturday"
            } else {
                dayName
            }

        val courses =
            when (actualDayName) {
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

    return result
}
