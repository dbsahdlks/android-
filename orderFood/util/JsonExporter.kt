import android.content.Context
import com.example.orderfood.dao.AppDatabase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class JsonExporter(private val context: Context, private val db: AppDatabase) {

    // 创建 Gson 实例
    private val gson = Gson()

    suspend fun exportToJsonServerFormat(): File {
        return withContext(Dispatchers.IO) {
            // 创建导出目录
            val exportDir = File(context.getExternalFilesDir(null), "json_export")
            if (!exportDir.exists()) exportDir.mkdirs()

            // 导出数据
            val jsonData = mapOf(
                "businesses" to exportBusinesses(),
                "users" to exportUsers()
            )

            // 写入文件
            val jsonFile = File(exportDir, "db.json")
            FileWriter(jsonFile).use { writer ->
                gson.toJson(jsonData, writer)
            }
            jsonFile
        }
    }

    private suspend fun exportBusinesses(): List<BusinessExport> {
        val businesses = db.businessDao().getAllBusinesses()
        return businesses.map { business ->
            BusinessExport(
                id = business.id,
                businessId = business.businessId,
                password = business.password,
                name = business.name,
                description = business.description,
                type = business.type,
                imagePath = business.imagePath
            )
        }
    }

    private suspend fun exportUsers(): List<UserExport> {
        val users = db.userDao().getAllUsers()
        return users.map { user ->
            UserExport(
                id = user.userId, // 使用 userId 作为主键
                password = user.password,
                name = user.name,
                sex = user.sex,
                address = user.address,
                phone = user.phone,
                imagePath = user.imagePath
            )
        }
    }

    // 导出数据结构
    data class BusinessExport(
        val id: Int,
        val businessId: String,
        val password: String,
        val name: String,
        val description: String,
        val type: String,
        val imagePath: String
    )

    data class UserExport(
        val id: String,  // 使用 userId 作为主键
        val password: String,
        val name: String,
        val sex: String,
        val address: String,
        val phone: String,
        val imagePath: String
    )
}