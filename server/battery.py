import smbus

def _safe_call(method):
    def wrapper(self, *args, **kwargs):
        try:
            return method(self, *args, **kwargs)
        except Exception:
            self._try_init()
            return None
    return wrapper

class BatterySensor:
    _REG_CONFIG = 0x00
    _REG_BUSVOLTAGE = 0x02
    _REG_CURRENT = 0x04
    _REG_CALIBRATION = 0x05
    _RANGE_32V = 0x01
    _DIV_8_320MV = 0x03
    _ADCRES_12BIT_32S = 0x0D
    _SANDBVOLT_CONTINUOUS_MODE = 0x07
    _I2C_ADDRESS = 0x41
    _CURRENT_LSB = 0.1
    _I2C_BUS = 0x1

    def __init__(self):
        self._try_init()

    def _try_init(self):
        self.available = True
        try:
            self.bus = smbus.SMBus(self._I2C_BUS)
            self._init_sensor()
        except Exception:
            self.available = False

    def _init_sensor(self):
        calibration_value = 4096
        self.bus.write_i2c_block_data(self._I2C_ADDRESS, self._REG_CALIBRATION, [(calibration_value >> 8) & 0xFF, calibration_value & 0xFF])
        config = (self._RANGE_32V << 13 | self._DIV_8_320MV << 11 | self._ADCRES_12BIT_32S << 7 | self._ADCRES_12BIT_32S << 3 | self._SANDBVOLT_CONTINUOUS_MODE)
        self.bus.write_i2c_block_data(self._I2C_ADDRESS, self._REG_CONFIG, [(config >> 8) & 0xFF, config & 0xFF])

    @_safe_call
    def get_voltage(self):
        raw = self.bus.read_i2c_block_data(self._I2C_ADDRESS, self._REG_BUSVOLTAGE, 2)
        voltage = (((raw[0] << 8) | raw[1]) >> 3) * 0.004
        return voltage

    @_safe_call
    def get_percentage(self, min_v=9.0, max_v=12.4):
        voltage = self.get_voltage()
        percent = (voltage - min_v) / (max_v - min_v) * 100
        return max(0, min(100, percent))

    @_safe_call
    def get_current(self):
        raw = self.bus.read_i2c_block_data(self._I2C_ADDRESS, self._REG_CURRENT, 2)
        value = (raw[0] << 8) | raw[1]
        if value > 32767:
            value -= 65535
        return value * self._CURRENT_LSB

    @_safe_call
    def is_charging(self, threshold=-5):
        current = self.get_current()
        return current > threshold
