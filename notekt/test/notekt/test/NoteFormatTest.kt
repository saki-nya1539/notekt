package notekt.test

import notekt.Note
import notekt.NoteFormat
import java.io.File

fun testNoteFormat(h: TestHarness) {
    h.run("エンコードしたものをデコードすると元に戻る") {
        val note = Note(id = 1, title = "タイトル", body = "本文\n複数行", tags = listOf("a", "b"), createdAt = 12345L)
        val decoded = NoteFormat.decode(NoteFormat.encode(note))
        h.assertEquals(note, decoded)
    }

    h.run("タブ・改行・バックスラッシュを含む文字列も正しく往復できる") {
        val note = Note(id = 2, title = "タブ\tを含む", body = "改行\nと\\バックスラッシュ", tags = emptyList(), createdAt = 1L)
        val decoded = NoteFormat.decode(NoteFormat.encode(note))
        h.assertEquals(note, decoded)
    }

    h.run("タグなしのノートも往復できる") {
        val note = Note(id = 3, title = "T", body = "B", tags = emptyList(), createdAt = 1L)
        val decoded = NoteFormat.decode(NoteFormat.encode(note))
        h.assertEquals(emptyList<String>(), decoded?.tags)
    }

    h.run("ファイルへの書き込みと読み込みが一致する") {
        val tempFile = File.createTempFile("notekt_test", ".tsv")
        tempFile.deleteOnExit()
        val notes = listOf(
            Note(id = 1, title = "A", body = "a", tags = listOf("x"), createdAt = 1L),
            Note(id = 2, title = "B", body = "b", tags = emptyList(), createdAt = 2L)
        )
        NoteFormat.writeAll(notes, tempFile)
        val loaded = NoteFormat.readAll(tempFile)
        h.assertEquals(notes, loaded)
    }

    h.run("存在しないファイルを読むと空リストを返す") {
        val loaded = NoteFormat.readAll(File("/tmp/notekt_does_not_exist_${System.nanoTime()}.tsv"))
        h.assertTrue(loaded.isEmpty())
    }

    h.run("フィールド数が不正な行はデコードするとnullになる") {
        h.assertNull(NoteFormat.decode("1\tonly two fields"))
    }
}
