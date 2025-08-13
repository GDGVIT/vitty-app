package com.dscvit.vitty.ui.academics.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Green
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.academics.models.Course
import com.dscvit.vitty.ui.connect.components.FilterChip
import com.dscvit.vitty.ui.coursepage.components.RemindersChip
import com.dscvit.vitty.ui.coursepage.models.Reminder
import com.dscvit.vitty.ui.coursepage.models.ReminderStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun AcademicsHeader(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isCurrentSemester: Boolean,
    onSemesterFilterChange: (Boolean) -> Unit,
    reminderStatus: Int,
    onReminderStatusChange: (Int) -> Unit,
    reminderSearchQuery: String = "",
    onReminderSearchQueryChange: (String) -> Unit = {},
    courses: List<Course> = emptyList(),
    onCourseSelected: (Course) -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(bottom = 16.dp),
    ) {
        AcademicsTabRow(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
        )

        Spacer(Modifier.height(20.dp))

        when (selectedTab) {
            0 ->
                CoursesTabFilters(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    isCurrentSemester = isCurrentSemester,
                    onSemesterFilterChange = onSemesterFilterChange,
                )
            1 ->
                RemindersTabFilters(
                    reminderStatus = reminderStatus,
                    onReminderStatusChange = onReminderStatusChange,
                    searchQuery = reminderSearchQuery,
                    onSearchQueryChange = onReminderSearchQueryChange,
                    courses = courses,
                    onCourseSelected = onCourseSelected,
                )
        }
    }
}

@Composable
fun AcademicsTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    TabRow(
        modifier = Modifier.padding(horizontal = 20.dp),
        selectedTabIndex = selectedTab,
        containerColor = Background,
        contentColor = TextColor,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier =
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(3.dp),
                color = TextColor,
            )
        },
        divider = {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Secondary),
            )
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tab,
                        style =
                            if (selectedTab == index) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                        color = if (selectedTab == index) TextColor else Accent,
                    )
                },
                selectedContentColor = TextColor,
            )
        }
    }
}

@Composable
fun CoursesTabFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isCurrentSemester: Boolean,
    onSemesterFilterChange: (Boolean) -> Unit,
) {
    Column {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
        )
    }
}

