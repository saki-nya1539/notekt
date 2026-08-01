package notekt

/**
 * 1件のメモを表すデータクラス。
 */
data class Note(
    val id: Int,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    /** タイトル・本文・タグのいずれかに`query`が(大文字小文字を無視して)含まれるか。 */
    fun matches(query: String): Boolean {
        val q = query.lowercase()
        return title.lowercase().contains(q) ||
            body.lowercase().contains(q) ||
            tags.any { it.lowercase().contains(q) }
    }

    /** 指定したタグを(大文字小文字を無視して)持っているか。 */
    fun hasTag(tag: String): Boolean = tags.any { it.equals(tag, ignoreCase = true) }
}
