package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity

@Database(entities = [PostEntity::class], version = 2, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE PostEntity_new (id INTEGER PRIMARY KEY NOT NULL, author TEXT NOT NULL, authorAvatar TEXT NOT NULL, content TEXT NOT NULL, published TEXT NOT NULL, likedByMe INTEGER NOT NULL, likes INTEGER NOT NULL, hidden INTEGER NOT NULL DEFAULT 0)")
                database.execSQL("INSERT INTO PostEntity_new (id, author, authorAvatar, content, published, likedByMe, likes) SELECT id, author, authorAvatar, content, published, likedByMe, likes FROM PostEntity")
                database.execSQL("DROP TABLE PostEntity")
                database.execSQL("ALTER TABLE PostEntity_new RENAME TO PostEntity")
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
    }
}