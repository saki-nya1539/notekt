package notekt.test

import notekt.NoteStore
import notekt.NoteStoreError
import java.io.File

// 「新規作成できるか」も確認したいので、あえて存在しないファイルパスから始める。
private fun tempStore(): NoteStore {
    val file = File.createTempFile("notekt_store_test", ".tsv")
    file.deleteOnExit()
    file.delete()
    return NoteStore(file)
}

fun testNoteStore(h: TestHarness) {
    h.run("ノートを追加して取得できる") {
        val store = tempStore()
        val note = store.add("Buy milk", "2%のやつ", listOf("home"))
        h.assertEquals("Buy milk", store.find(note.id)?.title)
    }

    h.run("IDは1から自動採番される") {
        val store = tempStore()
        val n1 = store.add("A", "", emptyList())
        val n2 = store.add("B", "", emptyList())
        h.assertEquals(1, n1.id)
        h.assertEquals(2, n2.id)
    }

    h.run("空タイトルはエラー") {
        val store = tempStore()
        h.assertThrows<NoteStoreError.EmptyTitle> { store.add("   ", "", emptyList()) }
    }

    h.run("削除すると取得できなくなる") {
        val store = tempStore()
        val note = store.add("A", "", emptyList())
        store.remove(note.id)
        h.assertNull(store.find(note.id))
    }

    h.run("存在しないIDの削除はエラー") {
        val store = tempStore()
        h.assertThrows<NoteStoreError.NotFound> { store.remove(999) }
    }

    h.run("searchはキーワードで絞り込む") {
        val store = tempStore()
        store.add("Kotlin note", "about sealed class", listOf("kotlin"))
        store.add("Ruby note", "about blocks", listOf("ruby"))
        val results = store.search(query = "kotlin")
        h.assertEquals(1, results.size)
    }

    h.run("searchはタグで絞り込む") {
        val store = tempStore()
        store.add("A", "", listOf("work"))
        store.add("B", "", listOf("home"))
        val results = store.search(tag = "home")
        h.assertEquals(1, results.size)
        h.assertEquals("B", results[0].title)
    }

    h.run("保存後、新しいNoteStoreインスタンスからも読み込める(永続化)") {
        val file = File.createTempFile("notekt_persist_test", ".tsv")
        file.deleteOnExit()
        file.delete()

        val store1 = NoteStore(file)
        store1.add("Persisted", "body", listOf("tag1"))

        val store2 = NoteStore(file)
        h.assertEquals(1, store2.all().size)
        h.assertEquals("Persisted", store2.all().first().title)
    }
}
