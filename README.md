# 🎓 Student Helper — Smart Student Workspace & AI Study Copilot

**Student Helper** is an all-in-one native Android collaborative workspace and study assistant built with **Jetpack Compose**, **Kotlin**, **Room Database**, and **Google Gemini 3.5 Flash API integration**.

Designed specifically to tackle student cognitive overload, Student Helper unifies structured block-based note-taking, active recall learning tools, automated homework tracking with push alarms, weekly class timetables, and Pomodoro focus blocks into a single offline-first native application.

---

## ✨ Key Features

### 📝 1. Notion-Style Block Note Editor
- **Rich Block Types**: Build structured lecture notes using `Headings`, `Paragraphs`, `To-Do Checklists`, `Bullet Lists`, `Callout Boxes`, `Code/Formula Snippets`, and `Quotes`.
- **Media & Document Support**: Attach images/diagrams and PDF documents with native file picking and internal URI persistence.
- **Peer Collaboration**: View live co-authoring collaborator avatars and discuss notes using an integrated peer comment system.

### 🧠 2. Active Recall Study Mode & Flashcards
- **Interactive Flashcard Blocks**: Embed study flashcards directly inside lecture notes.
- **3D Card-Flip Study Dialog**: Practice active recall with an interactive study overlay featuring card flips, mastery tracking (*Mastered* vs. *Needs Review*), and performance breakdowns.

### 🤖 3. Gemini 3.5 Flash AI Study Copilot
- **Multimodal Visual Analysis**: Upload textbook images, lecture slides, or handwritten diagrams to extract key concepts, formulas, and high-yield exam takeaways.
- **Automated Study Tools**:
  - **Note Summarization**: Generate structured markdown summaries with core takeaways.
  - **Practice Quiz Generation**: Instantly generate active-recall multiple-choice quizzes from notes.
  - **Task Extraction**: Scan notes for homework items and auto-populate them into your agenda.
- **Smart Local Fallback**: Full offline functionality with built-in algorithmic fallbacks if no API key is provided.

### 📅 4. Daily Student Agenda & Push Alarms
- **Priority & Due Date Filtering**: Categorize tasks by subject and priority (`URGENT`, `HIGH`, `MEDIUM`, `LOW`).
- **Exact System Alarms**: High-priority push notifications scheduled via Android `AlarmManager` and `BroadcastReceiver` so you never miss a deadline.

### 🗓️ 5. Class Schedule & Timetable Manager
- **Weekly Timetable**: Interactive daily class schedule tracker showing professors, classrooms, and course codes.
- **One-Tap Note Creation**: Convert any scheduled class into a pre-formatted lecture note template instantly.

### ⏱️ 6. Pomodoro Focus Timer
- **Customizable Intervals**: Switch seamlessly between 25-minute focus blocks, short breaks, and long breaks.
- **Subject Analytics**: Track total study minutes and focus session history organized by subject.
- **Haptic Completion Feedback**: Integrated vibration cues upon completing focus sessions.

### 🔄 7. Offline-First CRDT Sync Engine
- **Room Database Storage**: Complete offline-first architecture powered by Room (`version 3`).
- **Conflict-Free Sync Queue**: Background queue monitoring network connectivity to synchronize local changes across student devices.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3 Theme with Light, Dark & Auto modes)
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Local Database**: Room Database (`AppDatabase`)
- **Networking & API**: Retrofit 2, Moshi, OkHttp 4
- **AI Integration**: Google Gemini 3.5 Flash REST API (`gemini-3.5-flash:generateContent`)
- **Image Loading**: Coil (`coil-compose`)
- **Background Tasks & Notifications**: Android `AlarmManager`, `BroadcastReceiver`, Notification Channels

---

## 📂 Project Structure

```text
app/src/main/java/com/example/
├── ai/                # Gemini Client, Request/Response Data Classes & Service Logic
├── database/          # Room Entities, DAOs, and AppDatabase Definition
├── model/             # Domain Models (Page, Block, Task, Schedule, FocusSession)
├── receiver/          # Task Reminder BroadcastReceiver for push alarms
├── reminder/          # AlarmManager scheduling utility
├── repository/        # Central Workspace Repository & Seed Data Logic
├── sync/              # Offline-first Sync Engine & Queue Processor
├── ui/
│   ├── components/    # Common UI widgets (BlockItemView, FlashcardDialog, Badges)
│   ├── screens/       # Main Tab Screens (Pages, Tasks, Schedule, Focus, AI Tutor)
│   └── theme/         # Material 3 Color Schemes & Typography
└── viewmodel/         # WorkspaceViewModel managing reactive StateFlows
