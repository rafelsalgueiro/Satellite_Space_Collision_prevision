package sscp.satellite_space_collision_prevision


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
import com.example.satellite_space_collision_prevision.databinding.ActivityMainBinding
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import utilities.SSHConnection
import utilities.callToServer
import utilities.orbitsOfSomeSat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicReference


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var mainActivityObserver: MainActivityObserver? = null
    lateinit var configurationButton: ImageButton
    lateinit var checkCollisionButton: Button
    lateinit var satellitesSpinner1: Spinner
    lateinit var satellitesSpinner2: Spinner
    lateinit var infoInLayout: TextView
    lateinit var collisionInfo: TextView
    val dataList: MutableList<String> = ArrayList()
    var selectedSatellite2: String? = null
    lateinit var playButton: ImageButton
    var isPlaying = false
    lateinit var returnedData: String
    private val resultRef = AtomicReference("False")
    private var isDataDownloaded = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        callToServer(applicationContext, resultRef)

//        if (!isDataDownloaded) {
//            CoroutineScope(Dispatchers.Default).launch {
//                downloadSatelliteData(applicationContext);
//            }
//            isDataDownloaded = true
//        }


        configurationButton = binding.configurationButton
        checkCollisionButton = binding.checkCollisionButton
        satellitesSpinner1 = binding.selectSat1
        satellitesSpinner2 = binding.selectSat2
        infoInLayout = binding.satInfo
        collisionInfo = binding.probabilityText
        playButton = binding.playButton
        val satelliteData = readSatelliteDataName(applicationContext, "satellite_data.csv")
        val spinnerAdapter =
            ArrayAdapter(applicationContext, R.layout.simple_spinner_item, satelliteData)
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
        returnedData = callToServer.sendPostRequest()
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
            val file = File(context.getExternalFilesDir(null), fileName)
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

    fun someSats(percentage: Int){
        val fis = applicationContext.openFileInput("satellite_data.csv")
        val reader = CSVReader(InputStreamReader(fis))
        reader.readNext() // Skip header

        val numRowsToRetrieve = reader.readAll().size / percentage
        val selectedRows = reader.readAll().take(numRowsToRetrieve)

        val satelliteNames = selectedRows.map { it[0] }.toTypedArray()
        if (SSHConnection.isConnected()){
            orbitsOfSomeSat(satelliteNames, applicationContext)
        }
    }
}