package com.example.androidintermediate.views.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.utils.Constant
import kotlinx.coroutines.launch

class SettingViewModel(private val pref: SettingPreferences): ViewModel() {

    fun getUserPreferences(property:String): LiveData<String> {
        return when(property){
            Constant.UserPreferences.UserUID.name -> pref.getUserUid().asLiveData()
            Constant.UserPreferences.UserToken.name -> pref.getUserToken().asLiveData()
            Constant.UserPreferences.UserName.name -> pref.getUserName().asLiveData()
            Constant.UserPreferences.UserEmail.name -> pref.getUserEmail().asLiveData()
            Constant.UserPreferences.UserLastLogin.name -> pref.getUserLastLogin().asLiveData()
            else -> pref.getUserUid().asLiveData()
        }
    }

    fun setUserPreferences(userToken: String, userUid: String, userName:String, userEmail: String) {
        viewModelScope.launch {
            pref.saveLoginSession(userToken,userUid,userName,userEmail)
        }
    }

    fun clearUserPreferences() {
        viewModelScope.launch {
            pref.clearLoginSession()
        }
    }

    class Factory(private val pref: SettingPreferences) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingViewModel(pref) as T
    }
}