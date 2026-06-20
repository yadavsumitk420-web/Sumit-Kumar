package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "articles")
data class NewsArticle(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val summary: String,
    val category: String,
    val date: String,
    val author: String,
    val imageUrl: String,
    val isBookmarked: Boolean = false,
    val isAiGenerated: Boolean = false,
    val bhojpuriTranslation: String? = null
)

@Dao
interface NewsArticleDao {
    @Query("SELECT * FROM articles ORDER BY date DESC")
    fun getAllArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY date DESC")
    fun getArticlesByCategory(category: String): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY date DESC")
    fun getBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): NewsArticle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: NewsArticle)

    @Update
    suspend fun updateArticle(article: NewsArticle)

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean)
}

@Database(entities = [NewsArticle::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsArticleDao(): NewsArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buxar_news_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
