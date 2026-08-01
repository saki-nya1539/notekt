package notekt.test

/**
 * PixelForge/PhysiSim/BookNest/TaskRexpress/HabitVaporと同じ設計思想の
 * 自作アサーションハーネス。Gradle無しでkotlinc単体でビルド・実行できるよう、
 * kotlin.test(JUnit依存)は使わず素朴なassertスタイルで統一している。
 */
class TestHarness {
    private var passed = 0
    private var failed = 0
    private val failures = mutableListOf<String>()

    fun run(name: String, block: () -> Unit) {
        try {
            block()
            passed++
            println("  [OK] $name")
        } catch (e: Throwable) {
            failed++
            failures.add("$name: ${e.message}")
            println("  [NG] $name: ${e.message}")
        }
    }

    fun assertTrue(condition: Boolean, message: String = "expected true") {
        if (!condition) throw AssertionError(message)
    }

    fun assertFalse(condition: Boolean, message: String = "expected false") = assertTrue(!condition, message)

    fun <T> assertEquals(expected: T, actual: T, message: String? = null) {
        assertTrue(expected == actual, message ?: "expected <$expected>, got <$actual>")
    }

    fun assertNull(value: Any?, message: String = "expected null") = assertTrue(value == null, message)

    fun assertNotNull(value: Any?, message: String = "expected non-null") = assertTrue(value != null, message)

    /** `block`が型`E`の例外を投げることを検証する。 */
    inline fun <reified E : Throwable> assertThrows(block: () -> Unit) {
        var thrown: Throwable? = null
        try {
            block()
        } catch (e: Throwable) {
            thrown = e
        }
        if (thrown == null) {
            throw AssertionError("expected ${E::class.simpleName} to be thrown, but nothing was")
        }
        if (thrown !is E) {
            throw AssertionError("expected ${E::class.simpleName}, got ${thrown::class.simpleName}")
        }
    }

    fun summary(): Triple<Int, Int, List<String>> = Triple(passed, failed, failures)
}
