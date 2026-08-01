package notekt.test

import notekt.Cli
import notekt.NoteStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

private fun makeCli(): Triple<Cli, ByteArrayOutputStream, ByteArrayOutputStream> {
    val file = File.createTempFile("notekt_cli_test", ".tsv")
    file.deleteOnExit()
    file.delete()
    val store = NoteStore(file)
    val outBuf = ByteArrayOutputStream()
    val errBuf = ByteArrayOutputStream()
    val cli = Cli(store, PrintStream(outBuf), PrintStream(errBuf))
    return Triple(cli, outBuf, errBuf)
}

fun testCli(h: TestHarness) {
    h.run("addコマンドでノートが追加される") {
        val (cli, out, _) = makeCli()
        cli.run(listOf("add", "Buy milk", "--tag", "home"))
        h.assertTrue(out.toString().contains("added #1"))
    }

    h.run("listコマンドでノート一覧が表示される") {
        val (cli, out, _) = makeCli()
        cli.run(listOf("add", "Note A"))
        cli.run(listOf("list"))
        h.assertTrue(out.toString().contains("Note A"))
    }

    h.run("searchコマンドでキーワード絞り込みができる") {
        val (cli, out, _) = makeCli()
        cli.run(listOf("add", "Kotlin note", "--tag", "kotlin"))
        cli.run(listOf("add", "Ruby note", "--tag", "ruby"))
        cli.run(listOf("search", "Kotlin"))
        val output = out.toString()
        h.assertTrue(output.contains("Kotlin note"))
        h.assertFalse(output.contains("Ruby note"))
    }

    h.run("showコマンドで本文が表示される") {
        val (cli, out, _) = makeCli()
        cli.run(listOf("add", "Note A", "--body", "hello world"))
        cli.run(listOf("show", "1"))
        h.assertTrue(out.toString().contains("hello world"))
    }

    h.run("rmコマンドでノートが削除される") {
        val (cli, out, _) = makeCli()
        cli.run(listOf("add", "Note A"))
        cli.run(listOf("rm", "1"))
        h.assertTrue(out.toString().contains("removed #1"))
    }

    h.run("存在しないコマンドはエラーになり終了コード1を返す") {
        val (cli, _, err) = makeCli()
        val code = cli.run(listOf("nope"))
        h.assertEquals(1, code)
        h.assertTrue(err.toString().contains("unknown command"))
    }

    h.run("存在しないidのshowはエラーになり終了コード1を返す") {
        val (cli, _, err) = makeCli()
        val code = cli.run(listOf("show", "999"))
        h.assertEquals(1, code)
        h.assertTrue(err.toString().contains("note not found"))
    }
}
