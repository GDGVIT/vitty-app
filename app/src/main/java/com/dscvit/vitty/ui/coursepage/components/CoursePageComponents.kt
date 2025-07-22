package com.dscvit.vitty.ui.coursepage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Green
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.theme.Yellow
import com.dscvit.vitty.ui.coursepage.models.Note
import com.dscvit.vitty.ui.coursepage.models.NoteType
import com.dscvit.vitty.ui.coursepage.models.Reminder
import com.dscvit.vitty.ui.coursepage.models.ReminderStatus
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun NoteList(
    notes: List<Note>,
    onImageClick: (String) -> Unit = {},
    onStarClick: (Note) -> Unit = {},
    onNoteClick: (Note) -> Unit = {},
    onDeleteNote: (Note) -> Unit = {},
) {
    if (notes.isEmpty()) {
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_document),
                    contentDescription = "No notes",
                    tint = TextColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the + button to add your first note",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextColor.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            items(
                count = notes.size,
                key = { index -> notes[index].id },
            ) { index ->
                val note = notes[index]
                SwipeToDismissNote(
                    note = note,
                    onNoteClick = { onNoteClick(note) },
                    onImageClick = onImageClick,
                    onStarClick = onStarClick,
                    onDelete = { onDeleteNote(note) },
                )
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onNoteClick: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onStarClick: (Note) -> Unit = {},
    onImageLoaded: () -> Unit = {},
) {
    when (note.type) {
        NoteType.IMAGE -> {
            note.imagePath?.let { imagePath ->
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Image note",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onImageClick(imagePath) },
                    contentScale = ContentScale.Crop,
                    onSuccess = { onImageLoaded() },
                    onError = { onImageLoaded() },
                )
            }
        }
        NoteType.TEXT -> {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNoteClick() }
                        .background(Secondary)
                        .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (note.isStarred) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "Starred",
                            tint = Accent,
                            modifier =
                                Modifier
                                    .padding(bottom = 4.dp)
                                    .clickable { onStarClick(note) },
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "Not Starred",
                            tint = TextColor.copy(alpha = 0.3f),
                            modifier =
                                Modifier
                                    .padding(bottom = 4.dp)
                                    .clickable { onStarClick(note) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                MarkdownText(
                    markdown = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                    truncateOnTextOverflow = true,
                    maxLines = 3,
                    checkboxColor = TextColor.copy(alpha = 0.7f),
                    checkedCheckboxColor = Accent,
                    checkmarkColor = Background,
                )
            }
        }
    }
}

@Composable
fun SwipeToDismissNote(
    note: Note,
    onNoteClick: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onStarClick: (Note) -> Unit = {},
    onDelete: () -> Unit = {},
) {
    var isImageLoaded by remember { mutableStateOf(note.type != NoteType.IMAGE) }
    var isDismissed by remember { mutableStateOf(false) }

    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        if (isImageLoaded) {
                            isDismissed = true
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
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = !isDismissed,
        exit = fadeOut(animationSpec = tween(200)),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                color = Red,
                                shape = RoundedCornerShape(16.dp),
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
            },
            enableDismissFromEndToStart = isImageLoaded,
            enableDismissFromStartToEnd = false,
        ) {
            NoteItem(
                note = note,
                onNoteClick = onNoteClick,
                onImageClick = onImageClick,
                onStarClick = onStarClick,
                onImageLoaded = { isImageLoaded = true },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePageHeader(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Course Page",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_chevron_left),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
    )
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
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
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
fun CourseInfoSection(
    courseTitle: String,
    reminders: List<Reminder>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = courseTitle,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp, lineHeight = 20.sp),
            color = TextColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        val prioritizedReminders =
            remember(reminders) {
                mutableListOf<Reminder>().apply {
                    addAll(reminders.filter { it.status == ReminderStatus.UPCOMING })
                    addAll(reminders.filter { it.status == ReminderStatus.CAN_WAIT })
                    addAll(reminders.filter { it.status == ReminderStatus.COMPLETED })
                }
            }

        val displayedReminders = prioritizedReminders.take(3)
        val remainingCount = prioritizedReminders.size - 3

        if (displayedReminders.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) {
                items(displayedReminders.size) { index ->
                    RemindersChip(
                        text = "${displayedReminders[index].title} by ${displayedReminders[index].dueDate}",
                        reminder = displayedReminders[index],
                    )
                }

                if (remainingCount > 0) {
                    item {
                        RemindersChip(
                            text = "+$remainingCount",
                            reminder = null,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun RemindersChip(
    text: String,
    reminder: Reminder?,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Secondary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                reminder == null -> {
                    Text(
                        text = text,
                        color = TextColor,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                }
                reminder.status == ReminderStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = text,
                        color = TextColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    )
                }
                else -> {
                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (reminder.status) {
                                        ReminderStatus.UPCOMING -> Red
                                        ReminderStatus.CAN_WAIT -> Yellow
                                        else -> Secondary
                                    },
                                ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = text,
                        color = TextColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedFabGroup(
    showExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onWriteNote: () -> Unit,
    onSetReminder: () -> Unit,
    onUploadFile: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomEnd)
                .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = showExpanded,
            enter =
                fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        animationSpec = spring(dampingRatio = 0.8f),
                        initialOffsetY = { it / 3 },
                    ),
            exit =
                fadeOut(animationSpec = tween(200)) +
                    slideOutVertically(
                        animationSpec = tween(200),
                        targetOffsetY = { it / 3 },
                    ),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FabWithTooltip("Write Note", R.drawable.ic_edit_document, onWriteNote)
                FabWithTooltip("Set Reminder", R.drawable.ic_clock, onSetReminder)
                FabWithTooltip("Upload File", R.drawable.ic_upload, onUploadFile)
            }
        }

        FloatingActionButton(
            onClick = onToggleExpanded,
            containerColor = Secondary,
            contentColor = TextColor,
            elevation =
                FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    hoveredElevation = 8.dp,
                    focusedElevation = 8.dp,
                ),
            shape = RoundedCornerShape(9999.dp),
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (showExpanded) 45f else 0f,
                animationSpec = spring(dampingRatio = 0.8f),
                label = "fab_rotation",
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (showExpanded) "Close" else "Add content",
                modifier =
                    Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = rotation),
            )
        }
    }
}

@Composable
fun FabWithTooltip(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = true,
            enter =
                scaleIn(
                    animationSpec = spring(dampingRatio = 0.8f),
                    initialScale = 0.8f,
                ) + fadeIn(animationSpec = tween(200)),
            exit =
                scaleOut(
                    animationSpec = tween(150),
                    targetScale = 0.8f,
                ) + fadeOut(animationSpec = tween(150)),
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Secondary, RoundedCornerShape(8.dp))
                        .background(Background)
                        .clickable {
                            onClick()
                        }.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        AnimatedVisibility(
            visible = true,
            enter =
                scaleIn(
                    animationSpec = spring(dampingRatio = 0.8f),
                    initialScale = 0.6f,
                ) + fadeIn(animationSpec = tween(200)),
            exit =
                scaleOut(
                    animationSpec = tween(150),
                    targetScale = 0.6f,
                ) + fadeOut(animationSpec = tween(150)),
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = TextColor,
                contentColor = Secondary,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(9999.dp),
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        hoveredElevation = 8.dp,
                        focusedElevation = 8.dp,
                    ),
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                )
            }
        }
    }
}

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full screen image",
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                contentScale = ContentScale.Fit,
            )

            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
