package com.harshit.contactsjc

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val contactRepository = ContactRepository(application)


    private val _item : MutableState<ContactModel?> = mutableStateOf(null)
    val item: State<ContactModel?> = _item

    fun setItem(contact: ContactModel){
        _item.value = contact
    }

    private val _expand = MutableLiveData(false)
    val expand: LiveData<Boolean> = _expand

    fun setExpand(value: Boolean) {
        _expand.value = value
    }

    private val _contactListLiveData = MutableLiveData<List<ContactModel>>()
    val contactListLiveData: LiveData<List<ContactModel>> get() = _contactListLiveData

    fun saveContact(name: String, phoneNumber: String) {
        viewModelScope.launch {
            val result = contactRepository.saveContact(name, phoneNumber)
            if(result){
                getContacts()
            }
        }
    }
    fun getContacts(){
        viewModelScope.launch {
            try {
                _contactListLiveData.value =  contactRepository.getContacts()
            }catch (_:Exception){

            }
        }
    }
}
