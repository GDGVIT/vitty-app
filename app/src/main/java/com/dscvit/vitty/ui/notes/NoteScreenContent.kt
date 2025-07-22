package com.dscvit.vitty.ui.notes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.coursepage.models.Note
import com.dscvit.vitty.ui.notes.components.NoteHeader
import com.dscvit.vitty.ui.notes.components.NoteToolbar
import com.dscvit.vitty.ui.notes.components.applyFormatting
import com.dscvit.vitty.ui.notes.components.applyListFormatting
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay

@Composable
fun NoteScreenContent(
    onBackClick: () -> Unit,
    noteToEdit: Note? = null,
    onSaveNote: (title: String, content: String) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    var noteTitle by remember { mutableStateOf(noteToEdit?.title ?: "") }
    var noteText by remember { mutableStateOf(TextFieldValue(noteToEdit?.content ?: "")) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var undoStack by remember { mutableStateOf(listOf<TextFieldValue>()) }
    var redoStack by remember { mutableStateOf(listOf<TextFieldValue>()) }
    var lastSavedText by remember { mutableStateOf(TextFieldValue(noteToEdit?.content ?: "")) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges =
        remember(noteTitle, noteText, noteToEdit) {
            val originalTitle = noteToEdit?.title ?: ""
            val originalContent = noteToEdit?.content ?: ""
            noteTitle != originalTitle || noteText.text != originalContent
        }

    fun validateAndSaveNote(): Boolean =
        when {
            noteTitle.isBlank() && noteText.text.isBlank() -> {
                Toast.makeText(context, "Please add a title and content to save the note", Toast.LENGTH_SHORT).show()
                false
            }
            noteTitle.isBlank() -> {
                Toast.makeText(context, "Please add a title to save the note", Toast.LENGTH_SHORT).show()
                false
            }
            noteText.text.isBlank() -> {
                Toast.makeText(context, "Please add some content to save the note", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                onSaveNote(noteTitle, noteText.text)
                Toast.makeText(context, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                true
            }
        }

    LaunchedEffect(noteToEdit) {
        noteToEdit?.let { note ->
            noteTitle = note.title
            noteText = TextFieldValue(note.content)
            lastSavedText = TextFieldValue(note.content)
        }
    }

    LaunchedEffect(noteText) {
        delay(1000)
        if (noteText.text != lastSavedText.text && noteText.text.isNotEmpty()) {
            undoStack = (undoStack + lastSavedText).takeLast(50)
            redoStack = emptyList()
            lastSavedText = noteText
        }
    }

    fun saveToUndoStack() {
        if (noteText != lastSavedText) {
            undoStack = (undoStack + noteText).takeLast(50)
            redoStack = emptyList()
            lastSavedText = noteText
        }
    }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
        containerColor = Background,
        topBar = {
            NoteHeader(
                onBackClick = {
                    if (hasUnsavedChanges) {
                        showUnsavedChangesDialog = true
                    } else {
                        onBackClick()
                    }
                },
                isPreviewMode = isPreviewMode,
                onTogglePreview = { isPreviewMode = !isPreviewMode },
                onSaveNote = {
                    validateAndSaveNote()
                },
                canSave = noteTitle.isNotBlank() || noteText.text.isNotBlank(),
            )
        },
        bottomBar = {
            NoteToolbar(
                onBoldClick = {
                    saveToUndoStack()
                    noteText = applyFormatting(noteText, "**", "**")
                },
                onItalicClick = {
                    saveToUndoStack()
                    noteText = applyFormatting(noteText, "*", "*")
                },
                onUnderlineClick = {
                    saveToUndoStack()
                    noteText = applyFormatting(noteText, "<u>", "</u>")
                },
                onBulletClick = {
                    saveToUndoStack()
                    noteText = applyListFormatting(noteText, "- ")
                },
                onChecklistClick = {
                    saveToUndoStack()
                    noteText = applyListFormatting(noteText, "- [ ] ")
                },
                onUndoClick = {
                    if (undoStack.isNotEmpty()) {
                        redoStack = (redoStack + noteText).takeLast(50)
                        noteText = undoStack.last()
                        undoStack = undoStack.dropLast(1)
                        lastSavedText = noteText
                    }
                },
                onRedoClick = {
                    if (redoStack.isNotEmpty()) {
                        undoStack = (undoStack + noteText).takeLast(50)
                        noteText = redoStack.last()
                        redoStack = redoStack.dropLast(1)
                        lastSavedText = noteText
                    }
                },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            BasicTextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                textStyle =
                    MaterialTheme.typography.headlineSmall.copy(
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                    ),
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (noteTitle.isEmpty()) {
                            Text(
                                text = "Note title...",
                                style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                        color = TextColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Background)
                        .padding(16.dp),
            ) {
                if (isPreviewMode) {
                    MarkdownText(
                        markdown = noteText.text,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                        checkboxColor = TextColor.copy(alpha = 0.7f), // Unchecked checkbox border
                        checkedCheckboxColor = Accent, // Checked checkbox fill color
                        checkmarkColor = Background, // Checkmark color (your dark background)
                    )
                } else {
                    BasicTextField(
                        value = noteText,
                        onValueChange = { newValue ->
                            noteText = newValue
                        },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                        cursorBrush = SolidColor(Accent),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if (noteText.text.isEmpty()) {
                                    Text(
                                        text = "Start writing your note...\n\nMarkdown supported:\n**bold** *italic* <u>underline</u>\n- bullet points\n- [ ] checkboxes",
                                        color = TextColor.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }
        }
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = {
                Text(
                    text = "Unsaved Changes",
                    color = TextColor,
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Do you want to save them before leaving?",
                    color = TextColor,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (validateAndSaveNote()) {
                            showUnsavedChangesDialog = false
                        }
                    },
                ) {
                    Text("Save", color = Accent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onBackClick()
                    },
                ) {
                    Text("Discard", color = TextColor)
                }
            },
            containerColor = Background,
        )
    }
}
