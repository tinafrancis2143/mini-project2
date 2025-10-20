package com.example.miniproject2.data

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

// =====================================================================================
// PART 1: ENTITIES
// =====================================================================================

@Entity(tableName = "ingredients_table")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "ingredient_name") val name: String,
    @ColumnInfo(name = "overall_score") val overallScore: Int,
    @ColumnInfo(name = "skin_score") val skinScore: Int,
    @ColumnInfo(name = "hair_score") val hairScore: Int,
    @ColumnInfo(name = "body_score") val bodyScore: Int,
    @ColumnInfo(name = "eyes_score") val eyesScore: Int,
    @ColumnInfo(name = "lips_score") val lipsScore: Int
)

@Entity(tableName = "my_products_table")
data class MyProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "brand_name")
    val brand: String,
    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long
)

// =====================================================================================
// PART 2: DAOs
// =====================================================================================

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients_table WHERE ingredient_name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findIngredientByName(name: String): Ingredient?
}

@Dao
interface MyProductDao {
    // --- THIS IS THE CORRECTED LINE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: MyProduct): Long // It now returns the new ID

    @Query("SELECT * FROM my_products_table ORDER BY expiry_date ASC")
    fun getAllProducts(): Flow<List<MyProduct>>

    @Query("DELETE FROM my_products_table WHERE id = :productId")
    suspend fun deleteProductById(productId: Int)
}

// =====================================================================================
// PART 3: THE DATABASE CLASS
// =====================================================================================

@Database(entities = [Ingredient::class, MyProduct::class], version = 2, exportSchema = false)
abstract class IngredientDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun myProductDao(): MyProductDao

    companion object {
        @Volatile
        private var INSTANCE: IngredientDatabase? = null

        fun getDatabase(context: Context): IngredientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IngredientDatabase::class.java,
                    "ingredient_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(IngredientDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class IngredientDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.ingredientDao(), context)
                }
            }
        }

        suspend fun populateDatabase(ingredientDao: IngredientDao, context: Context) {
            // This code for reading the CSV remains unchanged
            try {
                val inputStream = context.assets.open("cosmetic_ingredient_toxicity_full.csv")
                val buffer = BufferedReader(InputStreamReader(inputStream))
                buffer.readLine()

                val lines = buffer.readLines()
                lines.forEach { line ->
                    val columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                    if (columns.size >= 7) {
                        val ingredient = Ingredient(
                            id = 0,
                            name = columns[0].trim().removeSurrounding("\""),
                            overallScore = columns[1].toIntOrNull() ?: 0,
                            skinScore = columns[2].toIntOrNull() ?: 0,
                            hairScore = columns[3].toIntOrNull() ?: 0,
                            bodyScore = columns[4].toIntOrNull() ?: 0,
                            eyesScore = columns[5].toIntOrNull() ?: 0,
                            lipsScore = columns[6].toIntOrNull() ?: 0
                        )
                        ingredientDao.insert(ingredient)
                    }
                }
                Log.d("DatabasePopulation", "Successfully populated ingredients from CSV.")
            } catch (e: Exception) {
                Log.e("DatabasePopulation", "Error populating ingredients table", e)
            }
        }
    }
}