package com.dscvit.vitty.ui.academics

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.academics.components.AcademicsContent
import com.dscvit.vitty.ui.academics.components.AcademicsHeader
import com.dscvit.vitty.ui.academics.models.Course
import com.dscvit.vitty.ui.coursepage.CoursePageViewModel
import com.dscvit.vitty.ui.coursepage.components.SetReminderBottomSheet
import com.dscvit.vitty.ui.coursepage.models.Reminder
import com.dscvit.vitty.util.SemesterUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicsScreenContent(
    modifier: Modifier = Modifier,
    profilePictureUrl: String?,
    allCourses: List<Course>,
    onCourseClick: (Course) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    academicsViewModel: AcademicsViewModel,
    coursePageViewModel: CoursePageViewModel,
) {
    val context = LocalContext.current
    val tabs = listOf("Courses", "Reminders")
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var reminderSearchQuery by remember { mutableStateOf("") }
    var isCurrentSemester by remember { mutableStateOf(true) }
    var reminderStatus by remember { mutableIntStateOf(0) }
    var showSetReminderBottomSheet by remember { mutableStateOf(false) }
    var selectedCourseForReminder by remember { mutableStateOf<Course?>(null) }
    val setReminderSheetState = rememberModalBottomSheetState()

    val allReminders by academicsViewModel.allReminders.collectAsStateWithLifecycle()

    val filteredCourses =
        remember(allCourses, searchQuery, isCurrentSemester) {
            allCourses.filter { course ->
                val matchesSearch =
                    searchQuery.isBlank() ||
                        course.title.contains(searchQuery, ignoreCase = true)

                if (isCurrentSemester) {
                    matchesSearch && course.semester == SemesterUtils.determineSemester()
                } else {
                    matchesSearch
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Academics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            actions = {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    AsyncImage(
                        model = profilePictureUrl,
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
        )

        AcademicsHeader(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isCurrentSemester = isCurrentSemester,
            onSemesterFilterChange = { isCurrentSemester = it },
            reminderStatus = reminderStatus,
            onReminderStatusChange = { reminderStatus = it },
            reminderSearchQuery = reminderSearchQuery,
            onReminderSearchQueryChange = { reminderSearchQuery = it },
            courses = allCourses,
            onCourseSelected = { course ->
                selectedCourseForReminder = course
                showSetReminderBottomSheet = true
            },
        )

        AcademicsContent(
            selectedTab = selectedTab,
            courses = filteredCourses,
            reminders = allReminders,
            reminderStatus = reminderStatus,
            reminderSearchQuery = reminderSearchQuery,
            onCourseClick = onCourseClick,
            onToggleReminderComplete = { reminderId: Long, isCompleted: Boolean ->
                academicsViewModel.updateReminderStatus(reminderId, isCompleted)
            },
            onDeleteReminder = { reminder: Reminder ->
                academicsViewModel.deleteReminder(reminder)
            },
        )
    }

    if (showSetReminderBottomSheet && selectedCourseForReminder != null) {
        ModalBottomSheet(
            onDismissRequest = { showSetReminderBottomSheet = false },
            sheetState = setReminderSheetState,
            containerColor = Background,
            contentColor = TextColor,
            dragHandle = { },
        ) {
            SetReminderBottomSheet(
                onDismiss = { showSetReminderBottomSheet = false },
                courseTitle = selectedCourseForReminder!!.title,
                onSaveReminder = {
                    title,
                    description,
                    dateMillis,
                    fromTime,
                    toTime,
                    isAllDay,
                    alertDaysBefore,
                    attachmentUrl,
                    ->
                    coursePageViewModel.setCourseId(selectedCourseForReminder!!.code)
                    coursePageViewModel.setCourseTitle(selectedCourseForReminder!!.title)

                    coursePageViewModel.addReminder(
                        title = title,
                        description = description,
                        dateMillis = dateMillis,
                        fromTime = fromTime,
                        toTime = toTime,
                        isAllDay = isAllDay,
                        alertDaysBefore = alertDaysBefore,
                        attachmentUrl = attachmentUrl,
                        onSuccess = {
                            showSetReminderBottomSheet = false
                            Toast
                                .makeText(
                                    context,
                                    "Reminder created successfully",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        },
                        onError = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        },
                    )
                },
            )
        }
    }
}
