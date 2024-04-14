package com.harshit.contactsjc

import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.harshit.contactsjc.ui.theme.ContactsJCTheme

class MainActivity : ComponentActivity() {
    private val contactViewModel: ContactViewModel by viewModels()
    private val requestWriteContactPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){}
    private fun checkWriteContactPermission() {
        if(ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestWriteContactPermission.launch(android.Manifest.permission.WRITE_CONTACTS)
        }
    }
    private fun addContact(name: String,phoneNumber: String){
        contactViewModel.saveContact(name,phoneNumber)
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
                    onAddContact = {name,phoneNumber->
                        addContact(name = name,phoneNumber = phoneNumber)
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
@Composable
fun AddContactView(onExpandChange: (flag: Boolean) -> Unit,onAddContact:(name: String,phoneNumber: String)->Unit) {
    var name by rememberSaveable {
        mutableStateOf("")
    }
    var phoneNumber by rememberSaveable {
        mutableStateOf("")
    }
    var phoneNumberCheck by rememberSaveable {
        mutableStateOf(false)
    }
    Box (
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ){
        Card(modifier = Modifier
            .fillMaxWidth()
            ,shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 6.dp)) {
                    Text(text = stringResource(id = R.string.add_button),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextField(value = name, onValueChange = {
                      name = it.trim()
                },
                    label = {
                            Text(text = stringResource(id = R.string.name_lable))
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 10.dp),
                    singleLine = true
                )
                TextField(value = phoneNumber, onValueChange = {
                     phoneNumber = it.trim()
                },
                    label = {
                        Text(text = stringResource(id = R.string.phone_label))
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 10.dp),
                    singleLine = true,
                    isError = phoneNumberCheck
                )
                Box(modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd){
                    Button(onClick = {
                        val phoneNumberLast = phoneNumber.trim().takeLast(10)
                        val digit = phoneNumberLast.all { it.isDigit() }
                        phoneNumberCheck = if(phoneNumberLast.length == 10 && digit){
                            onExpandChange(false)
                            onAddContact(name,phoneNumber)
                            false
                        } else{
                            true
                        }
                    }, colors = ButtonDefaults.buttonColors(Color.DarkGray)) {
                        Text(text = stringResource(id = R.string.save_button))
                    }
                }
            }
        }
    }

}

@Composable
fun ContactList(expand: MutableState<Boolean>,contactList: SnapshotStateList<ContactModel>,item: ContactModel?,onClickItem:(model: ContactModel)->Unit,onAddContact:(name: String,phoneNumber: String)->Unit,onExpandChange:(flag: Boolean)->Unit) {
    val scrollState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scrollState,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !expand.value) {
                FloatingActionButton(onClick = {
                    onExpandChange(true)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = stringResource(id = R.string.add_button)
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
            AddContactView(onExpandChange,onAddContact)
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