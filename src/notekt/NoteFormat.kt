package notekt

import java.io.File

/**
 * JDK標準にJSONサポートが無く、kotlinx.serializationやGson等の外部ライブラリも
 * 使わない方針のため、Note専用の軽量なテキスト形式を自前で実装している。
 *
 * フォーマット: 1行1ノート、フィールドはタブ区切り。
 *   id \t escapedTitle \t escapedBody \t tag1,tag2,... \t createdAt
 *
 * タイトル・本文に含まれうるタブ/改行/バックスラッシュは区切り文字と衝突しないよう
 * エスケープしてから書き込み、読み込み時に元に戻す。
 */
object NoteFormat {
    private const val FIELD_SEP = "\t"
    private const val TAG_SEP = ","

    fun encode(note: Note): String {
        val tags = note.tags.joinToString(TAG_SEP)
        return listOf(
            note.id.toString(),
            escape(note.title),
            escape(note.body),
            tags,
            note.createdAt.toString()
        ).joinToString(FIELD_SEP)
    }

    fun decode(line: String): Note? {
        val parts = line.split(FIELD_SEP)
        if (parts.size != 5) return null

        val id = parts[0].toIntOrNull() ?: return null
        val title = unescape(parts[1])
        val body = unescape(parts[2])
        val tags = if (parts[3].isEmpty()) emptyList() else parts[3].split(TAG_SEP)
        val createdAt = parts[4].toLongOrNull() ?: return null

        return Note(id, title, body, tags, createdAt)
    }

    fun writeAll(notes: List<Note>, file: File) {
        file.parentFile?.mkdirs()
        file.writeText(notes.joinToString("\n") { encode(it) })
    }

    fun readAll(file: File): List<Note> {
        if (!file.exists()) return emptyList()
        return file.readLines()
            .filter { it.isNotBlank() }
            .mapNotNull { decode(it) }
    }

    // バックスラッシュを先に二重化してから他の特殊文字をエスケープすることで、
    // 後段の置換が生成した新しいバックスラッシュを誤って再エスケープしないようにしている。
    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")

    private fun unescape(value: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                when (value[i + 1]) {
                    '\\' -> { sb.append('\\'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    else -> { sb.append(c); i += 1 }
                }
            } else {
                sb.append(c)
                i += 1
            }
        }
        return sb.toString()
    }
}
