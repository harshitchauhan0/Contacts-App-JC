package com.harshit.contactsjc

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract

class ContactRepository(private val application: Application) {

     fun getContacts(): List<ContactModel> {
        val cr: ContentResolver = application.contentResolver
        val cursor: Cursor? = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        val mobileNoSet = HashSet<String>()
        val contacts = mutableListOf<ContactModel>()
        cursor?.use {
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
        }
        return contacts
    }

    companion object {
        private val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
    }
}
