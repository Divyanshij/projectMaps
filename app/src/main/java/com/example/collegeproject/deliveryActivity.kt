package com.example.collegeproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.collegeproject.ui.theme.CollegeprojectTheme

class deliveryActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var petrolButton: Button
    private lateinit var mechanicButton: Button
    private lateinit var dieselButton: Button
    private lateinit var vehicleNameEditText: EditText
    private lateinit var quantitySpinner: Spinner
    private lateinit var orderNowButton: Button
    private lateinit var dbHelper: orderDatabaseHelper
    private var selectedService: String = ""
    private var selectedButton: Button? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delivery)

        // Initialize views
            petrolButton = findViewById(R.id.petrolButton)
            mechanicButton = findViewById(R.id.mechanicButton)
            dieselButton = findViewById(R.id.dieselButton)
            vehicleNameEditText = findViewById(R.id.vehicleNameEditText)
            quantitySpinner = findViewById(R.id.quantity_spinner)
            orderNowButton = findViewById(R.id.orderButton)

            dbHelper = orderDatabaseHelper(this)
            // Setup service buttons
            petrolButton.setOnClickListener { selectService("Petrol",petrolButton) }
            mechanicButton.setOnClickListener { selectService("Mechanic",mechanicButton) }
            dieselButton.setOnClickListener { selectService("Diesel",dieselButton) }

            // Setup quantity spinner
            val quantities = arrayOf("1 Liter", "5 Liters", "10 Liters", "20 Liters")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, quantities)
            quantitySpinner.adapter = adapter

            // Order now button click listener
            orderNowButton.setOnClickListener {
                placeOrder()
            }
        }

        private fun selectService(service: String , button: Button) {
            selectedService = service
            selectedButton?.setBackgroundColor(ContextCompat.getColor(this, R.color.defaultColor))

            // Set the new selected button's color
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.hoverColor))

            // Update selected service and selected button reference
            selectedService = service
            selectedButton = button
            Toast.makeText(this, "$service selected", Toast.LENGTH_SHORT).show()
        }

        private fun placeOrder() {
            val vehicleName = vehicleNameEditText.text.toString()
            val quantity = quantitySpinner.selectedItem.toString()

            if (selectedService.isEmpty()) {
                Toast.makeText(this, "Please select a service", Toast.LENGTH_SHORT).show()
                return
            }

            if (vehicleName.isEmpty()) {
                Toast.makeText(this, "Please enter your vehicle name", Toast.LENGTH_SHORT).show()
                return
            }

            val orderId = dbHelper.insertOrder(selectedService, vehicleName, quantity)

            if (orderId != -1L) {
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
            }

            val orderDetails = """
            Service: $selectedService
            Vehicle: $vehicleName
            Quantity: $quantity
        """.trimIndent()

            Toast.makeText(this, "Order Placed: \n$orderDetails", Toast.LENGTH_LONG).show()

            val spinner: Spinner = findViewById(R.id.quantity_spinner)
            ArrayAdapter.createFromResource(
                this,
                R.array.quantity_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            val intent = intent
            val stationName = intent.getStringExtra("station_name")


            // Get the TextViews from your layout
            val petrolNameTextView: TextView = findViewById(R.id.petrolPumpName)


            // Update the TextViews with the petrol pump name and location
            petrolNameTextView.text = stationName
            // You can now send this order to the backend or process it further
        }
    }

