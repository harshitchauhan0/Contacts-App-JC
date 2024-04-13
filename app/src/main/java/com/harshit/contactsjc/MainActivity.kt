package com.harshit.contactsjc

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.FloatingActionButton

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.harshit.contactsjc.ui.theme.ContactsJCTheme

class MainActivity : ComponentActivity() {
    private val contactViewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkContactPermission()
        setContent {
            ContactsJCTheme {
                val contactList = remember { mutableStateListOf<ContactModel>() }
                contactViewModel.contactListLiveData.observe(this) {
                    contactList.clear()
                    contactList.addAll(it)
                }
                ContactList(contactList,contactViewModel.item.value){
                    contactViewModel.setItem(it)
                }
            }
        }
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getContacts()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    private fun getContacts() {
        contactViewModel.getContactList()
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            contactViewModel.getContactList()
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    ContactsJCTheme {
        val contactList = remember { mutableStateListOf<ContactModel>() }
        ContactList(contactList,null){

        }
    }
}

@Composable
fun ContactList(contactList: SnapshotStateList<ContactModel>,item: ContactModel?,onClickItem:(model: ContactModel)->Unit) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = "My App") }
            )
        }
        , floatingActionButton = {
            FloatingActionButton(onClick = {
                addNewContact()
            }) {
                Image(painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add Button")
            }
        }
    ){
        LazyColumn(modifier = Modifier.padding(paddingValues = it)) {
            items(contactList) { model ->
                ContactItem(model,model == item) {
                    onClickItem(model)
                }
            }
        }
    }
}

private fun addNewContact(){
//     Work to do
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