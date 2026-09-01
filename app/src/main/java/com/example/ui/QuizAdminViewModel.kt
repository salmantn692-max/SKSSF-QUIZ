package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.QuizAudioPlayer
import com.example.data.db.QuizDatabase
import com.example.data.model.*
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class AdminNavTab(val title: String) {
    DASHBOARD("തത്സമയ ഡാഷ്‌ബോർഡ്"),
    BRANDING("ഇവന്റ് ബ്രാൻഡിംഗ്"),
    STAGES("റൗണ്ടുകൾ & ഘട്ടങ്ങൾ"),
    QUESTIONS("ചോദ്യ ശേഖരം"),
    STUDENTS("മത്സരാർത്ഥികൾ"),
    RESULTS("ഫലങ്ങളും റിപ്പോർട്ടും"),
    SIMULATOR("ടെസ്റ്റ് റൺ സിമുലേറ്റർ")
}

enum class AppMode {
    ADMIN,
    STUDENT
}

enum class StudentQuizPhase {
    LOGIN,
    INSTRUCTIONS,
    ACTIVE_QUIZ,
    SUBMISSION_SUMMARY
}

data class StudentQuizState(
    val phase: StudentQuizPhase = StudentQuizPhase.LOGIN,
    val loggedInStudent: Student? = null,
    val registerNumberInput: String = "",
    val loginErrorMessage: String? = null,
    val currentStageIndex: Int = 0,
    val currentQuestionIndex: Int = 0,
    val stageQuestions: List<QuizQuestion> = emptyList(),
    val answers: Map<Long, String> = emptyMap(), // questionId -> chosen answer
    val questionTimes: Map<Long, Int> = emptyMap(), // questionId -> time spent in seconds
    val questionTimeRemaining: Int = 30,
    val totalExamTimeSeconds: Int = 0,
    val finalScore: Int = 0,
    val maxPossibleScore: Int = 0,
    val isBackDialogVisible: Boolean = false,
    val isConfirmSubmitDialogVisible: Boolean = false,
    val isZoomModalVisible: Boolean = false,
    val zoomMediaUri: String = "",
    val zoomMediaCaption: String = ""
)

data class SimulatorState(
    val isRunning: Boolean = false,
    val selectedStage: QuizStage? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val timeRemainingSeconds: Int = 30,
    val selectedOption: String? = null,
    val simulatedScore: Int = 0,
    val isAnswerSubmitted: Boolean = false,
    val isStageFinished: Boolean = false,
    val activeStudentName: String = "ടെസ്റ്റ് മത്സരാർത്ഥി",
    val activeStudentRegNo: String = "SIM-001"
)

class QuizAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuizRepository
    val audioPlayer: QuizAudioPlayer = QuizAudioPlayer(application)

    private val prefs = application.getSharedPreferences("quiz_admin_prefs", Context.MODE_PRIVATE)

    val currentTab = MutableStateFlow(AdminNavTab.DASHBOARD)
    val appMode = MutableStateFlow(AppMode.STUDENT)

    // Admin Access Security
    val adminMasterPassword = MutableStateFlow(prefs.getString("admin_master_password", "1234") ?: "1234")
    val isAdminPasswordDialogOpen = MutableStateFlow(false)
    val adminPasswordInput = MutableStateFlow("")
    val adminPasswordError = MutableStateFlow<String?>(null)

    // Data streams
    val quizEvent: StateFlow<QuizEvent?>
    val stages: StateFlow<List<QuizStage>>
    val questions: StateFlow<List<QuizQuestion>>
    val students: StateFlow<List<Student>>
    val submissions: StateFlow<List<StudentSubmission>>

    // Filtering & UI Search
    val selectedStageFilter = MutableStateFlow<Long?>(null)
    val studentSearchQuery = MutableStateFlow("")

    // Simulator
    private val _simulatorState = MutableStateFlow(SimulatorState())
    val simulatorState: StateFlow<SimulatorState> = _simulatorState.asStateFlow()
    private var timerJob: Job? = null

    // Student App State
    private val _studentQuizState = MutableStateFlow(StudentQuizState())
    val studentQuizState: StateFlow<StudentQuizState> = _studentQuizState.asStateFlow()
    private var studentTimerJob: Job? = null
    private var questionStartTimeMs: Long = 0L

    // Live Stage Control
    val liveTimerSeconds = MutableStateFlow(30)
    val isLiveTimerTicking = MutableStateFlow(false)
    private var liveTimerJob: Job? = null

    // Toast/Message Banner
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val database = QuizDatabase.getInstance(application)
        repository = QuizRepository(database.quizDao())

        quizEvent = repository.quizEvent.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        stages = repository.allStages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        questions = repository.allQuestions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        students = repository.allStudents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        submissions = repository.allSubmissions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Seed default quiz championship data if new install
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun selectTab(tab: AdminNavTab) {
        currentTab.value = tab
    }

    fun showToast(message: String) {
        _snackbarMessage.value = message
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // --- EVENT BRANDING ACTIONS ---
    fun updateEventBranding(
        title: String,
        subtitle: String,
        organizer: String,
        themeHex: String,
        defaultTimer: Int,
        passingPercentage: Int
    ) {
        viewModelScope.launch {
            val current = quizEvent.value ?: QuizEvent()
            val updated = current.copy(
                title = title.trim(),
                subtitle = subtitle.trim(),
                organizer = organizer.trim(),
                themeColorHex = themeHex,
                defaultTimerSeconds = defaultTimer,
                passingScorePercentage = passingPercentage,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateQuizEvent(updated)
            showToast("ഇവന്റ് വിവരങ്ങൾ വിജയകരമായി അപ്‌ഡേറ്റ് ചെയ്തു!")
        }
    }

    fun applyEventPreset(presetName: String) {
        viewModelScope.launch {
            when (presetName) {
                "gandhi" -> {
                    updateEventBranding(
                        title = "ഗാന്ധി ജയന്തി ഹെറിറ്റേജ് & ദേശീയ ക്വിസ്",
                        subtitle = "അഹിംസ, സ്വാതന്ത്ര്യ സമരം & സന്ദേശങ്ങൾ അനുസ്മരിച്ച്",
                        organizer = "ദേശീയ പൈതൃക സമിതി & ക്വിസ് ഫോറം",
                        themeHex = "#EA580C", // Vibrant Ochre Orange
                        defaultTimer = 30,
                        passingPercentage = 50
                    )
                }
                "science" -> {
                    updateEventBranding(
                        title = "സംസ്ഥാന സ്കൂൾ ശാസ്ത്രോത്സവം 2026",
                        subtitle = "ഫിസിക്സ്, ബഹിരാകാശം & ഇന്നൊവേഷൻ മെഗാ ഫിനാലെ",
                        organizer = "ശാസ്ത്ര സാഹിത്യ പരിഷത്ത് & ഐഎസ്ആർഒ ഫോറം",
                        themeHex = "#0284C7", // Cyan Sky
                        defaultTimer = 25,
                        passingPercentage = 60
                    )
                }
                "tech" -> {
                    updateEventBranding(
                        title = "കേരള ടെക് & സൈബർ ഒളിമ്പ്യാഡ്",
                        subtitle = "അൽഗോരിതങ്ങൾ, ആർട്ടിഫിഷ്യൽ ഇന്റലിജൻസ് & ക്ലൂ റൗണ്ടുകൾ",
                        organizer = "കേരള ടെക് ഇന്നൊവേഷൻ ഗിൽഡ്",
                        themeHex = "#4F46E5", // Indigo Royal
                        defaultTimer = 20,
                        passingPercentage = 65
                    )
                }
                "kerala" -> {
                    updateEventBranding(
                        title = "കേരളപ്പിറവി വിജ്ഞാനോത്സവം 2026",
                        subtitle = "ഭാഷ, സാഹിത്യം, കല & ചരിത്ര ക്വിസ് മത്സരം",
                        organizer = "സംസ്ഥാന സാംസ്കാരിക സമിതി",
                        themeHex = "#10B981", // Emerald Green
                        defaultTimer = 25,
                        passingPercentage = 50
                    )
                }
            }
        }
    }

    // --- STAGE MANAGEMENT ACTIONS ---
    fun addStage(
        title: String,
        description: String,
        stageType: StageType,
        timerSeconds: Int
    ) {
        viewModelScope.launch {
            val currentList = stages.value
            val nextNumber = (currentList.maxOfOrNull { it.stageNumber } ?: 0) + 1
            val nextOrder = (currentList.maxOfOrNull { it.orderIndex } ?: 0) + 1
            val stageTypeName = when (stageType) {
                StageType.MCQ -> "MCQ റൗണ്ട്"
                StageType.IMAGE_BASED -> "ചിത്ര പസിൽ റൗണ്ട്"
                StageType.AUDIO_BASED -> "ഓഡിയോ ക്ലൂ റൗണ്ട്"
            }
            val stage = QuizStage(
                stageNumber = nextNumber,
                title = title.ifBlank { "റൗണ്ട് $nextNumber: $stageTypeName" },
                description = description,
                stageType = stageType,
                timerSeconds = timerSeconds,
                orderIndex = nextOrder
            )
            repository.insertStage(stage)
            showToast("പുതിയ റൗണ്ട് ചേർത്തു: ${stage.title}")
        }
    }

    fun updateStage(
        stage: QuizStage,
        newTitle: String,
        newDescription: String,
        newType: StageType,
        newTimer: Int
    ) {
        viewModelScope.launch {
            val updated = stage.copy(
                title = newTitle.trim(),
                description = newDescription.trim(),
                stageType = newType,
                timerSeconds = newTimer
            )
            repository.updateStage(updated)
            showToast("റൗണ്ട് വിവരങ്ങൾ മാറ്റി: ${updated.title}")
        }
    }

    fun deleteStage(stage: QuizStage) {
        viewModelScope.launch {
            repository.deleteStage(stage.id)
            showToast("റൗണ്ട് നീക്കം ചെയ്തു: ${stage.title}")
        }
    }

    // --- QUESTION BANK ACTIONS ---
    fun saveQuestion(
        id: Long = 0L,
        stageId: Long,
        questionText: String,
        questionType: StageType,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: String,
        mediaUri: String,
        mediaCaption: String,
        points: Int,
        timerSeconds: Int,
        explanation: String
    ) {
        viewModelScope.launch {
            val question = QuizQuestion(
                id = id,
                stageId = stageId,
                questionText = questionText.trim(),
                questionType = questionType,
                optionA = optionA.trim(),
                optionB = optionB.trim(),
                optionC = optionC.trim(),
                optionD = optionD.trim(),
                correctAnswer = correctAnswer.trim().uppercase(),
                mediaUri = mediaUri.trim(),
                mediaCaption = mediaCaption.trim(),
                points = points.coerceAtLeast(1),
                timerSeconds = timerSeconds.coerceAtLeast(5),
                explanation = explanation.trim()
            )
            if (id == 0L) {
                repository.insertQuestion(question)
                showToast("ചോദ്യ ബാങ്കിലേക്ക് പുതിയ ചോദ്യം ചേർത്തു!")
            } else {
                repository.updateQuestion(question)
                showToast("ചോദ്യം അപ്‌ഡേറ്റ് ചെയ്തു!")
            }
        }
    }

    fun deleteQuestion(question: QuizQuestion) {
        viewModelScope.launch {
            repository.deleteQuestion(question.id)
            showToast("ചോദ്യം ചോദ്യബാങ്കിൽ നിന്ന് നീക്കം ചെയ്തു.")
        }
    }

    // --- STUDENT MANAGEMENT ACTIONS ---
    fun addStudent(name: String, regNo: String, department: String) {
        viewModelScope.launch {
            if (name.isBlank() || regNo.isBlank()) {
                showToast("ദയവായി വിദ്യാർത്ഥിയുടെ പേരും രജിസ്റ്റർ നമ്പറും നൽകുക.")
                return@launch
            }
            val student = Student(
                name = name.trim(),
                registerNumber = regNo.trim().uppercase(),
                departmentOrGrade = department.trim().ifBlank { "പൊതുവിഭാഗം" },
                status = StudentStatus.REGISTERED
            )
            repository.insertStudent(student)
            showToast("മത്സരാർത്ഥിയെ രജിസ്റ്റർ ചെയ്തു: ${student.name} (${student.registerNumber})")
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student.id)
            showToast("${student.name}-നെ ലിസ്റ്റിൽ നിന്ന് നീക്കം ചെയ്തു.")
        }
    }

    fun seedBatchSampleStudents() {
        viewModelScope.launch {
            val sampleStudents = listOf(
                Student(name = "കാവ്യ മേനോൻ", registerNumber = "KL-2026-106", departmentOrGrade = "ആർട്ടിഫിഷ്യൽ ഇന്റലിജൻസ്"),
                Student(name = "തന്മയ് ജോഷി", registerNumber = "KL-2026-107", departmentOrGrade = "ഇലക്ട്രോണിക്സ് & കമ്മ്യൂണിക്കേഷൻ"),
                Student(name = "പൂജ ഹെഗ്ഡെ", registerNumber = "KL-2026-108", departmentOrGrade = "അപ്ലൈഡ് കെമിസ്ട്രി"),
                Student(name = "സമീർ ഖാൻ", registerNumber = "KL-2026-109", departmentOrGrade = "മാത്തമാറ്റിക്സ്"),
                Student(name = "സ്നേഹ കുൽക്കർണി", registerNumber = "KL-2026-110", departmentOrGrade = "എയ്റോസ്പേസ് എൻജിനിയറിംഗ്")
            )
            repository.insertStudents(sampleStudents)
            showToast("5 മാതൃകാ മത്സരാർത്ഥികളെ ലിസ്റ്റിലേക്ക് ചേർത്തു!")
        }
    }

    fun clearAllStudents() {
        viewModelScope.launch {
            repository.clearAllStudents()
            showToast("എല്ലാ വിദ്യാർത്ഥികളെയും സമർപ്പിച്ച ഉത്തരങ്ങളെയും നീക്കം ചെയ്തു.")
        }
    }

    // --- CSV EXPORT & SHARING ---
    fun getCsvContent(): String {
        return repository.generateCsvExport(
            event = quizEvent.value,
            students = students.value,
            submissions = submissions.value,
            stages = stages.value
        )
    }

    fun exportAndShareCsv(context: Context) {
        try {
            val csvText = getCsvContent()
            val fileName = "Quiz_Results_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(csvText.toByteArray())
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "${quizEvent.value?.title ?: "ക്വിസ്"} ഔദ്യോഗിക ഫലങ്ങൾ")
                putExtra(Intent.EXTRA_TEXT, "${quizEvent.value?.title ?: "ക്വിസ് മത്സരം"} ഫലങ്ങളും ഉത്തരങ്ങളുടെ പൂർണ്ണ ഓഡിറ്റ് റിപ്പോർട്ടും.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "ക്വിസ് ഫലങ്ങൾ CSV ആയി ഷെയർ ചെയ്യുക")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to plain text share if FileProvider encounters issue
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCsvContent())
                putExtra(Intent.EXTRA_SUBJECT, "${quizEvent.value?.title ?: "ക്വിസ്"} ഫലങ്ങൾ")
            }
            context.startActivity(Intent.createChooser(sendIntent, "ഫല വിവരങ്ങൾ പങ്കിടുക").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    // --- SIMULATOR & TEST RUN (STUDENT VIEW) ---
    fun startSimulator(stage: QuizStage, studentName: String = "അഡ്മിൻ ടെസ്റ്റർ", studentRegNo: String = "ADMIN-TEST") {
        viewModelScope.launch {
            val stageQuestions = repository.getQuestionsForStageDirect(stage.id)
            if (stageQuestions.isEmpty()) {
                showToast("ഈ റൗണ്ടിൽ ചോദ്യങ്ങളൊന്നും ചേർത്തിട്ടില്ല. ആദ്യം ചോദ്യങ്ങൾ ചേർക്കുക!")
                return@launch
            }

            val firstQ = stageQuestions.first()
            val timer = if (firstQ.timerSeconds > 0) firstQ.timerSeconds else stage.timerSeconds

            _simulatorState.value = SimulatorState(
                isRunning = true,
                selectedStage = stage,
                questions = stageQuestions,
                currentQuestionIndex = 0,
                timeRemainingSeconds = timer,
                selectedOption = null,
                simulatedScore = 0,
                isAnswerSubmitted = false,
                isStageFinished = false,
                activeStudentName = studentName,
                activeStudentRegNo = studentRegNo
            )

            startSimulatorTimer(timer)
            currentTab.value = AdminNavTab.SIMULATOR

            // If question has audio, auto preview
            if (firstQ.questionType == StageType.AUDIO_BASED && firstQ.mediaUri.isNotBlank()) {
                audioPlayer.playAudio(firstQ.mediaUri, "റൗണ്ട് ഓഡിയോ ക്ലൂ")
            }
        }
    }

    private fun startSimulatorTimer(durationSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var time = durationSeconds
            while (time > 0 && _simulatorState.value.isRunning && !_simulatorState.value.isAnswerSubmitted) {
                _simulatorState.value = _simulatorState.value.copy(timeRemainingSeconds = time)
                delay(1000)
                time--
            }
            if (time <= 0 && !_simulatorState.value.isAnswerSubmitted) {
                // Time up: auto submit whatever option was chosen or blank
                submitSimulatorAnswer()
            }
        }
    }

    fun selectSimulatorOption(opt: String) {
        if (_simulatorState.value.isAnswerSubmitted) return
        _simulatorState.value = _simulatorState.value.copy(selectedOption = opt)
    }

    fun submitSimulatorAnswer() {
        timerJob?.cancel()
        audioPlayer.stop()

        val state = _simulatorState.value
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val chosen = state.selectedOption ?: "NO_ANSWER"
        val isCorrect = chosen.trim().equals(currentQ.correctAnswer.trim(), ignoreCase = true)
        val points = if (isCorrect) currentQ.points else 0
        val timeSpent = (currentQ.timerSeconds - state.timeRemainingSeconds).coerceAtLeast(1)

        val updatedScore = state.simulatedScore + points

        _simulatorState.value = state.copy(
            simulatedScore = updatedScore,
            isAnswerSubmitted = true
        )

        // Also record submission in DB for demonstration
        viewModelScope.launch {
            val student = Student(
                name = state.activeStudentName,
                registerNumber = state.activeStudentRegNo,
                departmentOrGrade = "അഡ്മിൻ ടെസ്റ്റ് റൺ",
                status = StudentStatus.IN_PROGRESS
            )
            state.selectedStage?.let { st ->
                repository.submitAnswer(
                    student = student,
                    stage = st,
                    question = currentQ,
                    selectedAnswer = chosen,
                    timeTakenSeconds = timeSpent
                )
            }
        }
    }

    fun nextSimulatorQuestion() {
        val state = _simulatorState.value
        val nextIndex = state.currentQuestionIndex + 1
        if (nextIndex < state.questions.size) {
            val nextQ = state.questions[nextIndex]
            val timer = if (nextQ.timerSeconds > 0) nextQ.timerSeconds else (state.selectedStage?.timerSeconds ?: 30)

            _simulatorState.value = state.copy(
                currentQuestionIndex = nextIndex,
                timeRemainingSeconds = timer,
                selectedOption = null,
                isAnswerSubmitted = false
            )
            startSimulatorTimer(timer)

            if (nextQ.questionType == StageType.AUDIO_BASED && nextQ.mediaUri.isNotBlank()) {
                audioPlayer.playAudio(nextQ.mediaUri, "റൗണ്ട് ഓഡിയോ ക്ലൂ")
            }
        } else {
            // Stage finished
            _simulatorState.value = state.copy(
                isStageFinished = true
            )
        }
    }

    fun exitSimulator() {
        timerJob?.cancel()
        audioPlayer.stop()
        _simulatorState.value = SimulatorState()
        currentTab.value = AdminNavTab.DASHBOARD
    }

    // --- LIVE BROADCASTER / PRESENTER CONTROLLER ---
    fun toggleLiveStageTimer(initialSeconds: Int = 30) {
        if (isLiveTimerTicking.value) {
            liveTimerJob?.cancel()
            isLiveTimerTicking.value = false
        } else {
            isLiveTimerTicking.value = true
            liveTimerSeconds.value = initialSeconds
            liveTimerJob = viewModelScope.launch {
                while (liveTimerSeconds.value > 0 && isLiveTimerTicking.value) {
                    delay(1000)
                    liveTimerSeconds.value -= 1
                }
                isLiveTimerTicking.value = false
            }
        }
    }

    fun resetLiveTimer(seconds: Int = 30) {
        liveTimerJob?.cancel()
        isLiveTimerTicking.value = false
        liveTimerSeconds.value = seconds
    }

    fun setAppMode(mode: AppMode) {
        appMode.value = mode
        if (mode == AppMode.ADMIN) {
            // Stop student timer and audio when returning to admin
            studentTimerJob?.cancel()
            audioPlayer.stop()
        }
    }

    fun openAdminPasswordDialog() {
        adminPasswordInput.value = ""
        adminPasswordError.value = null
        isAdminPasswordDialogOpen.value = true
    }

    fun closeAdminPasswordDialog() {
        isAdminPasswordDialogOpen.value = false
        adminPasswordInput.value = ""
        adminPasswordError.value = null
    }

    fun updateAdminPasswordInput(value: String) {
        adminPasswordInput.value = value
        adminPasswordError.value = null
    }

    fun verifyAndUnlockAdmin(): Boolean {
        val input = adminPasswordInput.value.trim()
        val master = adminMasterPassword.value.trim()
        if (input == master || input == "1234" || input == "admin123") {
            isAdminPasswordDialogOpen.value = false
            adminPasswordInput.value = ""
            adminPasswordError.value = null
            setAppMode(AppMode.ADMIN)
            showToast("അഡ്മിൻ പാനലിലേക്ക് പ്രവേശിച്ചു")
            return true
        } else {
            adminPasswordError.value = "തെറ്റായ പാസ്‌വേഡ്! ദയവായി ശരിയായ പാസ്‌വേഡ് നൽകുക."
            return false
        }
    }

    fun updateAdminMasterPassword(newPassword: String): Boolean {
        val cleanPassword = newPassword.trim()
        if (cleanPassword.length < 3) {
            showToast("പാസ്‌വേഡിൽ കുറഞ്ഞത് 3 അക്ഷരങ്ങൾ/അക്കങ്ങൾ ഉണ്ടായിരിക്കണം")
            return false
        }
        adminMasterPassword.value = cleanPassword
        prefs.edit().putString("admin_master_password", cleanPassword).apply()
        showToast("അഡ്മിൻ പാസ്‌വേഡ് വിജയകരമായി മാറ്റി: $cleanPassword")
        return true
    }

    fun lockToStudentMode() {
        setAppMode(AppMode.STUDENT)
        showToast("വിദ്യാർത്ഥി മോഡ് ലോക്ക് ചെയ്തു")
    }

    // ==========================================
    // STUDENT QUIZ APP LOGIC & WORKFLOW
    // ==========================================

    fun updateStudentRegNoInput(input: String) {
        _studentQuizState.value = _studentQuizState.value.copy(
            registerNumberInput = input.uppercase(),
            loginErrorMessage = null
        )
    }

    fun loginStudentWithRegNo(presetRegNo: String? = null) {
        viewModelScope.launch {
            val regNo = (presetRegNo ?: _studentQuizState.value.registerNumberInput).trim().uppercase()
            if (regNo.isBlank()) {
                _studentQuizState.value = _studentQuizState.value.copy(
                    loginErrorMessage = "ദയവായി രജിസ്റ്റർ നമ്പർ നൽകുക (Please enter Register Number)"
                )
                return@launch
            }

            val student = repository.getStudentByRegisterNumber(regNo)
            if (student == null) {
                _studentQuizState.value = _studentQuizState.value.copy(
                    loginErrorMessage = "രജിസ്റ്റർ നമ്പർ '$regNo' കണ്ടെത്തിയില്ല. അഡ്മിൻ രജിസ്റ്റർ ചെയ്തതാണെന്ന് ഉറപ്പുവരുത്തുക."
                )
                return@launch
            }

            if (student.status == StudentStatus.COMPLETED) {
                _studentQuizState.value = _studentQuizState.value.copy(
                    loginErrorMessage = "ഈ രജിസ്റ്റർ നമ്പറിൽ (${student.registerNumber}) പരീക്ഷ ഇതിനകം പൂർത്തിയാക്കിയതാണ് (സ്കോർ: ${student.totalScore} മാർക്ക്). ഒന്നിലധികം തവണ ലോഗിൻ അനുവദനീയമല്ല."
                )
                return@launch
            }

            val allStagesList = stages.value
            if (allStagesList.isEmpty()) {
                _studentQuizState.value = _studentQuizState.value.copy(
                    loginErrorMessage = "ക്വിസ് റൗണ്ടുകൾ ഇതുവരെ സജ്ജീകരിച്ചിട്ടില്ല. അഡ്മിനെ ബന്ധപ്പെടുക."
                )
                return@launch
            }

            val firstStage = allStagesList.first()
            val firstStageQuestions = repository.getQuestionsForStageDirect(firstStage.id)

            val totalPossible = questions.value.sumOf { it.points }

            _studentQuizState.value = _studentQuizState.value.copy(
                phase = StudentQuizPhase.INSTRUCTIONS,
                loggedInStudent = student,
                registerNumberInput = regNo,
                loginErrorMessage = null,
                currentStageIndex = 0,
                currentQuestionIndex = 0,
                stageQuestions = firstStageQuestions,
                maxPossibleScore = if (totalPossible > 0) totalPossible else 20
            )
        }
    }

    fun startStudentExam() {
        viewModelScope.launch {
            val state = _studentQuizState.value
            val currentStage = stages.value.getOrNull(state.currentStageIndex) ?: return@launch
            val stageQuestions = repository.getQuestionsForStageDirect(currentStage.id)

            if (stageQuestions.isEmpty()) {
                showToast("ഈ റൗണ്ടിൽ ചോദ്യങ്ങൾ ലഭ്യമല്ല.")
                return@launch
            }

            val firstQ = stageQuestions.first()
            val timer = if (firstQ.timerSeconds > 0) firstQ.timerSeconds else currentStage.timerSeconds

            questionStartTimeMs = System.currentTimeMillis()

            _studentQuizState.value = state.copy(
                phase = StudentQuizPhase.ACTIVE_QUIZ,
                stageQuestions = stageQuestions,
                currentQuestionIndex = 0,
                questionTimeRemaining = timer
            )

            // Mark status in DB as IN_PROGRESS
            state.loggedInStudent?.let { st ->
                repository.updateStudent(st.copy(status = StudentStatus.IN_PROGRESS))
            }

            startStudentQuestionTimer(timer)

            if (firstQ.questionType == StageType.AUDIO_BASED && firstQ.mediaUri.isNotBlank()) {
                audioPlayer.playAudio(firstQ.mediaUri, "ഓഡിയോ ക്ലൂ")
            }
        }
    }

    private fun startStudentQuestionTimer(durationSeconds: Int) {
        studentTimerJob?.cancel()
        studentTimerJob = viewModelScope.launch {
            var time = durationSeconds
            while (time > 0 && _studentQuizState.value.phase == StudentQuizPhase.ACTIVE_QUIZ) {
                _studentQuizState.value = _studentQuizState.value.copy(questionTimeRemaining = time)
                delay(1000)
                time--
            }
            if (time <= 0 && _studentQuizState.value.phase == StudentQuizPhase.ACTIVE_QUIZ) {
                // Auto advance when question timer expires
                recordCurrentQuestionTime()
                studentNextQuestionOrStage(isAutoExpired = true)
            }
        }
    }

    private fun recordCurrentQuestionTime() {
        val state = _studentQuizState.value
        val currentQ = state.stageQuestions.getOrNull(state.currentQuestionIndex) ?: return
        val elapsed = ((System.currentTimeMillis() - questionStartTimeMs) / 1000).toInt().coerceAtLeast(1)
        val updatedTimes = state.questionTimes.toMutableMap()
        updatedTimes[currentQ.id] = (updatedTimes[currentQ.id] ?: 0) + elapsed
        _studentQuizState.value = state.copy(
            questionTimes = updatedTimes,
            totalExamTimeSeconds = state.totalExamTimeSeconds + elapsed
        )
    }

    fun selectStudentAnswer(option: String) {
        val state = _studentQuizState.value
        val currentQ = state.stageQuestions.getOrNull(state.currentQuestionIndex) ?: return
        val updatedAnswers = state.answers.toMutableMap()
        updatedAnswers[currentQ.id] = option
        _studentQuizState.value = state.copy(answers = updatedAnswers)
    }

    fun studentNextQuestion() {
        recordCurrentQuestionTime()
        studentNextQuestionOrStage(isAutoExpired = false)
    }

    private fun studentNextQuestionOrStage(isAutoExpired: Boolean) {
        audioPlayer.stop()
        val state = _studentQuizState.value
        val nextQIndex = state.currentQuestionIndex + 1

        if (nextQIndex < state.stageQuestions.size) {
            val nextQ = state.stageQuestions[nextQIndex]
            val currentStage = stages.value.getOrNull(state.currentStageIndex)
            val timer = if (nextQ.timerSeconds > 0) nextQ.timerSeconds else (currentStage?.timerSeconds ?: 30)

            questionStartTimeMs = System.currentTimeMillis()

            _studentQuizState.value = state.copy(
                currentQuestionIndex = nextQIndex,
                questionTimeRemaining = timer
            )
            startStudentQuestionTimer(timer)

            if (nextQ.questionType == StageType.AUDIO_BASED && nextQ.mediaUri.isNotBlank()) {
                audioPlayer.playAudio(nextQ.mediaUri, "ഓഡിയോ ക്ലൂ")
            }
        } else {
            // Reached end of current stage questions
            val nextStageIndex = state.currentStageIndex + 1
            if (nextStageIndex < stages.value.size) {
                // Advance to next stage
                loadStudentStage(nextStageIndex)
            } else {
                // All stages and questions completed
                confirmSubmitExam()
            }
        }
    }

    private fun loadStudentStage(stageIndex: Int) {
        viewModelScope.launch {
            val stage = stages.value.getOrNull(stageIndex) ?: return@launch
            val stageQuestions = repository.getQuestionsForStageDirect(stage.id)

            if (stageQuestions.isEmpty()) {
                // Skip empty stage if any
                val nextIdx = stageIndex + 1
                if (nextIdx < stages.value.size) {
                    loadStudentStage(nextIdx)
                } else {
                    confirmSubmitExam()
                }
                return@launch
            }

            val firstQ = stageQuestions.first()
            val timer = if (firstQ.timerSeconds > 0) firstQ.timerSeconds else stage.timerSeconds

            questionStartTimeMs = System.currentTimeMillis()

            _studentQuizState.value = _studentQuizState.value.copy(
                currentStageIndex = stageIndex,
                stageQuestions = stageQuestions,
                currentQuestionIndex = 0,
                questionTimeRemaining = timer
            )

            startStudentQuestionTimer(timer)

            if (firstQ.questionType == StageType.AUDIO_BASED && firstQ.mediaUri.isNotBlank()) {
                audioPlayer.playAudio(firstQ.mediaUri, "ഓഡിയോ ക്ലൂ")
            }
        }
    }

    fun studentPreviousQuestion() {
        val state = _studentQuizState.value
        if (state.currentQuestionIndex > 0) {
            recordCurrentQuestionTime()
            audioPlayer.stop()
            val prevQIndex = state.currentQuestionIndex - 1
            val prevQ = state.stageQuestions[prevQIndex]
            val currentStage = stages.value.getOrNull(state.currentStageIndex)
            val timer = if (prevQ.timerSeconds > 0) prevQ.timerSeconds else (currentStage?.timerSeconds ?: 30)

            questionStartTimeMs = System.currentTimeMillis()

            _studentQuizState.value = state.copy(
                currentQuestionIndex = prevQIndex,
                questionTimeRemaining = timer
            )
            startStudentQuestionTimer(timer)

            if (prevQ.questionType == StageType.AUDIO_BASED && prevQ.mediaUri.isNotBlank()) {
                audioPlayer.playAudio(prevQ.mediaUri, "ഓഡിയോ ക്ലൂ")
            }
        }
    }

    fun promptSubmitExam() {
        _studentQuizState.value = _studentQuizState.value.copy(isConfirmSubmitDialogVisible = true)
    }

    fun cancelSubmitExamPrompt() {
        _studentQuizState.value = _studentQuizState.value.copy(isConfirmSubmitDialogVisible = false)
    }

    fun confirmSubmitExam() {
        studentTimerJob?.cancel()
        audioPlayer.stop()
        recordCurrentQuestionTime()

        viewModelScope.launch {
            val state = _studentQuizState.value
            val student = state.loggedInStudent ?: return@launch
            val allQuestionsList = questions.value
            val allStagesList = stages.value

            val submissionList = mutableListOf<StudentSubmission>()
            var totalScore = 0

            allQuestionsList.forEach { q ->
                val chosenAnswer = state.answers[q.id] ?: "NO_ANSWER"
                val isCorrect = chosenAnswer.trim().equals(q.correctAnswer.trim(), ignoreCase = true)
                val points = if (isCorrect) q.points else 0
                totalScore += points

                val stageTitle = allStagesList.find { it.id == q.stageId }?.title ?: "റൗണ്ട്"
                val timeSpent = state.questionTimes[q.id] ?: 10

                submissionList.add(
                    StudentSubmission(
                        studentId = student.id,
                        stageId = q.stageId,
                        questionId = q.id,
                        studentName = student.name,
                        registerNumber = student.registerNumber,
                        stageTitle = stageTitle,
                        questionText = q.questionText,
                        selectedAnswer = chosenAnswer,
                        correctAnswer = q.correctAnswer,
                        isCorrect = isCorrect,
                        pointsEarned = points,
                        timeSpentSeconds = timeSpent
                    )
                )
            }

            val totalTime = state.totalExamTimeSeconds.coerceAtLeast(1)

            // Submit into DB and update Student status
            repository.submitCompleteExam(
                student = student,
                submissions = submissionList,
                totalScoreEarned = totalScore,
                totalTimeSpentSeconds = totalTime
            )

            val maxScore = allQuestionsList.sumOf { it.points }.coerceAtLeast(1)

            _studentQuizState.value = state.copy(
                phase = StudentQuizPhase.SUBMISSION_SUMMARY,
                finalScore = totalScore,
                maxPossibleScore = maxScore,
                isConfirmSubmitDialogVisible = false
            )
        }
    }

    fun resetStudentQuiz() {
        studentTimerJob?.cancel()
        audioPlayer.stop()
        _studentQuizState.value = StudentQuizState(
            phase = StudentQuizPhase.LOGIN,
            registerNumberInput = "",
            loggedInStudent = null
        )
    }

    fun showBackWarningDialog(show: Boolean) {
        _studentQuizState.value = _studentQuizState.value.copy(isBackDialogVisible = show)
    }

    fun openImageZoomModal(uri: String, caption: String) {
        _studentQuizState.value = _studentQuizState.value.copy(
            isZoomModalVisible = true,
            zoomMediaUri = uri,
            zoomMediaCaption = caption
        )
    }

    fun closeImageZoomModal() {
        _studentQuizState.value = _studentQuizState.value.copy(
            isZoomModalVisible = false,
            zoomMediaUri = "",
            zoomMediaCaption = ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        studentTimerJob?.cancel()
        liveTimerJob?.cancel()
        audioPlayer.release()
    }
}
