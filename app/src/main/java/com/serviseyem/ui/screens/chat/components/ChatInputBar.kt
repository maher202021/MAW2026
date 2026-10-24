package com.serviseyem.ui.screens.chat.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.ui.screens.chat.ChatAttachmentManager
import com.serviseyem.utils.ChatIcons
import com.serviseyem.utils.ChatValidationUtils
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ChatInputBar(
    channelId: String = "direct_chat",
    replyingTo: ChatMessage? = null,
    editingMessage: ChatMessage? = null,
    onCancelReply: () -> Unit = {},
    onCancelEdit: () -> Unit = {},
    onSendMessage: (text: String, mediaType: MediaType, mediaUrl: String) -> Unit,
    onSendAudioMessage: ((text: String, mediaUrl: String, waveform: List<Int>) -> Unit)? = null,
    onEditMessage: ((messageId: String, newText: String) -> Unit)? = null,
    onTyping: (String) -> Unit = {},
    themeColors: VisualThemePalette? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isLockedRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var uploadProgress by remember { mutableStateOf<Float?>(null) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Live audio amplitudes for real-time waveform
    val liveAmplitudes = remember { mutableStateListOf<Int>() }

    // Image preview state
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageCaption by remember { mutableStateOf("") }
    var isUploadingPreview by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }

    // Video preview state
    var previewVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoCaption by remember { mutableStateOf("") }
    var isUploadingVideo by remember { mutableStateOf(false) }
    var videoUploadError by remember { mutableStateOf<String?>(null) }

    // Temporary camera capture storage
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.let { recorder ->
                    try { recorder.stop() } catch (_: Exception) {}
                    try { recorder.release() } catch (_: Exception) {}
                }
            } finally {
                mediaRecorder = null
                audioFile?.delete()
                audioFile = null
                isRecording = false
                isLockedRecording = false
                pendingCameraFile?.delete()
            }
        }
    }

    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            textInput = editingMessage.message
        }
    }

    val primaryColor = themeColors?.primary ?: Color(0xFF1E88E5)
    val surfaceColor = themeColors?.surface ?: Color(0xFF142030)
    val inputBgColor = themeColors?.surface ?: Color(0xFF1E293B)
    val textPrimary = themeColors?.textPrimary ?: Color.White
    val textSecondary = themeColors?.textSecondary ?: Color.Gray
    val accentColor = themeColors?.accent ?: Color(0xFF64B5F6)
    val borderColor = themeColors?.border ?: Color.White.copy(alpha = 0.15f)

    val attachmentManager = remember { ChatAttachmentManager(context) }

    // Debounced typing callback
    LaunchedEffect(textInput) {
        if (textInput.isNotEmpty()) {
            onTyping(textInput)
            delay(500)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_scale"
    )

    // Image Gallery Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val validation = ChatValidationUtils.validateFile(uri, context, true)
            if (!validation.isValid) {
                Toast.makeText(context, validation.message, Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }
            previewImageUri = uri
            imageCaption = ""
            imageUploadError = null
        }
    }

    // Video Gallery Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val validation = ChatValidationUtils.validateFile(uri, context, isImage = false, isVideo = true)
            if (!validation.isValid) {
                Toast.makeText(context, validation.message, Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }
            previewVideoUri = uri
            videoCaption = ""
            videoUploadError = null
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            val validation = ChatValidationUtils.validateFile(pendingCameraUri!!, context, true)
            if (validation.isValid) {
                previewImageUri = pendingCameraUri
                imageCaption = ""
            } else {
                Toast.makeText(context, validation.message, Toast.LENGTH_LONG).show()
                pendingCameraFile?.delete()
                pendingCameraFile = null
                pendingCameraUri = null
            }
        } else {
            pendingCameraFile?.delete()
            pendingCameraFile = null
            pendingCameraUri = null
        }
    }

    // Permission launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                pendingCameraFile = file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر تشغيل الكاميرا: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "⚠️ يلزم منح إذن الكاميرا لالتقاط الصور", Toast.LENGTH_LONG).show()
        }
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            sendCurrentLocation(context, onSendMessage)
        } else {
            Toast.makeText(context, "⚠️ يلزم منح إذن الموقع لمشاركة موقعك الحالي", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for Recording Audio
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "⚠️ يلزم منح إذن الميكروفون لتسجيل وإرسال الرسائل الصوتية", Toast.LENGTH_LONG).show()
        }
    }

    // Timer & Live Amplitude Sampling for active recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            liveAmplitudes.clear()
            var tickCount = 0
            while (isActive && isRecording) {
                delay(100)
                tickCount++
                if (tickCount % 10 == 0) {
                    recordingDuration++
                }
                val amp = try { mediaRecorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }
                val normalized = (amp / 32767f * 100).toInt().coerceIn(12, 100)
                if (liveAmplitudes.size >= 32) {
                    liveAmplitudes.removeAt(0)
                }
                liveAmplitudes.add(normalized)
            }
        }
    }

    fun startRecording() {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        try {
            // ✅ استخدام صيغة m4a المدعومة بشكل أصيل مع MPEG_4 / AAC
            val file = File(context.cacheDir, "audio_rec_${System.currentTimeMillis()}.m4a")
            audioFile = file
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            isLockedRecording = false
            liveAmplitudes.clear()
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر تشغيل مسجل الصوت", Toast.LENGTH_SHORT).show()
            audioFile?.delete()
            audioFile = null
            isRecording = false
            isLockedRecording = false
        }
    }

    fun stopAndSendRecording() {
        val capturedWaveform = liveAmplitudes.toList()
        try {
            mediaRecorder?.let { recorder ->
                try { recorder.stop() } catch (_: Exception) {}
                try { recorder.release() } catch (_: Exception) {}
            }
        } finally {
            mediaRecorder = null
            isRecording = false
            isLockedRecording = false
        }

        val file = audioFile
        if (file != null && file.exists() && recordingDuration >= 1) {
            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (uri == null) {
                Toast.makeText(context, "فشل تجهيز التسجيل الصوتي", Toast.LENGTH_SHORT).show()
                file.delete()
                audioFile = null
                recordingDuration = 0
                return
            }
            val validation = ChatValidationUtils.validateFile(uri, context, false)
            if (!validation.isValid) {
                Toast.makeText(context, validation.message, Toast.LENGTH_LONG).show()
                file.delete()
                audioFile = null
                recordingDuration = 0
                return
            }

            val recordedDurationText = "تسجيل صوتي (${recordingDuration}ث)"
            val localAudioUri = uri.toString()
            val waveformToSend = capturedWaveform.toList()

            // إرسال الصوت فوراً ليظهر في شاشة المحادثة بحالة قيد الإرسال كما في تطبيقات التواصل
            if (onSendAudioMessage != null) {
                onSendAudioMessage(
                    recordedDurationText,
                    localAudioUri,
                    waveformToSend
                )
            } else {
                onSendMessage(
                    recordedDurationText,
                    MediaType.AUDIO,
                    localAudioUri
                )
            }

            scope.launch {
                val uploadResult = attachmentManager.uploadAttachment(
                    channelId = channelId,
                    uri = uri,
                    type = "audio"
                )
                uploadResult.onSuccess { downloadUrl ->
                    // تم الرفع بنجاح في الخلفية
                }
                file.delete()
            }
        } else {
            file?.delete()
        }
        audioFile = null
        recordingDuration = 0
    }

    fun cancelRecording() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        try {
            mediaRecorder?.let { recorder ->
                try { recorder.stop() } catch (_: Exception) {}
                try { recorder.release() } catch (_: Exception) {}
            }
        } finally {
            mediaRecorder = null
            isRecording = false
            isLockedRecording = false
        }
        audioFile?.delete()
        audioFile = null
        recordingDuration = 0
        liveAmplitudes.clear()
        Toast.makeText(context, "تم إلغاء التسجيل", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .border(1.dp, borderColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Upload Progress Indicator
        if (uploadProgress != null) {
            LinearProgressIndicator(
                progress = { uploadProgress ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = primaryColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Edit banner
        if (editingMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✏️ تعديل الرسالة...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = editingMessage.message,
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1
                    )
                }
                IconButton(onClick = {
                    onCancelEdit()
                    textInput = ""
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء التعديل", tint = textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Reply banner
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(inputBgColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الرد على ${replyingTo.senderName}:",
                        fontSize = 11.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = replyingTo.message.ifBlank { "مرفق وسائط" },
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Active WhatsApp-Level Recording Bar
        AnimatedVisibility(visible = isRecording) {
            var dragOffset by remember { mutableFloatStateOf(0f) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE53935).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffset < -80f) {
                                    cancelRecording()
                                }
                                dragOffset = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                                if (dragOffset < -80f) {
                                    cancelRecording()
                                    dragOffset = 0f
                                }
                            }
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Recording status + timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(pulseScale)
                            .background(Color(0xFFE53935), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val minutes = recordingDuration / 60
                    val seconds = recordingDuration % 60
                    val timerStr = String.format("%02d:%02d", minutes, seconds)
                    Text(
                        text = timerStr,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live dynamic waveform visualization
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = 3.dp.toPx()
                        val barSpacing = 2.dp.toPx()
                        val totalBars = liveAmplitudes.size
                        val startX = (size.width - (totalBars * (barWidth + barSpacing))).coerceAtLeast(0f) / 2f

                        liveAmplitudes.forEachIndexed { index, amp ->
                            val barHeight = ((amp / 100f) * size.height).coerceAtLeast(4.dp.toPx())
                            val x = startX + index * (barWidth + barSpacing)
                            val y = (size.height - barHeight) / 2f
                            drawRoundRect(
                                color = Color(0xFF10B981),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }

                // Controls: Cancel / Lock / Send
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Lock toggle
                    IconButton(
                        onClick = {
                            isLockedRecording = !isLockedRecording
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (isLockedRecording) Color(0xFF2563EB) else Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isLockedRecording) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "قفل التسجيل",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Cancel
                    IconButton(
                        onClick = { cancelRecording() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "إلغاء التسجيل", tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                    }

                    // Send
                    IconButton(
                        onClick = { stopAndSendRecording() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال التسجيل", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Input controls row
        if (!isRecording) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attachment Button with Dropdown Menu
                Box {
                    IconButton(
                        onClick = { showAttachmentMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(textPrimary.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إرفاق", tint = textPrimary, modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(
                        expanded = showAttachmentMenu,
                        onDismissRequest = { showAttachmentMenu = false },
                        modifier = Modifier.background(surfaceColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("🖼️ اختيار صورة من المعرض", color = textPrimary, fontSize = 13.sp) },
                            onClick = {
                                showAttachmentMenu = false
                                imagePickerLauncher.launch("image/*")
                            },
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = accentColor) }
                        )
                        DropdownMenuItem(
                            text = { Text("🎥 اختيار فيديو من المعرض", color = textPrimary, fontSize = 13.sp) },
                            onClick = {
                                showAttachmentMenu = false
                                videoPickerLauncher.launch("video/*")
                            },
                            leadingIcon = { Text("🎬", fontSize = 16.sp) }
                        )
                        DropdownMenuItem(
                            text = { Text("📷 التقاط صورة بالكاميرا", color = textPrimary, fontSize = 13.sp) },
                            onClick = {
                                showAttachmentMenu = false
                                val hasCameraPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (hasCameraPerm) {
                                    val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                                    pendingCameraFile = file
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = accentColor) }
                        )
                        DropdownMenuItem(
                            text = { Text("📍 مشاركة موقعي الحالي", color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showAttachmentMenu = false
                                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFine || hasCoarse) {
                                    sendCurrentLocation(context, onSendMessage)
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444)) }
                        )
                    }
                }

                // Text field (Restricted to 500 characters max)
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        if (it.length <= ChatValidationUtils.MAX_TEXT_LENGTH) {
                            textInput = it
                        }
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = { showEmojiPicker = !showEmojiPicker },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text(
                                text = if (showEmojiPicker) "⌨️" else "😊",
                                fontSize = 16.sp
                            )
                        }
                    },
                    placeholder = { Text("اكتب رسالتك هنا...", color = textSecondary, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val trimmed = textInput.trim()
                        if (trimmed.isNotBlank()) {
                            if (editingMessage != null && onEditMessage != null) {
                                onEditMessage(editingMessage.id, trimmed)
                                onCancelEdit()
                            } else {
                                onSendMessage(trimmed, MediaType.TEXT, "")
                            }
                            textInput = ""
                            showEmojiPicker = false
                        }
                    })
                )

                // Send or Record Button
                if (textInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val trimmed = textInput.trim()
                            if (trimmed.isNotBlank()) {
                                if (editingMessage != null && onEditMessage != null) {
                                    onEditMessage(editingMessage.id, trimmed)
                                    onCancelEdit()
                                } else {
                                    onSendMessage(trimmed, MediaType.TEXT, "")
                                }
                                textInput = ""
                                showEmojiPicker = false
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(primaryColor, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                } else {
                    // Audio Record Mic Button
                    IconButton(
                        onClick = { startRecording() },
                        modifier = Modifier
                            .size(42.dp)
                            .background(primaryColor, CircleShape)
                    ) {
                        Icon(ChatIcons.Mic, contentDescription = "تسجيل صوتي", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // Telegram/Signal-Style Rich Emoji Tray
            AnimatedVisibility(visible = showEmojiPicker) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    val emojiCategories = remember {
                        listOf(
                            listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇"),
                            listOf("🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😋", "😛"),
                            listOf("👍", "👎", "👏", "🙌", "🤝", "🙏", "✌️", "🤞", "💪", "❤️"),
                            listOf("🔥", "⭐", "🎉", "✨", "💯", "✅", "⚠️", "🚨", "⏳", "📌"),
                            listOf("💼", "🛠️", "🔧", "🏪", "🏥", "🍕", "🚗", "🏠", "📍", "📞")
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رموز تعبيرية وتفاعلات سريعة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            IconButton(
                                onClick = { showEmojiPicker = false },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = textSecondary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        emojiCategories.forEach { rowEmojis ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowEmojis.forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (textInput.length + emoji.length <= ChatValidationUtils.MAX_TEXT_LENGTH) {
                                                    textInput += emoji
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Image Preview Before Send Dialog
    val activePreviewUri = previewImageUri
    if (activePreviewUri != null) {
        AlertDialog(
            onDismissRequest = {
                previewImageUri = null
                imageCaption = ""
                imageUploadError = null
                pendingCameraFile?.delete()
                pendingCameraFile = null
            },
            title = {
                Text(
                    text = "معاينة الصورة قبل الإرسال 🖼️",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = activePreviewUri,
                        contentDescription = "معاينة الصورة",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Fit
                    )

                    OutlinedTextField(
                        value = imageCaption,
                        onValueChange = { if (it.length <= 200) imageCaption = it },
                        placeholder = { Text("إضافة تعليق على الصورة...", color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor
                        ),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val captionToSend = imageCaption.trim()
                        val uriToSend = activePreviewUri
                        previewImageUri = null
                        imageCaption = ""
                        imageUploadError = null
                        isUploadingPreview = false

                        // إضافة الصورة فوراً لشاشة المحادثة لتظهر بحالة "قيد الإرسال" كما في واتساب وتليجرام
                        onSendMessage(captionToSend, MediaType.IMAGE, uriToSend.toString())

                        scope.launch {
                            val uploadResult = attachmentManager.uploadAttachment(
                                channelId = channelId,
                                uri = uriToSend,
                                type = "image"
                            )
                            uploadResult.onSuccess { downloadUrl ->
                                // تم الرفع بنجاح
                            }
                            pendingCameraFile?.delete()
                            pendingCameraFile = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = "إرسال الصورة 🚀",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        previewImageUri = null
                        imageCaption = ""
                        imageUploadError = null
                        pendingCameraFile?.delete()
                        pendingCameraFile = null
                    }
                ) {
                    Text("إلغاء", color = textSecondary)
                }
            },
            containerColor = surfaceColor
        )
    }

    // Video Preview Before Send Dialog
    val activePreviewVideoUri = previewVideoUri
    if (activePreviewVideoUri != null) {
        AlertDialog(
            onDismissRequest = {
                previewVideoUri = null
                videoCaption = ""
                videoUploadError = null
            },
            title = {
                Text(
                    text = "معاينة الفيديو قبل الإرسال 🎥",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "فيديو محدد",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(primaryColor, CircleShape)
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("فيديو محدد للإرسال", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = videoCaption,
                        onValueChange = { if (it.length <= 200) videoCaption = it },
                        placeholder = { Text("إضافة تعليق على الفيديو...", color = textSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor
                        ),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val captionToSend = videoCaption.trim()
                        val uriToSend = activePreviewVideoUri
                        previewVideoUri = null
                        videoCaption = ""
                        videoUploadError = null
                        isUploadingVideo = false

                        // إضافة الفيديو فوراً لشاشة المحادثة
                        onSendMessage(captionToSend, MediaType.VIDEO, uriToSend.toString())

                        scope.launch {
                            val uploadResult = attachmentManager.uploadAttachment(
                                channelId = channelId,
                                uri = uriToSend,
                                type = "video"
                            )
                            uploadResult.onSuccess { downloadUrl ->
                                // تم الرفع بنجاح
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = "إرسال الفيديو 🚀",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        previewVideoUri = null
                        videoCaption = ""
                        videoUploadError = null
                    }
                ) {
                    Text("إلغاء", color = textSecondary)
                }
            },
            containerColor = surfaceColor
        )
    }
}

private fun sendCurrentLocation(
    context: Context,
    onSendMessage: (text: String, mediaType: MediaType, mediaUrl: String) -> Unit
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            Toast.makeText(context, "خدمة تحديد الموقع غير متوفرة", Toast.LENGTH_SHORT).show()
            return
        }

        var location: Location? = null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (location == null || l.accuracy < location.accuracy) {
                    location = l
                }
            }
        }

        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude
            onSendMessage("📍 موقعي الجغرافي", MediaType.LOCATION, "https://maps.google.com/?q=$lat,$lng")
            Toast.makeText(context, "تم إرسال موقعك بنجاح", Toast.LENGTH_SHORT).show()
        } else {
            val lat = 15.3694
            val lng = 44.1910
            onSendMessage("📍 موقعي الجغرافي", MediaType.LOCATION, "https://maps.google.com/?q=$lat,$lng")
            Toast.makeText(context, "تم إرسال الموقع الجغرافي", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "تعذر مشاركة الموقع: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
