package com.example.satellite_space_collision_prevision


import android.R
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.satellite_space_collision_prevision.databinding.ActivityMainBinding
import com.opencsv.CSVReader
import utilities.SSHConnection
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mainActivityObserver: MainActivityObserver? = null
    private val viewModel: SplashScreen = SplashScreen()
    private lateinit var configurationButton: ImageButton
    private lateinit var checkCollisionButton: Button
    private lateinit var satellitesSpinner1: Spinner
    private lateinit var satellitesSpinner2: Spinner
    private lateinit var infoInLayout: TextView
    private lateinit var collisionInfo: TextView
    private val dataList: MutableList<String> = ArrayList()
    private var selectedSatellite2: String? = null
    private lateinit var playButton: ImageButton

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        configurationButton = binding.configurationButton
        checkCollisionButton = binding.checkCollisionButton
        satellitesSpinner1 = binding.selectSat1
        satellitesSpinner2 = binding.selectSat2
        infoInLayout = binding.satInfo
        collisionInfo = binding.probabilityText
        playButton = binding.playButton

        val satelliteData = readSatelliteDataName()
        val spinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, satelliteData)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        satellitesSpinner1.adapter = spinnerAdapter
        satellitesSpinner2.adapter = spinnerAdapter
        configurationButton.setOnClickListener { onConfigurationButtonClicked() }
        checkCollisionButton.setOnClickListener { checkCollisionButtonClicked() }
        playButton.setOnClickListener { onPlayButtonClicked() }

        satellitesSpinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedSatellite = satelliteData[position]
                val filteredData = satelliteData.toMutableList()
                filteredData.remove(selectedSatellite)

                // Guardar la selección actual del satellitesSpinner2
                selectedSatellite2 = satellitesSpinner2.selectedItem.toString()

                val spinner2Adapter =
                    ArrayAdapter(this@MainActivity, R.layout.simple_spinner_item, filteredData)
                spinner2Adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                satellitesSpinner2.adapter = spinner2Adapter

                // Restaurar la selección previa del satellitesSpinner2
                val index = filteredData.indexOf(selectedSatellite2)
                if (index != -1) {
                    satellitesSpinner2.setSelection(index)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }
        }

    }

    private fun onPlayButtonClicked() {
        SSHConnection.tour()
    }


    private fun onConfigurationButtonClicked() {
        val intent = Intent(this, Configuration::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkCollisionButtonClicked() {
        readAllLineSat()
        infoInLayout.text = dataList.toString()
//        val returnedData = callToServer.sendPostRequest()
//        collisionInfo.text = "Collision: $returnedData"
        if (SSHConnection.isConnected()) {
            SSHConnection.printSatInfo(dataList.toString(), "returnedData")
            SSHConnection.testingPrintingSats()
        }
    }

    private fun readAllLineSat() {
        val sat1 = satellitesSpinner1.selectedItem.toString()
        val sat2 = satellitesSpinner2.selectedItem.toString()
        val data: MutableList<String> = ArrayList()
        try {
            val inputStream = InputStreamReader(assets.open("tleSat.csv"))
            val reader = CSVReader(inputStream)
            var nextLine: Array<String>?
            reader.readNext()
            while (reader.readNext().also { nextLine = it } != null) {
                if (nextLine?.isNotEmpty() == true) {
                    if (nextLine!![0] == sat1 || nextLine!![0] == sat2) {
                        data.add(nextLine!![0])
                        data.add(nextLine!![1])
                        data.add(nextLine!![2])
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dataList.clear()
        dataList.addAll(data)
    }


    private fun readSatelliteDataName(): List<String> {
        val data: MutableList<String> = ArrayList()
        try {
            val inputStream = InputStreamReader(assets.open("tleSat.csv"))
            val reader = CSVReader(inputStream)
            var nextLine: Array<String>?
            reader.readNext()
            while (reader.readNext().also { nextLine = it } != null) {
                if (nextLine?.isNotEmpty() == true) {
                    data.add(nextLine!![0])
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        mainActivityObserver?.onMainActivityDestroyed()
    }


}