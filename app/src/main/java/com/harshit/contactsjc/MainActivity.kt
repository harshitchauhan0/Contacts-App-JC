package com.harshit.contactsjc

import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.RemoteException
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.RawContacts
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.harshit.contactsjc.ui.theme.ContactsJCTheme

class MainActivity : ComponentActivity() {
    private val contactViewModel: ContactViewModel by viewModels()

    companion object{
        private const val TAG = "TAG"
    }

    private val requestWriteContactPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){}

    private fun checkWriteContactPermission() {
        if(ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            requestWriteContactPermission.launch(android.Manifest.permission.WRITE_CONTACTS)
        }
    }

    private fun addContact(name: String,phoneNumber: String){
        val ops = ArrayList<ContentProviderOperation>()
        val rawContactInsertIndex = ops.size

        ops.add(
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null).build()
        )
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, name)
                .build()
        )
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex
                )
                .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, phoneNumber)
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()
        )

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            contactViewModel.getContacts()
        } catch (e: RemoteException) {
            Log.v(TAG, "${e.message}")
        } catch (e: OperationApplicationException) {
            Log.v(TAG, "${e.message}")
        }
    }

    private fun checkReadContactPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestReadContactPermission.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }
    override fun onResume() {
        super.onResume()
        contactViewModel.getContacts()
    }

    private lateinit var expand: MutableState<Boolean>
    private val requestReadContactPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            contactViewModel.getContacts()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkReadContactPermission()
        checkWriteContactPermission()
        setContent {
            ContactsJCTheme {
                expand = remember {
                    mutableStateOf(false)
                }
                val contactList = remember { mutableStateListOf<ContactModel>() }
                contactViewModel.contactListLiveData.observe(this) {
                    contactList.clear()
                    contactList.addAll(it)
                }
                contactViewModel.expand.observe(this){
                    expand.value = it
                }
                ContactList(expand,contactList,contactViewModel.item.value,
                    onClickItem = {
                        contactViewModel.setItem(it)
                    },
                    onAddContact = {
                        addContact(name = "",phoneNumber = "")
                    },
                    onExpandChange = {
                        contactViewModel.setExpand(it)
                    }
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (expand.value) {
                    expand.value = false
                } else {
                    remove()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    ContactsJCTheme {
        AddContactView(onExpandChange = {})
    }
}

@Composable
fun AddContactView(onExpandChange: (flag: Boolean) -> Unit) {
    Box (
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ){
        Card(modifier = Modifier
            .fillMaxWidth()
            ,shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 6.dp)) {
                    Text(text = "Add Contact",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextField(value = "", onValueChange = {},
                    label = {
                            Text(text = "Enter Name")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 10.dp)
                )
                TextField(value = "", onValueChange = {},
                    label = {
                        Text(text = "Enter Phone Number")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 10.dp)
                )
                Box(modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd){
                    Button(onClick = {
                        onExpandChange(false)
                    }, colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
                        Text(text = "Save")
                    }
                }
            }
        }
    }

}

@Composable
fun ContactList(expand: MutableState<Boolean>,contactList: SnapshotStateList<ContactModel>,item: ContactModel?,onClickItem:(model: ContactModel)->Unit,onAddContact:()->Unit,onExpandChange:(flag: Boolean)->Unit) {
    val scrollState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scrollState,
        topBar = {
            TopAppBar(
                title = { Text(text = "My App") }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !expand.value) {
                FloatingActionButton(onClick = {
                    onExpandChange(true)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Add Button"
                    )
                }
            }
        }, modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
        ) {
            items(contactList) { model ->
                ContactItem(model, model == item) {
                    onClickItem(model)
                }
            }
        }
        AnimatedVisibility(
            visible = expand.value,
            enter = slideInVertically (
                initialOffsetY = {fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 600)
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 1500),
                targetOffsetY = { fullHeight -> fullHeight },
            )
        ) {
            AddContactView(onExpandChange)
        }
    }
}

@Composable
fun ContactItem(model: ContactModel,isSelected: Boolean,onClick: ()->Unit) {
    val color = if(!isSelected){Color.LightGray}else{Color.Gray}
    val textStyle = if (isSelected) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .background(color)
                .fillMaxSize()
                .padding(16.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = model.name, style = textStyle)
            Text(text = model.phoneNumber)
        }
    }
}