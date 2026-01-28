#include <Adafruit_MLX90640.h>
#include <ArduinoBLE.h>
#include <Wire.h>

#define TOTAL_SIZE 32 * 24
#define MAX_CHUNK_SIZE 116
#define SUCCESS 0

Adafruit_MLX90640 mlx;
BLEService cameraService("febf1d04-587e-444e-bcae-4569ce926715");
BLECharacteristic settingsChar("173f51fe-0ca9-4aac-873c-ac2811209099", BLEWrite | BLEWriteWithoutResponse, 2);
BLECharacteristic frameChar("705b66b4-8f43-440e-96fd-e071ab46d471", BLERead | BLENotify, MAX_CHUNK_SIZE * 2 + 2);

String white_list[] = { "34:C9:3D:01:4E:E6", "AA:AA:AA:AA:AA:AA" };
const int white_list_size = sizeof(white_list) / sizeof(white_list[0]);

uint16_t frame_bytes[TOTAL_SIZE];
float frame[TOTAL_SIZE];
uint8_t frame_idx = 0;

void setup() {
  Wire.begin(6, 5);
  Wire.setClock(800000);
  Serial.begin(115200);
  delay(1000);

  while (!mlx.begin(MLX90640_I2CADDR_DEFAULT, &Wire)) {
    Serial.println("MLX90640 not found!");
    delay(1000);
  }

  mlx.setMode(MLX90640_INTERLEAVED);
  mlx.setRefreshRate(MLX90640_8_HZ);
  mlx.setResolution(MLX90640_ADC_16BIT);

  while (!BLE.begin()) {
    Serial.println("BLE init failed!");
    delay(1000);
  }

  BLE.setAdvertisedService(cameraService);
  cameraService.addCharacteristic(frameChar);
  cameraService.addCharacteristic(settingsChar);
  BLE.addService(cameraService);
  BLE.advertise();

  Serial.println("BLE Camera Service Started");
}

void loop() {
  BLEDevice central = BLE.central();

  if (central) {
    Serial.print("Connected to central: ");
    Serial.println(central.address());

    bool allowed = false;
    for (int i = 0; i < white_list_size; i++)
      if (central.address().equalsIgnoreCase(white_list[i]))
        allowed = true;

    if (!allowed) {
      Serial.print("Unauthorized device...");
      central.disconnect();
    }

    while (central.connected()) {
      if (settingsChar.written()) {
        uint8_t settings[2];
        settingsChar.readValue(settings, 2);

        if (settings[0] <= 7)
          mlx.setRefreshRate((mlx90640_refreshrate_t)settings[0]);

        if (settings[1] <= 3)
          mlx.setResolution((mlx90640_resolution_t)settings[1]);

        Serial.printf("Updated settings: Refresh rate = %d, Resolution = %d\n", settings[0], settings[1]);
      }

      if (mlx.getFrame(frame) == SUCCESS) {
        for (int i = 0; i < TOTAL_SIZE; i++) {
          int32_t scaled = (int32_t)(frame[i] * 1000.0);
          frame_bytes[i] = (uint16_t)constrain(scaled, 0, UINT16_MAX);
        }

        for (int i = 0; i < TOTAL_SIZE; i += MAX_CHUNK_SIZE) {
          uint8_t buffer[MAX_CHUNK_SIZE * 2 + 2];
          buffer[0] = frame_idx;           // frame index
          buffer[1] = i / MAX_CHUNK_SIZE;  // chunk index

          int length = min(MAX_CHUNK_SIZE, TOTAL_SIZE - i);
          for (int j = 0; j < length; j++) {
            buffer[2 + j * 2] = frame_bytes[i + j] & 0xFF;      // low byte
            buffer[2 + j * 2 + 1] = frame_bytes[i + j] >> 8;    // high byte
          }

          frameChar.writeValue(buffer, length * 2 + 2);
        }

        frame_idx++;
      }
    }

    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}