import notekt.test.TestHarness
import notekt.test.testCli
import notekt.test.testNote
import notekt.test.testNoteFormat
import notekt.test.testNoteStore

fun main() {
    val h = TestHarness()

    println("Note:")
    testNote(h)

    println("\nNoteFormat:")
    testNoteFormat(h)

    println("\nNoteStore:")
    testNoteStore(h)

    println("\nCli:")
    testCli(h)

    val (passed, failed, failures) = h.summary()
    println("\n" + "-".repeat(40))
    println("passed: $passed, failed: $failed")

    if (failed > 0) {
        println("\nFAILURES:")
        failures.forEach { println("  - $it") }
        kotlin.system.exitProcess(1)
    }

    println("\n全テスト成功")
}
