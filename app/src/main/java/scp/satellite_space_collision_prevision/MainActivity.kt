package scp.satellite_space_collision_prevision


import android.R
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.opencsv.CSVReader
import com.satellite_space_collision_prevision.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import utilities.InfoPopupDialog
import utilities.SSHConnection
import utilities.callToServer
import utilities.callToServer.connection
import utilities.orbitsOfSomeSat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicReference


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var mainActivityObserver: MainActivityObserver? = null
    private lateinit var configurationButton: ImageButton
    private lateinit var checkCollisionButton: Button
    private lateinit var satellitesSpinner1: Spinner
    private lateinit var satellitesSpinner2: Spinner
    private lateinit var multOrbits25: Button
    private lateinit var multOrbits50: Button
    private lateinit var multOrbits75: Button
    private lateinit var multOrbits100: Button
    private lateinit var infoInLayout: TextView
    private lateinit var collisionInfo: TextView
    private val dataList: MutableList<String> = ArrayList()
    private val satData: MutableList<String> = ArrayList()
    private var selectedSatellite2: String? = null
    private lateinit var playButton: ImageButton
    private var isPlaying = false
    private lateinit var returnedData: String
    private lateinit var syncButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var LGStatus: TextView
    private lateinit var serverStatus: TextView
    private lateinit var info: ImageButton
    private val resultRef = AtomicReference("False")

    companion object {
        var isDataDownloaded = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        callToServer(applicationContext, resultRef)

        configurationButton = binding.configurationButton
        checkCollisionButton = binding.checkCollisionButton
        satellitesSpinner1 = binding.selectSat1
        satellitesSpinner2 = binding.selectSat2
        infoInLayout = binding.satInfo
        collisionInfo = binding.probabilityText
        playButton = binding.playButton
        multOrbits25 = binding.smallsatellites
        multOrbits50 = binding.halfSatellites
        multOrbits75 = binding.threeParts
        multOrbits100 = binding.allSatellites
        syncButton = binding.syncButton
        backButton = binding.backButton
        nextButton = binding.nextButton
        LGStatus = binding.LGStatus
        serverStatus = binding.statusServer
        info = binding.info
        val satelliteData = readSatelliteDataName(applicationContext, "satellite_data.csv")
        val spinnerAdapter =
            ArrayAdapter(applicationContext, R.layout.simple_spinner_item, satelliteData)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        if (!isDataDownloaded) {
            syncButton
            LGStatus.text = "Status: Disconnected"
            LGStatus.setTextColor(resources.getColor(R.color.holo_red_light))
            serverStatus.text = "Status: Disconnected"
            serverStatus.setTextColor(resources.getColor(R.color.holo_red_light))
        }

        if (SSHConnection.isConnected()) {
            LGStatus.text = "Status: Connected"
            LGStatus.setTextColor(resources.getColor(R.color.holo_green_light))
        } else {
            LGStatus.text = "Status: Disconnected"
            LGStatus.setTextColor(resources.getColor(R.color.holo_red_light))
        }

        if (!Configuration.serverConnection) {
            serverStatus.text = "Status: Disconnected"
            serverStatus.setTextColor(resources.getColor(R.color.holo_red_light))
        } else {
            serverStatus.text = "Status: Connected"
            serverStatus.setTextColor(resources.getColor(R.color.holo_green_light))
        }
        satellitesSpinner1.adapter = spinnerAdapter
        satellitesSpinner2.adapter = spinnerAdapter
        configurationButton.setOnClickListener { onConfigurationButtonClicked() }
        checkCollisionButton.setOnClickListener { checkCollisionButtonClicked() }
        playButton.setOnClickListener { onPlayButtonClicked() }
        multOrbits25.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                someSats(99.8)
            }
        }
        multOrbits50.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch { someSats(99.6) }
        }
        multOrbits75.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                someSats(99.4)
            }
        }
        multOrbits100.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                someSats(99.2)
            }
        }
        syncButton.setOnClickListener { sync() }
        backButton.setOnClickListener { onClickBack() }
        nextButton.setOnClickListener { onClickNext() }
        info.setOnClickListener{val infoPopup = InfoPopupDialog()
            infoPopup.show(supportFragmentManager, "InfoPopupDialog")}


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

    private fun sync() {
        CoroutineScope(Dispatchers.Default).launch {
            downloadSatelliteData(applicationContext);
        }
        isDataDownloaded = true
    }

    private fun onClickBack() {
        utilities.backButton()

    }

    private fun onClickNext() {
        utilities.nextButton()
    }

    private fun onPlayButtonClicked() {
        if (!isPlaying) {
            SSHConnection.tour()
            playButton.setImageResource(R.drawable.ic_media_pause)
            isPlaying = true
        } else {
            SSHConnection.stopTour()
            playButton.setImageResource(R.drawable.ic_media_play)
            isPlaying = false
        }
    }


    private fun onConfigurationButtonClicked() {
        val intent = Intent(this, Configuration::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkCollisionButtonClicked() {
        readAllLineSat()
        infoInLayout.text = dataList.toString().replace(",", "").replace("[", "").replace("]", "")
        if (connection()) {
            returnedData = callToServer.sendPostRequest(
                satData.toString().replace(",", "").replace("[", "").replace("]", "")
            )
        } else {
            returnedData = "Server is not connected"
        }

        collisionInfo.text = "Collision: $returnedData"
        if (SSHConnection.isConnected()) {
            SSHConnection.printSatInfo(dataList.toString(), returnedData)
            SSHConnection.testingPrintingSats(
                satellitesSpinner1.selectedItem.toString(),
                satellitesSpinner2.selectedItem.toString()
            )
        }
    }

    private fun readAllLineSat() {
        val sat1 = satellitesSpinner1.selectedItem.toString()
        val sat2 = satellitesSpinner2.selectedItem.toString()
        val data: MutableList<String> = ArrayList()
        try {
            satData.clear()
            val fis = applicationContext.openFileInput("satellite_data.csv")
            val reader = CSVReader(InputStreamReader(fis))
            var nextLine: Array<String>?
            reader.readNext()
            while (reader.readNext().also { nextLine = it } != null) {
                if (nextLine?.isNotEmpty() == true) {
                    if (nextLine!![0] == sat1 || nextLine!![0] == sat2) {
                        data.add("Satellite name: " + nextLine!![0] + "\n")
                        data.add("   Satellite id: " + nextLine!![1] + "\n")
                        data.add("   Classification type: " + nextLine!![10] + "\n")
                        data.add("\n")
                        satData.add(nextLine!![5] + ",")
                        satData.add(nextLine!![4] + ",")
                        satData.add(nextLine!![14] + ",")
                        satData.add(nextLine!![3] + ",")
                        satData.add(nextLine!![8] + ",")
                        satData.add(nextLine!![6] + ",")
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


    private fun readSatelliteDataName(context: Context, fileName: String): List<String> {
        val data: MutableList<String> = ArrayList()
        try {
            val file = File(context.filesDir, fileName)
            val inputStream = InputStreamReader(FileInputStream(file))
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

    suspend fun downloadSatelliteData(context: Context) {
        val url = "https://celestrak.org/NORAD/elements/gp.php?GROUP=active&FORMAT=csv"
        val url2 = "https://celestrak.org/NORAD/elements/gp.php?GROUP=active&FORMAT=tle"
        val response = Jsoup.connect(url).timeout(100000).ignoreContentType(true).execute()
        val csvData = response.body()
        val response2 = Jsoup.connect(url2).timeout(100000).ignoreContentType(true).execute()
        val tleData = response2.body()

        val storageDir = context.filesDir
        if (storageDir != null && storageDir.exists()) {

            val csvFile = File(storageDir, "satellite_data.csv")
            val tleFile = File(storageDir, "tleSat.txt")

            withContext(Dispatchers.IO) {
                val outputStream = FileOutputStream(csvFile)
                val outputStream2 = FileOutputStream(tleFile)
                outputStream.write(csvData.toByteArray())
                outputStream2.write(tleData.toByteArray())
                outputStream.close()
                outputStream2.close()
            }
        }
        callToServer(applicationContext, resultRef)
    }

    private suspend fun someSats(percentage: Double) {
        if (SSHConnection.isConnected()) {
            SSHConnection.cleanBaloon()
            val fis = applicationContext.openFileInput("satellite_data.csv")
            val reader = CSVReader(InputStreamReader(fis))
            reader.readNext()
            val allRows = reader.readAll()
            val numRowsToRetrieve = allRows.size / percentage
            val selectedRows = allRows.take(numRowsToRetrieve.toInt())

            val satelliteNames = selectedRows.map { it[0] }.toTypedArray()
            if (SSHConnection.isConnected()) {
                orbitsOfSomeSat(satellites = satelliteNames, context = applicationContext)
            }
        }
    }
}