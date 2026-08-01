import notekt.Cli
import notekt.NoteStore
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val path = System.getenv("NOTEKT_DATA") ?: "data/notes.tsv"
    val store = NoteStore(File(path))
    val cli = Cli(store)
    exitProcess(cli.run(args.toList()))
}
