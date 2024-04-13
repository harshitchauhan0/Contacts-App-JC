package com.harshit.contactsjc

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.HashSet

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    companion object{
        private val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
    }

    private val _item : MutableState<ContactModel?> = mutableStateOf(null)
    val item: State<ContactModel?> = _item

    fun setItem(contact: ContactModel){
        _item.value = contact
    }

    private val _contactListLiveData = MutableLiveData<List<ContactModel>>()
    val contactListLiveData: LiveData<List<ContactModel>> get() = _contactListLiveData

     fun getContactList() {
        viewModelScope.launch {
            val cr: ContentResolver = getApplication<Application>().contentResolver
            val cursor: Cursor? = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val mobileNoSet = HashSet<String>()
                val contacts = mutableListOf<ContactModel>()
                val nameIndex: Int = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex: Int = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var name: String
                var number: String
                while (it.moveToNext()) {
                    name = it.getString(nameIndex)
                    number = it.getString(numberIndex)
                    number = number.replace(" ", "")
                    if (!mobileNoSet.contains(number)) {
                        contacts.add(ContactModel(name, number))
                        mobileNoSet.add(number)
                    }
                }
                _contactListLiveData.postValue(contacts)
            }
        }
    }
}
