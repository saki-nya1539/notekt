package notekt

import java.io.File

/** NoteStoreの操作で発生しうるエラー。 */
sealed class NoteStoreError(message: String) : Exception(message) {
    class NotFound(id: Int) : NoteStoreError("note not found: #$id")
    class EmptyTitle : NoteStoreError("title is required")
}

/**
 * メモの永続化(NoteFormatによるテキストファイル)とCRUD・検索を担当する。
 */
class NoteStore(private val file: File) {
    private val notes: MutableList<Note> = NoteFormat.readAll(file).toMutableList()

    fun all(): List<Note> = notes.sortedByDescending { it.createdAt }

    fun find(id: Int): Note? = notes.firstOrNull { it.id == id }

    fun add(title: String, body: String, tags: List<String>): Note {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) throw NoteStoreError.EmptyTitle()

        val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        val note = Note(id = nextId, title = trimmedTitle, body = body, tags = tags)
        notes.add(note)
        save()
        return note
    }

    fun remove(id: Int): Note {
        val note = find(id) ?: throw NoteStoreError.NotFound(id)
        notes.remove(note)
        save()
        return note
    }

    fun search(query: String? = null, tag: String? = null): List<Note> =
        all()
            .filter { query == null || it.matches(query) }
            .filter { tag == null || it.hasTag(tag) }

    private fun save() = NoteFormat.writeAll(notes, file)
}
