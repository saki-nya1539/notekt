package notekt

import java.io.PrintStream

/**
 * コマンドライン引数を解釈して、NoteStoreへの操作に変換する。
 * 使い方: notekt <コマンド> [オプション]
 */
class Cli(
    private val store: NoteStore,
    private val out: PrintStream = System.out,
    private val err: PrintStream = System.err
) {
    fun run(args: List<String>): Int {
        val command = args.firstOrNull()
        val rest = args.drop(1)

        return try {
            when (command) {
                "add" -> cmdAdd(rest)
                "list", "ls" -> cmdList()
                "search" -> cmdSearch(rest)
                "show" -> cmdShow(rest)
                "rm", "delete" -> cmdRemove(rest)
                null, "help", "-h", "--help" -> {
                    out.println(usage)
                    0
                }
                else -> {
                    err.println("unknown command: $command\n\n$usage")
                    1
                }
            }
        } catch (e: NoteStoreError) {
            err.println("error: ${e.message}")
            1
        } catch (e: IllegalArgumentException) {
            err.println("error: ${e.message}")
            1
        }
    }

    private fun cmdAdd(args: List<String>): Int {
        val options = parseOptions(args)
        val title = options.positional.joinToString(" ")
        val body = options.named["body"] ?: ""
        val note = store.add(title, body, options.tags)
        out.println("added #${note.id}: ${note.title}")
        return 0
    }

    private fun cmdList(): Int {
        val notes = store.all()
        if (notes.isEmpty()) {
            out.println("(0 notes)")
            return 0
        }
        notes.forEach { out.println(format(it)) }
        return 0
    }

    private fun cmdSearch(args: List<String>): Int {
        val options = parseOptions(args)
        val query = options.positional.joinToString(" ").ifBlank { null }
        val tag = options.named["tag"]
        val notes = store.search(query, tag)
        if (notes.isEmpty()) {
            out.println("(0 notes)")
            return 0
        }
        notes.forEach { out.println(format(it)) }
        return 0
    }

    private fun cmdShow(args: List<String>): Int {
        val id = parseId(args)
        val note = store.find(id) ?: throw NoteStoreError.NotFound(id)
        out.println("#${note.id}: ${note.title}")
        if (note.tags.isNotEmpty()) out.println("tags: ${note.tags.joinToString(", ")}")
        out.println()
        out.println(note.body)
        return 0
    }

    private fun cmdRemove(args: List<String>): Int {
        val id = parseId(args)
        val note = store.remove(id)
        out.println("removed #${note.id}: ${note.title}")
        return 0
    }

    private fun parseId(args: List<String>): Int {
        val raw = args.firstOrNull() ?: throw IllegalArgumentException("note id is required")
        return raw.toIntOrNull() ?: throw IllegalArgumentException("invalid note id: $raw")
    }

    private data class Options(
        val positional: List<String>,
        val named: Map<String, String>,
        val tags: List<String>
    )

    // --tag T (複数指定可) / --body B のようなフラグを解析し、
    // フラグ以外の引数はpositionalにまとめる。
    private fun parseOptions(args: List<String>): Options {
        val positional = mutableListOf<String>()
        val named = mutableMapOf<String, String>()
        val tags = mutableListOf<String>()

        var i = 0
        while (i < args.size) {
            val arg = args[i]
            when {
                arg == "--tag" && i + 1 < args.size -> {
                    tags.add(args[i + 1])
                    i += 2
                }
                arg == "--body" && i + 1 < args.size -> {
                    named["body"] = args[i + 1]
                    i += 2
                }
                else -> {
                    positional.add(arg)
                    i += 1
                }
            }
        }
        return Options(positional, named, tags)
    }

    private fun format(note: Note): String {
        val tagsPart = if (note.tags.isEmpty()) "" else " [${note.tags.joinToString(", ")}]"
        return "#${note.id} ${note.title}$tagsPart"
    }

    private val usage = """
        使い方: notekt <コマンド> [オプション]

        コマンド:
          add <タイトル> [--body 本文] [--tag タグ]...
          list
          search <キーワード> [--tag タグ]
          show <id>
          rm <id>
          help
    """.trimIndent()
}
