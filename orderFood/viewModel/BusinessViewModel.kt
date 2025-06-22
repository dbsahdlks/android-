package com.example.orderfood.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.orderfood.dao.AppDatabase
import com.example.orderfood.entity.Business
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BusinessViewModel(private val database: AppDatabase) : ViewModel() {

    private val _business = MutableStateFlow<Business?>(null)
    val business: StateFlow<Business?> = _business

    // 加载商家信息
    fun loadBusiness(businessId: String) {
        viewModelScope.launch {
            val business = database.businessDao().getBusinessById(businessId)
            _business.value = business
        }
    }
    suspend fun getBusinessById(businessId: String): Business? {
        return database.businessDao().getBusinessById(businessId)
    }
    fun deleteBusiness(businessId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.businessDao().deleteByBusinessId(businessId) // 调用 DAO 方法
        }
    }
    suspend fun checkPassword(businessId: String, inputPassword: String): Boolean {
        val business = getBusinessById(businessId) ?: return false
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), business.password).verified
    }
}