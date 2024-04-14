package com.harshit.contactsjc

import android.app.Application
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log

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

     fun saveContact(name: String,phoneNumber: String): Boolean {
         val ops = ArrayList<ContentProviderOperation>()
         val rawContactInsertIndex = ops.size
         ops.add(
             ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                 .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                 .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build()
         )
         ops.add(
             ContentProviderOperation
                 .newInsert(ContactsContract.Data.CONTENT_URI)
                 .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                 .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                 .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                 .build()
         )
         ops.add(
             ContentProviderOperation
                 .newInsert(ContactsContract.Data.CONTENT_URI)
                 .withValueBackReference(
                     ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex
                 )
                 .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                 .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                 .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()
         )

         try {
             application.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
             return true
         } catch (e: RemoteException) {
             Log.v("TAG", "${e.message}")
         } catch (e: OperationApplicationException) {
             Log.v("TAG", "${e.message}")
         }
         return false
     }
    companion object {
        private val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
    }
}
