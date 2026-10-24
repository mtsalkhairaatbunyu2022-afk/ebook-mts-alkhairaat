package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("E-BOOK MTS", appName)
  }

  @Test
  fun `load logo drawable from resources`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.logo_alkhairaat)
    assertNotNull(drawable)
  }

  @Test
  fun `launch MainActivity without crash`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    assertNotNull(controller.get())
  }

  @Test
  fun `test note insert edit and delete functions`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val noteDao = db.noteDao()

    val initialNote = com.example.data.NoteEntity(
        id = 101L,
        userId = "user_test",
        bookId = "book_1",
        bookTitle = "Bahasa Arab",
        pageNumber = 12,
        noteContent = "Catatan awal sebelum diedit",
        gradeClass = "Kelas VII"
    )
    noteDao.insertNote(initialNote)

    val notesBefore = noteDao.getNotesForGrade("Kelas VII").first()
    assertEquals(1, notesBefore.size)
    assertEquals("Catatan awal sebelum diedit", notesBefore[0].noteContent)

    // Edit note content
    val updatedNote = initialNote.copy(
        noteContent = "Catatan berhasil diperbarui/diedit",
        pageNumber = 15
    )
    noteDao.updateNote(updatedNote)

    val notesAfterUpdate = noteDao.getNotesForGrade("Kelas VII").first()
    assertEquals(1, notesAfterUpdate.size)
    assertEquals("Catatan berhasil diperbarui/diedit", notesAfterUpdate[0].noteContent)
    assertEquals(15, notesAfterUpdate[0].pageNumber)

    // Delete note
    noteDao.deleteNote(101L)
    val notesAfterDelete = noteDao.getNotesForGrade("Kelas VII").first()
    assertEquals(0, notesAfterDelete.size)

    db.close()
  }

  @Test
  fun `test roman numeral conversion and precision page mapping`() {
    // 1. Roman conversions
    assertEquals("i", com.example.ui.toRoman(1))
    assertEquals("ii", com.example.ui.toRoman(2))
    assertEquals("iii", com.example.ui.toRoman(3))
    assertEquals("iv", com.example.ui.toRoman(4))
    assertEquals(1, com.example.ui.fromRoman("i"))
    assertEquals(3, com.example.ui.fromRoman("iii"))

    // 2. Page mapping for textbook (142 pages, first content page at index 4)
    val pages = com.example.ui.generatePageMapping(pageCount = 142, firstArabicIndex = 4)
    assertEquals(142, pages.size)

    // Cover Page
    assertEquals(com.example.ui.PageCategory.COVER, pages[0].category)
    assertEquals("Sampul", pages[0].displayLabel)

    // Roman Preliminaries
    assertEquals(com.example.ui.PageCategory.ROMAN, pages[1].category)
    assertEquals("i", pages[1].displayLabel)
    assertEquals("Kata Pengantar (i)", pages[1].fullTitle)

    assertEquals(com.example.ui.PageCategory.ROMAN, pages[2].category)
    assertEquals("ii", pages[2].displayLabel)

    assertEquals(com.example.ui.PageCategory.ROMAN, pages[3].category)
    assertEquals("iii", pages[3].displayLabel)
    assertEquals("Daftar Isi (iii)", pages[3].fullTitle)

    // Arabic Content: Page 1 starts precisely at physical index 4!
    val pageOne = pages[4]
    assertEquals(com.example.ui.PageCategory.ARABIC, pageOne.category)
    assertEquals("1", pageOne.displayLabel)
    assertEquals(1, pageOne.arabicNumber)
    assertEquals("Halaman 1", pageOne.fullTitle)

    // Search page "1" precisely matches index 4
    val foundPageOne = pages.find { it.category == com.example.ui.PageCategory.ARABIC && it.arabicNumber == 1 }
    assertNotNull(foundPageOne)
    assertEquals(4, foundPageOne!!.physicalIndex)

    // Search roman "iii" matches index 3
    val foundRomanThree = pages.find { it.category == com.example.ui.PageCategory.ROMAN && it.displayLabel == "iii" }
    assertNotNull(foundRomanThree)
    assertEquals(3, foundRomanThree!!.physicalIndex)
  }
}
