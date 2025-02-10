package com.example.androidwebget

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetWeb()
        }
    }

}

@Composable
fun GetWeb() {

    val context = LocalContext.current

    var url by remember { mutableStateOf(TextFieldValue("")) }
    var startNum by remember { mutableStateOf(TextFieldValue("")) }
    var endNum by remember { mutableStateOf(TextFieldValue("")) }
    var protocol by remember { mutableStateOf("Http://") }

    Surface {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Download Web")
            Spacer(
                modifier = Modifier.padding(10.dp)
            )
            TextField (
                value = url,
                onValueChange = { url = it},
                label = { Text("Enter URL")},
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            )

            Row (
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(100.dp)
            ) {
                TextField(
                    value = startNum,
                    onValueChange = { startNum = it },
                    label = { Text("Start") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = endNum,
                    onValueChange = { endNum = it },
                    label = { Text("End") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row (
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (protocol == "Http://"),
                    onClick = { protocol = "Http://" }
                )
                Text("Http")
            }

            Row (
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (protocol == "Https://"),
                    onClick = { protocol = "Https://" }
                )
                Text("Https")
            }

            Button(onClick = {
                if (url.text.isEmpty()) {
                    Toast.makeText(context, "Please enter URL", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val start = startNum.text.toIntOrNull()?:0
                val end = endNum.text.toIntOrNull()?:0
                // 启动Download
                val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>().setInputData(
                    workDataOf(
                        "URL" to url.text,
                        "START_NUM" to start,
                        "END_NUM" to end,
                        "PROTOCOL" to protocol
                    )
                ).build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }) {
                Text("Download")
            }
        }

    }
}

