package com.example.myapplication.data


import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BleRepository(context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val _heartRate = MutableStateFlow<String?>(null)  // "72 bpm"
    val heartRate: StateFlow<String?> = _heartRate

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState

    private var currentGatt: BluetoothGatt? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!device.name.isNullOrBlank()) {
                println("Найдено устройство: ${device.name} - ${device.address}")

                val current = _devices.value.toMutableList()
                if (current.none { it.address == device.address }) {
                    current.add(device)
                    _devices.value = current.toList()
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            println("Сканирование провалилось: errorCode = $errorCode")
        }
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun startScan() {
        if (!adapter.isEnabled) return
        val scanner = adapter.bluetoothLeScanner ?: return

        _devices.value = emptyList()  // очищаем список

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(null, settings, scanCallback)
        _isScanning.value = true
    }

    fun stopScan() {
        adapter.bluetoothLeScanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    fun connect(device: BluetoothDevice) {
        stopScan()
        currentGatt = device.connectGatt(null, false, gattCallback)
        _connectionState.value = "Connecting"
    }

    fun disconnect() {
        currentGatt?.disconnect()
        currentGatt?.close()
        currentGatt = null
        _connectionState.value = "Disconnected"
        _heartRate.value = null
    }

    fun refreshData() {
        currentGatt?.let { gatt ->
            val service = gatt.getService(HEART_RATE_SERVICE_UUID)
            val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
            characteristic?.let {
                val success = gatt.readCharacteristic(it)
                println("Ручное обновление данных: $success")
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = "Connected"
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = "Disconnected"
                _heartRate.value = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            println("onServicesDiscovered: status = $status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                if (service == null) {
                    println("Heart Rate Service НЕ НАЙДЕН!")
                    return
                }

                val characteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
                if (characteristic == null) {
                    println("Heart Rate Measurement НЕ НАЙДЕНА!")
                    return
                }

                // Включаем уведомления
                gatt.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    println("Запись CCC-дескриптора отправлена")
                }
            } else {
                println("Обнаружение сервисов провалилось: status = $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            println("onCharacteristicRead: status = $status, uuid = ${characteristic.uuid}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                parseHeartRateData(characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            println("onCharacteristicChanged: uuid = ${characteristic.uuid}")
            parseHeartRateData(characteristic)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            println("onDescriptorWrite: status = $status, uuid = ${descriptor.uuid}")
        }

        private fun parseHeartRateData(characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value ?: return
            if (value.isEmpty()) return

            val flags = value[0].toInt()
            val is16Bit = (flags and 0x01) != 0
            
            val heartRateValue = if (is16Bit) {
                ((value[2].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
            } else {
                value[1].toInt() and 0xFF
            }

            val hrText = "$heartRateValue bpm"
            _heartRate.value = hrText
            println("Пульс прочитан: $hrText")
        }
    }

    companion object {
        private val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        private val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}