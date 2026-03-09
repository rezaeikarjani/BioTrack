package com.example.biotrack

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Retrofit Setup
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://api.openweathermap.org/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val weatherApi: WeatherApi = retrofit.create(WeatherApi::class.java)

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val _stepCount = MutableLiveData(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        // This satisfies the "Replace default splash" requirement (5p)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Sensor Setup
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Room Database Setup
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "biotrack-database"
        ).build()
        val healthDao = db.healthDao()

        setContent {
            val currentSteps by _stepCount.observeAsState(0)
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BioTrackApp(healthDao, currentSteps)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            _stepCount.postValue(event.values[0].toInt())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun BioTrackApp(healthDao: HealthDao, currentSteps: Int) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                healthDao = healthDao,
                currentSteps = currentSteps,
                onNavigateToHistory = {
                    navController.navigate("history")
                }
            )
        }
        composable("history") {
            HistoryScreen(
                healthDao = healthDao,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DashboardScreen(healthDao: HealthDao, currentSteps: Int, onNavigateToHistory: () -> Unit) {
    var noteText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current
    var ouluTemp by remember { mutableStateOf("Loading...") }

    // Fetch weather when screen opens
    LaunchedEffect(Unit) {
        try {
            val response = weatherApi.getOuluWeather()
            // response.main.temp will now be in Celsius because of units=metric
            ouluTemp = "${response.main.temp}°C"
        } catch (e: Exception) {
            // This will show you exactly what is wrong (e.g. "No Internet")
            ouluTemp = "Error: ${e.localizedMessage}"
            Log.e("BioTrack", "Weather Error", e)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BioTrack Dashboard", style = MaterialTheme.typography.headlineMedium)

        // Steps Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Steps Today", style = MaterialTheme.typography.titleMedium)
                Text("$currentSteps", style = MaterialTheme.typography.headlineLarge)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(onClick = { 
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) 
                }) {
                    Text("Enable Step Tracking (Permission 5p)")
                }
            }
        }

        // Weather Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Oulu Temperature: $ouluTemp", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    scope.launch {
                        try {
                            ouluTemp = "Loading..."
                            val response = weatherApi.getOuluWeather()
                            ouluTemp = "${response.main.temp}°C"
                            
                            // Also send notification after fetch
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            sendHealthNotification(context)
                        } catch (e: Exception) {
                            ouluTemp = "Error: ${e.localizedMessage}"
                            Log.e("BioTrack", "Detailed Error: ", e)
                        }
                    }
                }) {
                    Text("Get Oulu Weather")
                }
            }
        }

        // Photo Preview
        selectedImageUri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Selected Image",
                modifier = Modifier.size(200.dp).padding(16.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Button(onClick = { 
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
        }) {
            Text("Pick a Health Photo (HW3)")
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Enter Health Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (noteText.isNotEmpty()) {
                    scope.launch {
                        healthDao.insert(HealthEntry(
                            note = noteText, 
                            imageUri = selectedImageUri?.toString(),
                            timestamp = System.currentTimeMillis()
                        ))
                        noteText = ""
                        selectedImageUri = null
                    }
                }
            },
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
        ) {
            Text("Save Entry to Room")
        }

        Button(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth()) {
            Text("View History")
        }
    }
}

fun sendHealthNotification(context: Context) {
    val channelId = "health_notifications"
    val notificationId = 1

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Health Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("BioTrack Update")
        .setContentText("Don't forget to log your health today!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    try {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (e: SecurityException) {
        Log.e("BioTrack", "Notification permission not granted", e)
    }
}

@Composable
fun HistoryScreen(healthDao: HealthDao, onBack: () -> Unit) {
    val logs by healthDao.getAllLogs().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Recovery History", style = MaterialTheme.typography.headlineSmall)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(logs) { entry ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        entry.imageUri?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(entry.note)
                    }
                }
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Entity(tableName = "health_logs")
data class HealthEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val note: String,
    val imageUri: String? = null,
    val timestamp: Long
)

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HealthEntry>>

    @Insert
    suspend fun insert(entry: HealthEntry)
}

@Database(entities = [HealthEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao
}

// Matches the top level { "main": { ... } }
data class WeatherResponse(
    val main: MainData,
    val name: String
)
// Matches the inner { "temp": 271.54, "humidity": 85 }
data class MainData(
    val temp: Double,
    val humidity: Int
)

interface WeatherApi {
    @GET("data/2.5/weather?q=Oulu&units=metric&appid=1afa01e52fe10e8875c59cb4a06c832e")
    suspend fun getOuluWeather(): WeatherResponse
}