@Composable
fun RemindersTabFilters(
    reminderStatus: Int,
    onReminderStatusChange: (Int) -> Unit,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    courses: List<Course> = emptyList(),
    onCourseSelected: (Course) -> Unit = {},
) {
    var showCourseSelectionBottomSheet by remember { mutableStateOf(false) }

    Column {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            placeholder = "Search",
        )

        Spacer(Modifier.height(16.dp))

        FilterChipRowWithAddButton(
            options = listOf("Pending", "Completed"),
            selectedIndex = reminderStatus,
            onSelectionChange = onReminderStatusChange,
            onAddClick = { showCourseSelectionBottomSheet = true },
        )
    }

    if (showCourseSelectionBottomSheet) {
        CourseSelectionBottomSheet(
            courses = courses,
            onDismiss = { showCourseSelectionBottomSheet = false },
            onCourseSelected = { course ->
                showCourseSelectionBottomSheet = false
                onCourseSelected(course)
            },
        )
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholder: String = "Search",
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(2.dp, Secondary, RoundedCornerShape(9999.dp))
            .background(Background, RoundedCornerShape(9999.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(Accent),
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = TextColor,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                    ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Accent.copy(alpha = 0.3f),
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 16.sp,
                                    ),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Accent,
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipRowWithAddButton(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row {
            options.forEachIndexed { index, label ->
                FilterChip(
                    label = label,
                    isSelected = selectedIndex == index,
                    onClick = { onSelectionChange(index) },
                )
                if (index < options.lastIndex) {
                    Spacer(Modifier.width(12.dp))
                }
            }
        }

        AddButton(onClick = onAddClick)
    }
}

@Composable
fun AddButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(Secondary)
                .border(1.dp, Accent, CircleShape)
                .clickable { onClick() }
                .padding(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Reminder",
                tint = Accent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AcademicsContent(
    selectedTab: Int,
    courses: List<Course>,
    reminders: List<Reminder> = emptyList(),
    reminderStatus: Int,
    reminderSearchQuery: String = "",
    onCourseClick: (Course) -> Unit,
    onToggleReminderComplete: (Long, Boolean) -> Unit = { _, _ -> },
    onDeleteReminder: (Reminder) -> Unit = { },
) {
    when (selectedTab) {
        0 -> CoursesContent(courses = courses, onCourseClick = onCourseClick)
        1 ->
            RemindersContent(
                reminders = reminders,
                reminderStatus = reminderStatus,
                searchQuery = reminderSearchQuery,
                onToggleReminderComplete = onToggleReminderComplete,
                onDeleteReminder = onDeleteReminder,
            )
    }
}

@Composable
fun CoursesContent(
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
    ) {
        item {
            Spacer(Modifier.height(16.dp))
        }
        items(courses) { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course) },
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Secondary)
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = course.title,
                        color = TextColor,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (course.isStarred) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = Accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = course.details,
                    color = Accent,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Course",
                tint = Accent,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun RemindersContent(
    reminders: List<Reminder>,
    reminderStatus: Int,
    searchQuery: String = "",
    onToggleReminderComplete: (Long, Boolean) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
) {
    val statusFilteredReminders =
        reminders.filter { reminder ->
            when (reminderStatus) {
                0 -> reminder.status != ReminderStatus.COMPLETED
                1 -> reminder.status == ReminderStatus.COMPLETED
                else -> true
            }
        }

    val filteredReminders =
        if (searchQuery.isNotEmpty()) {
            statusFilteredReminders.filter { reminder ->
                reminder.title.contains(searchQuery, ignoreCase = true) ||
                    reminder.description.contains(searchQuery, ignoreCase = true) ||
                    reminder.courseTitle.contains(searchQuery, ignoreCase = true)
            }
        } else {
            statusFilteredReminders
        }

    if (filteredReminders.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text =
                        when {
                            searchQuery.isNotEmpty() -> "No reminders found"
                            reminderStatus == 0 -> "No pending reminders"
                            else -> "No completed reminders"
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextColor.copy(alpha = 0.6f),
                )
            }
        }
    } else {
        val groupedReminders = groupRemindersByDate(filteredReminders)

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
        ) {
            groupedReminders.forEach { (dateHeader, remindersList) ->
                item {
                    DateHeaderWithChip(dateHeader = dateHeader, remindersList = remindersList)
                }

                items(remindersList, key = { it.id }) { reminder ->
                    SwipeableReminderCard(
                        reminder = reminder,
                        onToggleComplete = onToggleReminderComplete,
                        onDelete = onDeleteReminder,
                    )
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                Spacer(Modifier.height(120.dp))
            }
        }
    }
}

fun groupRemindersByDate(reminders: List<Reminder>): LinkedHashMap<String, List<Reminder>> =
    reminders
        .sortedBy { it.dateMillis }
        .groupBy { reminder ->
            val reminderDate =
                try {
                    LocalDate.parse(reminder.date)
                } catch (e: Exception) {
                    LocalDateTime
                        .ofEpochSecond(
                            reminder.dateMillis / 1000,
                            0,
                            java.time.ZoneOffset
                                .systemDefault()
                                .rules
                                .getOffset(java.time.Instant.ofEpochMilli(reminder.dateMillis)),
                        ).toLocalDate()
                }

            reminderDate.format(DateTimeFormatter.ofPattern("dd MMMM"))
        }.toList()
        .sortedBy { (_, remindersList) ->

            remindersList.firstOrNull()?.dateMillis ?: 0L
        }.toMap(LinkedHashMap())

