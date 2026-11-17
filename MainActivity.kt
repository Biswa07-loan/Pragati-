package com.example.microfinancemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MicrofinanceApp() } }
    }
}

@Composable
fun MicrofinanceApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("microfinance_records", 0) }
    var customer by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var coApplicant by remember { mutableStateOf("") }
    var coPhone by remember { mutableStateOf("") }
    var cibilStatus by remember { mutableStateOf("Pending") }
    var cibilScore by remember { mutableStateOf("") }
    var finance by remember { mutableStateOf("") }
    var loan by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }
    var demand by remember { mutableStateOf("") }
    var demandDate by remember { mutableStateOf("") }
    var emi by remember { mutableStateOf("") }
    var outstanding by remember { mutableStateOf("") }
    var records by remember { mutableStateOf(loadRecords(prefs.getString("data", "[]") ?: "[]")) }
    var showRecords by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun saveRecord() {
        if (customer.isBlank() || phone.isBlank()) {
            message = "Customer name and phone are required."
            return
        }
        val obj = JSONObject().apply {
            put("customer", customer); put("phone", phone); put("address", address)
            put("coApplicant", coApplicant); put("coPhone", coPhone)
            put("cibilStatus", cibilStatus); put("cibilScore", cibilScore)
            put("finance", finance); put("loan", loan); put("percentage", percentage)
            put("demand", demand); put("demandDate", demandDate)
            put("emi", emi); put("outstanding", outstanding)
        }
        records = records + obj
        val array = JSONArray()
        records.forEach { array.put(it) }
        prefs.edit().putString("data", array.toString()).apply()
        message = "Customer record saved on this phone."
        customer = ""; phone = ""; address = ""; coApplicant = ""; coPhone = ""
        cibilStatus = "Pending"; cibilScore = ""; finance = ""; loan = ""
        percentage = ""; demand = ""; demandDate = ""; emi = ""; outstanding = ""
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Microfinance Manager", style = MaterialTheme.typography.headlineSmall)
        Text("Customer • Co-Applicant • Finance • EMI", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showRecords = false }) { Text("New Customer") }
            OutlinedButton(onClick = { showRecords = true }) { Text("All Records (${records.size})") }
        }
        Spacer(Modifier.height(8.dp))
        if (showRecords) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(records.reversed()) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.optString("customer"), style = MaterialTheme.typography.titleMedium)
                            Text("Phone: ${item.optString("phone")}  |  Co-applicant: ${item.optString("coApplicant")}")
                            Text("Finance: ${item.optString("finance")}  |  Loan: ${item.optString("loan")}")
                            Text("Demand: ${item.optString("demand")} (${item.optString("demandDate")})")
                            Text("EMI: ${item.optString("emi")}  |  Outstanding: ${item.optString("outstanding")}")
                            Text("CIBIL: ${item.optString("cibilStatus")}  Score: ${item.optString("cibilScore")}")
                        }
                    }
                }
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SectionTitle("Customer Details")
                Field("Customer Name *", customer) { customer = it }
                Field("Mobile Number *", phone) { phone = it }
                Field("Address", address) { address = it }
                SectionTitle("Co-Applicant Details")
                Field("Co-Applicant Name", coApplicant) { coApplicant = it }
                Field("Co-Applicant Mobile", coPhone) { coPhone = it }
                SectionTitle("CIBIL / Credit Check")
                Text("Check status")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Pending", "Checked", "Not Available").forEach {
                        FilterChip(selected = cibilStatus == it, onClick = { cibilStatus = it }, label = { Text(it) })
                    }
                }
                Field("CIBIL Score (if available)", cibilScore) { cibilScore = it }
                SectionTitle("Finance & Demand")
                Field("Finance Name", finance) { finance = it }
                Field("Loan Amount", loan) { loan = it }
                Field("Interest Percentage (%)", percentage) { percentage = it }
                Field("Demand Amount", demand) { demand = it }
                Field("Demand Date (DD-MM-YYYY)", demandDate) { demandDate = it }
                Field("EMI Amount", emi) { emi = it }
                Field("Outstanding Amount", outstanding) { outstanding = it }
                Spacer(Modifier.height(10.dp))
                Button(onClick = { saveRecord() }, modifier = Modifier.fillMaxWidth()) { Text("Save Customer") }
                if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Spacer(Modifier.height(12.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    HorizontalDivider()
}

@Composable
fun Field(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValue, label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        singleLine = true
    )
}

fun loadRecords(raw: String): List<JSONObject> {
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { arr.getJSONObject(it) }
    } catch (_: Exception) { emptyList() }
}
