package com.example.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

data class PermissionItemStatus(
  val permission: String,
  val title: String,
  val description: String,
  val ruleReference: String,
  val isGranted: Boolean
)

data class DeviceTelemetryState(
  val networkType: String = "Verificando...",
  val isVpnActive: Boolean = false,
  val isConnected: Boolean = false,
  val linkDownstreamKbps: Int = 0,
  val magneticFieldMicroTesla: Float = 0f,
  val magneticAnomalyDetected: Boolean = false,
  val ambientLightLux: Float = 0f,
  val accelerationMagnitude: Float = 9.8f,
  val shakeToCamouflageEnabled: Boolean = true,
  val batteryLevel: Int = 100,
  val batteryTempCelsius: Float = 25.0f,
  val isAdbEnabled: Boolean = false,
  val isDevOptionsEnabled: Boolean = false,
  val gpsCoordinates: String = "Sin lectura activa",
  val gpsAccuracyMeters: Float? = null,
  val lastUpdatedMillis: Long = System.currentTimeMillis()
)

class DeviceShieldManager(
  private val context: Context,
  private val onShakeCamouflageTriggered: () -> Unit
) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
  private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

  private val _telemetry = MutableStateFlow(DeviceTelemetryState())
  val telemetry: StateFlow<DeviceTelemetryState> = _telemetry.asStateFlow()

  private val _permissionsState = MutableStateFlow<List<PermissionItemStatus>>(emptyList())
  val permissionsState: StateFlow<List<PermissionItemStatus>> = _permissionsState.asStateFlow()

  private var lastShakeTimestamp = 0L

  init {
    refreshPermissionsStatus()
    refreshSystemAndNetworkTelemetry()
    startSensors()
  }

  fun startSensors() {
    try {
      accelerometer?.let {
        sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
      }
      magnetometer?.let {
        sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
      }
      lightSensor?.let {
        sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
      }
    } catch (_: Exception) {
    }
  }

  fun stopSensors() {
    try {
      sensorManager?.unregisterListener(this)
    } catch (_: Exception) {
    }
  }

  fun setShakeToCamouflage(enabled: Boolean) {
    _telemetry.value = _telemetry.value.copy(shakeToCamouflageEnabled = enabled)
  }

  fun getRequiredRuntimePermissions(): List<String> {
    val list = mutableListOf(
      Manifest.permission.RECORD_AUDIO,
      Manifest.permission.CAMERA,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      list.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      list.add(Manifest.permission.BLUETOOTH_SCAN)
      list.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    return list
  }

  fun refreshPermissionsStatus() {
    val items = mutableListOf<PermissionItemStatus>()

    items.add(
      PermissionItemStatus(
        permission = Manifest.permission.RECORD_AUDIO,
        title = "Micrófono y Voz en Vivo",
        description = "Reconocimiento de voz en tiempo real para pronunciación y conversación fluida.",
        ruleReference = "Práctica Oral",
        isGranted = isGranted(Manifest.permission.RECORD_AUDIO)
      )
    )

    items.add(
      PermissionItemStatus(
        permission = Manifest.permission.CAMERA,
        title = "Cámara y Escáner Visual IA",
        description = "Traducción instantánea de textos/menús y auditoría visual de documentos sospechosos.",
        ruleReference = "Reglas 1, 4 y 6",
        isGranted = isGranted(Manifest.permission.CAMERA)
      )
    )

    items.add(
      PermissionItemStatus(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        title = "Ubicación GPS de Resguardo",
        description = "Coordenadas de seguridad física y verificación anti-rastreo para personas protegidas.",
        ruleReference = "Reglas 1 y 2",
        isGranted = isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
      )
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      items.add(
        PermissionItemStatus(
          permission = Manifest.permission.POST_NOTIFICATIONS,
          title = "Alertas Privadas del Sistema",
          description = "Notificaciones discretas de estudio y avisos de seguridad Hyper Lux.",
          ruleReference = "Reglas 6 y 7",
          isGranted = isGranted(Manifest.permission.POST_NOTIFICATIONS)
        )
      )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      items.add(
        PermissionItemStatus(
          permission = Manifest.permission.BLUETOOTH_SCAN,
          title = "Escaneo Bluetooth Anti-Rastreo",
          description = "Detección de balizas cercanas, audífonos y periféricos desconocidos.",
          ruleReference = "Regla 1",
          isGranted = isGranted(Manifest.permission.BLUETOOTH_SCAN)
        )
      )
    }

    _permissionsState.value = items
  }

  private fun isGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
  }

  @SuppressLint("MissingPermission")
  fun refreshSystemAndNetworkTelemetry() {
    var netType = "Sin Conexión"
    var isVpn = false
    var connected = false
    var downSpeed = 0

    try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      val activeNet = cm?.activeNetwork
      val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null
      if (caps != null) {
        connected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        downSpeed = caps.linkDownstreamBandwidthKbps
        netType = when {
          caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Activo"
          caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Red Móvil Celular"
          caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet Segura"
          else -> "Red Activa"
        }
      }
    } catch (_: Exception) {
    }

    // Battery info
    var batteryPct = 100
    var batteryTemp = 26.0f
    try {
      val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
      if (intent != null) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
          batteryPct = (level * 100) / scale
        }
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 260)
        batteryTemp = tempTenths / 10f
      }
    } catch (_: Exception) {
    }

    // Developer / ADB check (Rule 1 anti-hacking advisory)
    var adbActive = false
    var devActive = false
    try {
      adbActive = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
      devActive = Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
    } catch (_: Exception) {
    }

    // GPS Location check if permission is granted
    var gpsString = _telemetry.value.gpsCoordinates
    var gpsAcc = _telemetry.value.gpsAccuracyMeters
    if (isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
      try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
          ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
          ?: lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        if (loc != null) {
          gpsString = String.format(java.util.Locale.US, "%.5f, %.5f", loc.latitude, loc.longitude)
          gpsAcc = loc.accuracy
        } else {
          gpsString = "Permiso activo (Esperando señal GPS)"
        }
      } catch (_: Exception) {
      }
    } else {
      gpsString = "Permiso de ubicación pendiente"
    }

    _telemetry.value = _telemetry.value.copy(
      networkType = netType,
      isVpnActive = isVpn,
      isConnected = connected,
      linkDownstreamKbps = downSpeed,
      batteryLevel = batteryPct,
      batteryTempCelsius = batteryTemp,
      isAdbEnabled = adbActive,
      isDevOptionsEnabled = devActive,
      gpsCoordinates = gpsString,
      gpsAccuracyMeters = gpsAcc,
      lastUpdatedMillis = System.currentTimeMillis()
    )
  }

  fun triggerDiscreetVibration() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        v?.vibrate(80)
      }
    } catch (_: Exception) {
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null) return
    when (event.sensor.type) {
      Sensor.TYPE_ACCELEROMETER -> {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        _telemetry.value = _telemetry.value.copy(accelerationMagnitude = magnitude)

        // Shake detection for Emergency Camouflage (Rule 7)
        if (_telemetry.value.shakeToCamouflageEnabled && magnitude > 22.5f) {
          val now = System.currentTimeMillis()
          if (now - lastShakeTimestamp > 1800L) {
            lastShakeTimestamp = now
            triggerDiscreetVibration()
            onShakeCamouflageTriggered()
          }
        }
      }

      Sensor.TYPE_MAGNETIC_FIELD -> {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val uT = sqrt(x * x + y * y + z * z)
        _telemetry.value = _telemetry.value.copy(
          magneticFieldMicroTesla = uT,
          magneticAnomalyDetected = uT > 85f
        )
      }

      Sensor.TYPE_LIGHT -> {
        val lux = event.values.firstOrNull() ?: 0f
        _telemetry.value = _telemetry.value.copy(ambientLightLux = lux)
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