@RequiresApi(Build.VERSION_CODES.O)
fun getDateChipInfo(reminderDate: LocalDate): Pair<String, Color> {
    val today = LocalDate.now()
    val daysFromNow = ChronoUnit.DAYS.between(today, reminderDate)

    return when {
        reminderDate.isEqual(today) -> "Today" to Red
        reminderDate.isEqual(today.plusDays(1)) -> "Tomorrow" to Red
        daysFromNow in 1..2 -> "$daysFromNow days to go" to Red
        daysFromNow > 2 -> "$daysFromNow days to go" to com.dscvit.vitty.theme.Yellow
        reminderDate.isBefore(today) -> {
            val daysPast = ChronoUnit.DAYS.between(reminderDate, today)
            if (daysPast == 1L) {
                "Yesterday" to com.dscvit.vitty.theme.Yellow
            } else {
                "$daysPast days ago" to com.dscvit.vitty.theme.Yellow
            }
        }
        else -> "" to Color.Transparent
    }
}

@Composable
fun SwipeableReminderCard(
    reminder: Reminder,
    onToggleComplete: (Long, Boolean) -> Unit,
    onDelete: (Reminder) -> Unit,
) {
    var isDismissed by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        isDismissed = true
                        true
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        if (reminder.status != ReminderStatus.COMPLETED) {
                            isCompleted = true
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            },
        )

    LaunchedEffect(isDismissed) {
        if (isDismissed) {
            kotlinx.coroutines.delay(200)
            onDelete(reminder)
        }
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            kotlinx.coroutines.delay(200)
            onToggleComplete(reminder.id, true)
        }
    }

    AnimatedVisibility(
        visible = !isDismissed && !isCompleted,
        exit = fadeOut(animationSpec = tween(200)),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        if (reminder.status != ReminderStatus.COMPLETED) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = Green,
                                            shape = RoundedCornerShape(20.dp),
                                        ).padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Complete",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = Red,
                                        shape = RoundedCornerShape(20.dp),
                                    ).padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    else -> {
                    }
                }
            },
            enableDismissFromStartToEnd = reminder.status != ReminderStatus.COMPLETED,
            enableDismissFromEndToStart = true,
        ) {
            ReminderCardContent(
                reminder = reminder,
                onClick = {
                    if (reminder.status != ReminderStatus.COMPLETED) {
                        onToggleComplete(reminder.id, true)
                    }
                },
            )
        }
    }
}

@Composable
fun ReminderCardContent(
    reminder: Reminder,
    onClick: () -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Secondary)
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = if (reminder.title.isNotEmpty()) reminder.title else "Reminder",
                color = TextColor,
                style = MaterialTheme.typography.labelLarge,
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = reminder.courseTitle.ifEmpty { "Course" },
                    color = Accent,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateHeaderWithChip(
    dateHeader: String,
    remindersList: List<Reminder>,
) {
    val firstReminder = remindersList.firstOrNull() ?: return
    val reminderDate =
        try {
            LocalDate.parse(firstReminder.date)
        } catch (e: Exception) {
            LocalDateTime
                .ofEpochSecond(
                    firstReminder.dateMillis / 1000,
                    0,
                    java.time.ZoneOffset
                        .systemDefault()
                        .rules
                        .getOffset(java.time.Instant.ofEpochMilli(firstReminder.dateMillis)),
                ).toLocalDate()
        }

    val (chipText, _) = getDateChipInfo(reminderDate)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateHeader,
            style = MaterialTheme.typography.titleMedium,
            color = TextColor,
            fontWeight = FontWeight.SemiBold,
        )

        if (chipText.isNotEmpty()) {
            RemindersChip(
                text = chipText,
                reminder = firstReminder,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionBottomSheet(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onCourseSelected: (Course) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background,
        contentColor = TextColor,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(top = 16.dp)
                        .width(120.dp)
                        .height(7.dp)
                        .background(Accent.copy(alpha = .4f), shape = RoundedCornerShape(44.dp)),
            ) {
            }
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Select a Course",
                style = MaterialTheme.typography.titleLarge,
                color = TextColor,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            LazyColumn(
                modifier = Modifier.wrapContentHeight(),
            ) {
                items(courses) { course ->
                    CourseItem(
                        course = course,
                        onClick = {
                            onCourseSelected(course)
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CourseItem(
    course: Course,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Secondary)
                .clickable { onClick() }
                .padding(16.dp),
    ) {
        Text(
            text = course.title,
            color = TextColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
