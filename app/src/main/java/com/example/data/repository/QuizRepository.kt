package com.example.data.repository

import com.example.data.db.QuizDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class QuizRepository(private val quizDao: QuizDao) {

    val quizEvent: Flow<QuizEvent?> = quizDao.getQuizEvent()
    val allStages: Flow<List<QuizStage>> = quizDao.getAllStages()
    val allQuestions: Flow<List<QuizQuestion>> = quizDao.getAllQuestions()
    val allStudents: Flow<List<Student>> = quizDao.getAllStudents()
    val allSubmissions: Flow<List<StudentSubmission>> = quizDao.getAllSubmissions()

    fun getQuestionsForStage(stageId: Long): Flow<List<QuizQuestion>> =
        quizDao.getQuestionsByStage(stageId)

    suspend fun getQuestionsForStageDirect(stageId: Long): List<QuizQuestion> =
        quizDao.getQuestionsByStageDirect(stageId)

    suspend fun getQuizEventDirect(): QuizEvent? =
        quizDao.getQuizEventDirect()

    suspend fun updateQuizEvent(event: QuizEvent) =
        quizDao.insertOrUpdateQuizEvent(event)

    suspend fun insertStage(stage: QuizStage): Long =
        quizDao.insertStage(stage)

    suspend fun updateStage(stage: QuizStage) =
        quizDao.updateStage(stage)

    suspend fun deleteStage(stageId: Long) {
        quizDao.deleteStageById(stageId)
        quizDao.deleteQuestionsByStageId(stageId)
    }

    suspend fun insertQuestion(question: QuizQuestion): Long =
        quizDao.insertQuestion(question)

    suspend fun updateQuestion(question: QuizQuestion) =
        quizDao.updateQuestion(question)

    suspend fun deleteQuestion(questionId: Long) =
        quizDao.deleteQuestionById(questionId)

    suspend fun insertStudent(student: Student): Long =
        quizDao.insertStudent(student)

    suspend fun insertStudents(students: List<Student>) =
        quizDao.insertStudents(students)

    suspend fun updateStudent(student: Student) =
        quizDao.updateStudent(student)

    suspend fun deleteStudent(studentId: Long) {
        quizDao.deleteStudentById(studentId)
        quizDao.deleteSubmissionsForStudent(studentId)
    }

    suspend fun clearAllStudents() {
        quizDao.clearAllStudents()
        quizDao.clearAllSubmissions()
    }

    suspend fun submitAnswer(
        student: Student,
        stage: QuizStage,
        question: QuizQuestion,
        selectedAnswer: String,
        timeTakenSeconds: Int
    ) {
        val isCorrect = selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
        val pointsEarned = if (isCorrect) question.points else 0

        val submission = StudentSubmission(
            studentId = student.id,
            stageId = stage.id,
            questionId = question.id,
            studentName = student.name,
            registerNumber = student.registerNumber,
            stageTitle = stage.title,
            questionText = question.questionText,
            selectedAnswer = selectedAnswer,
            correctAnswer = question.correctAnswer,
            isCorrect = isCorrect,
            pointsEarned = pointsEarned,
            timeSpentSeconds = timeTakenSeconds
        )

        quizDao.insertSubmission(submission)

        // Update student aggregated scores
        val updatedScore = student.totalScore + pointsEarned
        val updatedTime = student.timeSpentSeconds + timeTakenSeconds
        val updatedStudent = student.copy(
            totalScore = updatedScore,
            timeSpentSeconds = updatedTime,
            status = StudentStatus.COMPLETED
        )
        quizDao.updateStudent(updatedStudent)
    }

    suspend fun getStudentByRegisterNumber(regNo: String): Student? {
        return quizDao.getStudentByRegNo(regNo.trim().uppercase())
    }

    suspend fun submitCompleteExam(
        student: Student,
        submissions: List<StudentSubmission>,
        totalScoreEarned: Int,
        totalTimeSpentSeconds: Int
    ) {
        if (submissions.isNotEmpty()) {
            quizDao.insertSubmissions(submissions)
        }
        val updatedStudent = student.copy(
            totalScore = totalScoreEarned,
            timeSpentSeconds = totalTimeSpentSeconds,
            status = StudentStatus.COMPLETED
        )
        quizDao.updateStudent(updatedStudent)
    }

    fun generateCsvExport(
        event: QuizEvent?,
        students: List<Student>,
        submissions: List<StudentSubmission>,
        stages: List<QuizStage>
    ): String {
        val sb = StringBuilder()
        val title = event?.title ?: "ക്വിസ് മത്സര ഫലങ്ങൾ"
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        sb.appendLine("sep=,")
        sb.appendLine("\"=== $title - ഔദ്യോഗിക ഫല റിപ്പോർട്ട് ===\"")
        sb.appendLine("\"തയ്യാറാക്കിയ സമയം\",\"$dateStr\"")
        sb.appendLine("\"സംഘാടകർ\",\"${event?.organizer ?: "ക്വിസ് സമിതി"}\"")
        sb.appendLine("\"രജിസ്റ്റർ ചെയ്ത മത്സരാർത്ഥികൾ\",${students.size}")
        sb.appendLine("\"ആകെ റൗണ്ടുകൾ\",${stages.size}")
        sb.appendLine()

        // Section 1: Student Leaderboard Summary
        sb.appendLine("\"--- വിദ്യാർത്ഥികളുടെ ലീഡർബോർഡ് റാങ്ക് ലിസ്റ്റ് ---\"")
        sb.appendLine("\"റാങ്ക്\",\"രജിസ്റ്റർ നമ്പർ\",\"മത്സരാർത്ഥിയുടെ പേര്\",\"വിഭാഗം/ക്ലാസ്\",\"ആകെ സ്കോർ\",\"എടുത്ത സമയം (സെക്കൻഡ്)\",\"നിലവിലെ അവസ്ഥ\"")
        students.sortedByDescending { it.totalScore }.forEachIndexed { index, st ->
            val statusText = when (st.status) {
                StudentStatus.COMPLETED -> "പൂർത്തിയായി"
                StudentStatus.IN_PROGRESS -> "തുടരുന്നു"
                StudentStatus.REGISTERED -> "രജിസ്റ്റർ ചെയ്തു"
            }
            sb.appendLine("${index + 1},\"${st.registerNumber}\",\"${st.name}\",\"${st.departmentOrGrade}\",${st.totalScore},${st.timeSpentSeconds},\"$statusText\"")
        }
        sb.appendLine()

        // Section 2: Detailed Stage-wise Submissions
        sb.appendLine("\"--- സമർപ്പിച്ച ഉത്തരങ്ങളുടെ പൂർണ്ണ വിവരങ്ങൾ (Audit Trail) ---\"")
        sb.appendLine("\"ഐഡി\",\"മത്സരാർത്ഥി\",\"രജിസ്റ്റർ നമ്പർ\",\"റൗണ്ട്\",\"ചോദ്യം\",\"നൽകിയ ഉത്തരം\",\"ശരിയുത്തരം\",\"ഫലം\",\"ലഭിച്ച മാർക്ക്\",\"എടുത്ത സമയം\"")
        submissions.forEach { sub ->
            val resultStr = if (sub.isCorrect) "ശരി (CORRECT)" else "തെറ്റ് (INCORRECT)"
            val cleanQuestion = sub.questionText.replace("\"", "\"\"")
            val cleanStage = sub.stageTitle.replace("\"", "\"\"")
            sb.appendLine("${sub.id},\"${sub.studentName}\",\"${sub.registerNumber}\",\"$cleanStage\",\"$cleanQuestion\",\"${sub.selectedAnswer}\",\"${sub.correctAnswer}\",\"$resultStr\",${sub.pointsEarned},${sub.timeSpentSeconds}")
        }

        return sb.toString()
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingEvent = quizDao.getQuizEventDirect()
        if (existingEvent != null) return

        // 1. Initial Event
        val defaultEvent = QuizEvent(
            id = 1L,
            title = "സംസ്ഥാന തല ക്വിസ് ചാമ്പ്യൻഷിപ്പ് 2026",
            subtitle = "ഗാന്ധി സ്മൃതി & ശാസ്ത്ര സാങ്കേതിക മെഗാ ഫിനാലെ",
            organizer = "സംസ്ഥാന ക്വിസ് കൗൺസിൽ & ശാസ്ത്ര സാഹിത്യ സമിതി",
            themeColorHex = "#6750A4",
            defaultTimerSeconds = 30,
            passingScorePercentage = 50,
            liveActiveStageId = 1L
        )
        quizDao.insertOrUpdateQuizEvent(defaultEvent)

        // 2. Stages
        val stage1Id = quizDao.insertStage(
            QuizStage(
                stageNumber = 1,
                title = "റൗണ്ട് 1: പൊതുവിജ്ഞാനം & ചരിത്ര സ്മൃതി (MCQ)",
                description = "സ്വാതന്ത്ര്യ സമരം, കേരള ചരിത്രം, ഗാന്ധിജി, ശാസ്ത്രം എന്നിവയെക്കുറിച്ചുള്ള നാല് ഓപ്ഷൻ ചോദ്യങ്ങൾ.",
                stageType = StageType.MCQ,
                timerSeconds = 30,
                totalPointsWeight = 50,
                orderIndex = 1
            )
        )

        val stage2Id = quizDao.insertStage(
            QuizStage(
                stageNumber = 2,
                title = "റൗണ്ട് 2: ദൃശ്യ വിസ്മയം & ചിത്ര പസിലുകൾ (Image)",
                description = "ചരിത്ര സ്മാരകങ്ങൾ, ശാസ്ത്ര ഉപകരണങ്ങൾ, ബഹിരാകാശ ദൗത്യങ്ങൾ എന്നിവ ചിത്രങ്ങൾ കണ്ട് തിരിച്ചറിയുക.",
                stageType = StageType.IMAGE_BASED,
                timerSeconds = 25,
                totalPointsWeight = 50,
                orderIndex = 2
            )
        )

        val stage3Id = quizDao.insertStage(
            QuizStage(
                stageNumber = 3,
                title = "റൗണ്ട് 3: ശബ്ദരേഖ & ഓഡിയോ മിസ്റ്ററി (Audio)",
                description = "മോഴ്സ് കോഡ് സന്ദേശങ്ങൾ, സംഗീത താളങ്ങൾ, സോണാർ തരംഗങ്ങൾ എന്നിവ കേട്ട് ഉത്തരം കണ്ടെത്തുക.",
                stageType = StageType.AUDIO_BASED,
                timerSeconds = 20,
                totalPointsWeight = 60,
                orderIndex = 3
            )
        )

        // 3. Stage 1 Questions (MCQ)
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage1Id,
                questionText = "മഹാത്മാഗാന്ധി ചരിത്രപ്രസിദ്ധമായ ദണ്ഡി ഉപ്പുസത്യാഗ്രഹം ആരംഭിച്ച വർഷം ഏത്?",
                questionType = StageType.MCQ,
                optionA = "1920",
                optionB = "1930",
                optionC = "1942",
                optionD = "1915",
                correctAnswer = "B",
                points = 10,
                timerSeconds = 30,
                explanation = "1930 മാർച്ച് 12-നാണ് സബർമതി ആശ്രമത്തിൽ നിന്ന് ദണ്ഡിയിലേക്ക് ഉപ്പുസത്യാഗ്രഹ യാത്ര ആരംഭിച്ചത്."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage1Id,
                questionText = "റോക്കറ്റ് വിക്ഷേപണത്തിന്റെ അടിസ്ഥാന തത്വം വിശദീകരിക്കുന്ന ഭൗതികശാസ്ത്ര നിയമം ഏതാണ്?",
                questionType = StageType.MCQ,
                optionA = "ന്യൂട്ടന്റെ മൂന്നാം ചലന നിയമം",
                optionB = "ബർണോളി തത്വം",
                optionC = "കെപ്ലറുടെ രണ്ടാം നിയമം",
                optionD = "ഹൈസൻബർഗിന്റെ അനിശ്ചിതത്വ തത്വം",
                correctAnswer = "A",
                points = 10,
                timerSeconds = 30,
                explanation = "ഓരോ പ്രവർത്തനത്തിനും തുല്യവും വിപരീതവുമായ ഒരു പ്രതിപ്രവർത്തനം ഉണ്ടായിരിക്കും എന്ന ന്യൂട്ടന്റെ മൂന്നാം നിയമമാണ് ഇതിന്റെ ആധാരം."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage1Id,
                questionText = "ഗാന്ധിജിക്ക് 'മഹാത്മാ' എന്ന പദവി നൽകി ആദരിച്ച പ്രമുഖ വ്യക്തിത്വം ആര്?",
                questionType = StageType.MCQ,
                optionA = "സുഭാഷ് ചന്ദ്രബോസ്",
                optionB = "രവീന്ദ്രനാഥ ടാഗോർ",
                optionC = "ജവഹർലാൽ നെഹ്റു",
                optionD = "സർദാർ വല്ലഭ്ഭായ് പട്ടേൽ",
                correctAnswer = "B",
                points = 10,
                timerSeconds = 25,
                explanation = "1915-ൽ രവീന്ദ്രനാഥ ടാഗോറാണ് ഗാന്ധിജിയെ ആദ്യമായി 'മഹാത്മാ' എന്ന് അഭിസംബോധന ചെയ്തത്."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage1Id,
                questionText = "മലയാളത്തിലെ ആദ്യത്തെ സമ്പൂർണ്ണ ലക്ഷണമൊത്ത നോവലായി കണക്കാക്കപ്പെടുന്നത് ഏതാണ്?",
                questionType = StageType.MCQ,
                optionA = "കുന്ദലത",
                optionB = "മാർത്താണ്ഡവർമ്മ",
                optionC = "ഇന്ദുലേഖ",
                optionD = "ധർമ്മരാജാ",
                correctAnswer = "C",
                points = 10,
                timerSeconds = 20,
                explanation = "1889-ൽ ഒ. ചന്തുമേനോൻ രചിച്ച 'ഇന്ദുലേഖ'യാണ് മലയാളത്തിലെ ആദ്യത്തെ ലക്ഷണയുക്തമായ നോവൽ."
            )
        )

        // 4. Stage 2 Questions (Image-Based)
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage2Id,
                questionText = "2021-ൽ വിക്ഷേപിച്ച, സ്വർണ്ണ ഷഡ്ഭുജ ദർപ്പണങ്ങളുള്ള ലോകത്തിലെ ഏറ്റവും വലിയ സ്പേസ് ടെലിസ്കോപ്പ് ഏത്?",
                questionType = StageType.IMAGE_BASED,
                optionA = "ഹബിൾ സ്പേസ് ടെലിസ്കോപ്പ്",
                optionB = "ജെയിംസ് വെബ് സ്പേസ് ടെലിസ്കോപ്പ് (JWST)",
                optionC = "സ്പിറ്റ്സർ സ്പേസ് ടെലിസ്കോപ്പ്",
                optionD = "ചന്ദ്ര എക്സ്-റേ ഒബ്സർവേറ്ററി",
                correctAnswer = "B",
                mediaUri = "preset:telescope",
                mediaCaption = "18 സ്വർണ്ണ ലേപനം ചെയ്ത ബെറിലിയം ദർപ്പണങ്ങളുള്ള ഇൻഫ്രാറെഡ് ഒബ്സർവേറ്ററി",
                points = 15,
                timerSeconds = 25,
                explanation = "ഭൂമിയിൽ നിന്നും 15 ലക്ഷം കിലോമീറ്റർ അകലെയുള്ള L2 പോയിന്റിലാണ് ജെയിംസ് വെബ് പ്രവർത്തിക്കുന്നത്."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage2Id,
                questionText = "ഗാന്ധിജി അഹമ്മദാബാദിൽ സബർമതി നദീതീരത്ത് സ്ഥാപിച്ച ചരിത്രപ്രസിദ്ധമായ ആശ്രമം ഏത്?",
                questionType = StageType.IMAGE_BASED,
                optionA = "സബർമതി ആശ്രമം (അഹമ്മദാബാദ്)",
                optionB = "സേവാഗ്രാം ആശ്രമം (വർധ)",
                optionC = "ആഗാഖാൻ കൊട്ടാരം (പൂനെ)",
                optionD = "ഫിനിക്സ് സെറ്റിൽമെന്റ് (ഡർബൻ)",
                correctAnswer = "A",
                mediaUri = "preset:ashram",
                mediaCaption = "ഇന്ത്യൻ സ്വാതന്ത്ര്യസമര പോരാട്ടങ്ങളുടെ പ്രധാന കേന്ദ്രം",
                points = 15,
                timerSeconds = 25,
                explanation = "1917 മുതൽ 1930 വരെ ഗാന്ധിജിയുടെ പ്രധാന കർമ്മകേന്ദ്രമായിരുന്നു ഗുജറാത്തിലെ സബർമതി ആശ്രമം."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage2Id,
                questionText = "സ്വിറ്റ്സർലൻഡിൽ സ്ഥിതി ചെയ്യുന്ന ലോകത്തിലെ ഏറ്റവും വലിയ കണികാ പരീക്ഷണശാല (ലാർജ് ഹാഡ്രോൺ കൊളൈഡർ) ഏത് സ്ഥാപനത്തിന്റേതാണ്?",
                questionType = StageType.IMAGE_BASED,
                optionA = "സേൺ (CERN - Large Hadron Collider)",
                optionB = "ഫെർമിലാബ് (Fermilab)",
                optionC = "സ്ലാക്ക് ആക്സിലറേറ്റർ (SLAC)",
                optionD = "ഐഎസ്ആർഒ (ISRO)",
                correctAnswer = "A",
                mediaUri = "preset:cern",
                mediaCaption = "ഭൂമിക്കടിയിലുള്ള 27 കിലോമീറ്റർ ദൈർഘ്യമുള്ള പരീക്ഷണ വളയം",
                points = 15,
                timerSeconds = 25,
                explanation = "2012-ൽ 'ഹിഗ്സ് ബോസോൺ' കണിക കണ്ടെത്തിയത് സേണിലെ ലാർജ് ഹാഡ്രോൺ കൊളൈഡർ വഴിയാണ്."
            )
        )

        // 5. Stage 3 Questions (Audio-Based)
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage3Id,
                questionText = "ഈ ഓഡിയോ ശ്രദ്ധിച്ചു കേൾക്കൂ. അന്താരാഷ്ട്ര അടിയന്തര സഹായ സന്ദേശമായ മോഴ്സ് കോഡ് ഏതാണ്?",
                questionType = StageType.AUDIO_BASED,
                optionA = "S-O-S (... --- ...)",
                optionB = "C-Q-D (-.-. --.- -..)",
                optionC = "M-A-Y-D-A-Y",
                optionD = "O-K-A-Y",
                correctAnswer = "A",
                mediaUri = "preset:morse",
                mediaCaption = "800 Hz ഫ്രീക്വൻസിയിലുള്ള ടെലിഗ്രാഫിക് റേഡിയോ സിഗ്നൽ",
                points = 20,
                timerSeconds = 20,
                explanation = "മൂന്ന് ചെറിയ ഡോട്ട്, മൂന്ന് വലിയ ഡാഷ്, മൂന്ന് ഡോട്ട് എന്നിവ ചേർന്നതാണ് ലോകമെമ്പാടും ഉപയോഗിക്കുന്ന SOS സന്ദേശം."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage3Id,
                questionText = "ഈ മണിനാദ സംഗീതത്തിൽ കേൾക്കുന്ന ഹാർമോണിക് സ്വര ശ്രേണി ഏതാണ്?",
                questionType = StageType.AUDIO_BASED,
                optionA = "ഓഡ് ടു ജോയ് (ബീഥോവൻ)",
                optionB = "പെന്റാറ്റോണിക് ഹാർമോണിക് ബെൽസ് (C-E-G-B-C)",
                optionC = "ഫോർ സീസൺസ് (വിവാൽഡി)",
                optionD = "മോഹനം രാഗ സ്വരങ്ങൾ",
                correctAnswer = "B",
                mediaUri = "preset:melody",
                mediaCaption = "C മേജർ സ്വരത്തിലുള്ള ഹാർമോണിക് അക്കൗസ്റ്റിക് ചൈംസ്",
                points = 20,
                timerSeconds = 20,
                explanation = "ശുദ്ധമായ അനുരണനം നൽകുന്ന പെന്റാറ്റോണിക് ഫ്രീക്വൻസികളാണ് ഈ ക്ലിപ്പിലുള്ളത്."
            )
        )
        quizDao.insertQuestion(
            QuizQuestion(
                stageId = stage3Id,
                questionText = "സമുദ്രത്തിന്റെ അടിത്തട്ടിലെ വിവരങ്ങൾ കണ്ടെത്താൻ കപ്പലുകളും അന്തർവാഹിനികളും ഉപയോഗിക്കുന്ന ശബ്ദതരംഗ സംവിധാനം ഏത്?",
                questionType = StageType.AUDIO_BASED,
                optionA = "ആക്ടീവ് സോണാർ പിംഗ് (Active Sonar Ping)",
                optionB = "ഡോപ്ലർ റഡാർ സ്വീപ്പ്",
                optionC = "സീസ്‌മിക് ജിയോഫോൺ എക്കോ",
                optionD = "ലൈഡാർ ഒപ്റ്റിക്കൽ ഫ്രീക്വൻസി",
                correctAnswer = "A",
                mediaUri = "preset:sonar",
                mediaCaption = "വെള്ളത്തിനടിയിൽ പുറപ്പെടുവിക്കുന്ന അക്കൗസ്റ്റിക് പൾസ് സിഗ്നൽ",
                points = 20,
                timerSeconds = 20,
                explanation = "ജലത്തിനടിയിലെ വസ്തുക്കളെയും ആഴത്തെയും കണ്ടെത്താൻ സോണാർ (SONAR) ശബ്ദ തരംഗങ്ങൾ ഉപയോഗിക്കുന്നു."
            )
        )

        // 6. Registered Students
        val s1 = Student(
            name = "ആതിര വി. എം.",
            registerNumber = "KL-2026-101",
            departmentOrGrade = "ഫിസിക്സ് & അസ്ട്രോണമി",
            status = StudentStatus.COMPLETED,
            totalScore = 65,
            stagesCompleted = 3,
            timeSpentSeconds = 64
        )
        val s2 = Student(
            name = "രാഹുൽ കൃഷ്ണൻ",
            registerNumber = "KL-2026-102",
            departmentOrGrade = "കംപ്യൂട്ടർ സയൻസ്",
            status = StudentStatus.COMPLETED,
            totalScore = 80,
            stagesCompleted = 3,
            timeSpentSeconds = 52
        )
        val s3 = Student(
            name = "ഫാത്തിമ നസ്റീൻ",
            registerNumber = "KL-2026-103",
            departmentOrGrade = "ബയോടെക്നോളജി",
            status = StudentStatus.COMPLETED,
            totalScore = 55,
            stagesCompleted = 3,
            timeSpentSeconds = 71
        )
        val s4 = Student(
            name = "അർജുൻ മേനോൻ",
            registerNumber = "KL-2026-104",
            departmentOrGrade = "മെക്കാനിക്കൽ എൻജിനിയറിംഗ്",
            status = StudentStatus.REGISTERED,
            totalScore = 0,
            stagesCompleted = 0,
            timeSpentSeconds = 0
        )
        val s5 = Student(
            name = "അഞ്ജലി നായർ",
            registerNumber = "KL-2026-105",
            departmentOrGrade = "ചരിത്രം & ഭാഷാ വിഭാഗം",
            status = StudentStatus.REGISTERED,
            totalScore = 0,
            stagesCompleted = 0,
            timeSpentSeconds = 0
        )
        quizDao.insertStudents(listOf(s1, s2, s3, s4, s5))

        // Initial sample submissions
        quizDao.insertSubmissions(
            listOf(
                StudentSubmission(
                    studentId = 1L,
                    stageId = stage1Id,
                    questionId = 1L,
                    studentName = "ആതിര വി. എം.",
                    registerNumber = "KL-2026-101",
                    stageTitle = "റൗണ്ട് 1: പൊതുവിജ്ഞാനം & ചരിത്ര സ്മൃതി (MCQ)",
                    questionText = "മഹാത്മാഗാന്ധി ചരിത്രപ്രസിദ്ധമായ ദണ്ഡി ഉപ്പുസത്യാഗ്രഹം ആരംഭിച്ച വർഷം ഏത്?",
                    selectedAnswer = "B",
                    correctAnswer = "B",
                    isCorrect = true,
                    pointsEarned = 10,
                    timeSpentSeconds = 12
                ),
                StudentSubmission(
                    studentId = 2L,
                    stageId = stage1Id,
                    questionId = 1L,
                    studentName = "രാഹുൽ കൃഷ്ണൻ",
                    registerNumber = "KL-2026-102",
                    stageTitle = "റൗണ്ട് 1: പൊതുവിജ്ഞാനം & ചരിത്ര സ്മൃതി (MCQ)",
                    questionText = "മഹാത്മാഗാന്ധി ചരിത്രപ്രസിദ്ധമായ ദണ്ഡി ഉപ്പുസത്യാഗ്രഹം ആരംഭിച്ച വർഷം ഏത്?",
                    selectedAnswer = "B",
                    correctAnswer = "B",
                    isCorrect = true,
                    pointsEarned = 10,
                    timeSpentSeconds = 8
                ),
                StudentSubmission(
                    studentId = 2L,
                    stageId = stage2Id,
                    questionId = 5L,
                    studentName = "രാഹുൽ കൃഷ്ണൻ",
                    registerNumber = "KL-2026-102",
                    stageTitle = "റൗണ്ട് 2: ദൃശ്യ വിസ്മയം & ചിത്ര പസിലുകൾ (Image)",
                    questionText = "2021-ൽ വിക്ഷേപിച്ച, സ്വർണ്ണ ഷഡ്ഭുജ ദർപ്പണങ്ങളുള്ള ലോകത്തിലെ ഏറ്റവും വലിയ സ്പേസ് ടെലിസ്കോപ്പ് ഏത്?",
                    selectedAnswer = "B",
                    correctAnswer = "B",
                    isCorrect = true,
                    pointsEarned = 15,
                    timeSpentSeconds = 14
                ),
                StudentSubmission(
                    studentId = 2L,
                    stageId = stage3Id,
                    questionId = 8L,
                    studentName = "രാഹുൽ കൃഷ്ണൻ",
                    registerNumber = "KL-2026-102",
                    stageTitle = "റൗണ്ട് 3: ശബ്ദരേഖ & ഓഡിയോ മിസ്റ്ററി (Audio)",
                    questionText = "ഈ ഓഡിയോ ശ്രദ്ധിച്ചു കേൾക്കൂ. അന്താരാഷ്ട്ര അടിയന്തര സഹായ സന്ദേശമായ മോഴ്സ് കോഡ് ഏതാണ്?",
                    selectedAnswer = "A",
                    correctAnswer = "A",
                    isCorrect = true,
                    pointsEarned = 20,
                    timeSpentSeconds = 11
                )
            )
        )
    }
}
