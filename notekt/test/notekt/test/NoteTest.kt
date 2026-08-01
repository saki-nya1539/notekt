package notekt.test

import notekt.Note

fun testNote(h: TestHarness) {
    h.run("matchesはタイトル・本文・タグの部分一致で判定する(大文字小文字無視)") {
        val note = Note(id = 1, title = "Kotlin入門", body = "sealed classについて", tags = listOf("kotlin", "study"))
        h.assertTrue(note.matches("kotlin"))
        h.assertTrue(note.matches("sealed"))
        h.assertTrue(note.matches("STUDY"))
        h.assertFalse(note.matches("java"))
    }

    h.run("hasTagは大文字小文字を無視して判定する") {
        val note = Note(id = 1, title = "T", body = "", tags = listOf("Work"))
        h.assertTrue(note.hasTag("work"))
        h.assertFalse(note.hasTag("home"))
    }
}
