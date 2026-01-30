import smbus


class BatterySensor:
    _REG_CONFIG = 0x00
    _REG_BUSVOLTAGE = 0x02
    _REG_CURRENT = 0x04
    _REG_CALIBRATION = 0x05
    _RANGE_32V = 0x01
    _DIV_8_320MV = 0x03
    _ADCRES_12BIT_32S = 0x0D
    _SANDBVOLT_CONTINUOUS_MODE = 0x07

    def __init__(self, i2c_bus=1, address=0x41):
        self.bus = smbus.SMBus(i2c_bus)
        self.address = address
        self._current_lsb = 0.1
        self._init_sensor()

    def _init_sensor(self):
        calibration_value = 4096
        self.bus.write_i2c_block_data(self.address, self._REG_CALIBRATION, [(calibration_value >> 8) & 0xFF, calibration_value & 0xFF])
        config = (self._RANGE_32V << 13 | self._DIV_8_320MV << 11 | self._ADCRES_12BIT_32S << 7 | self._ADCRES_12BIT_32S << 3 | self._SANDBVOLT_CONTINUOUS_MODE)
        self.bus.write_i2c_block_data(self.address, self._REG_CONFIG, [(config >> 8) & 0xFF, config & 0xFF])

    def get_voltage(self):
        raw = self.bus.read_i2c_block_data(self.address, self._REG_BUSVOLTAGE, 2)
        voltage = (((raw[0] << 8) | raw[1]) >> 3) * 0.004
        return voltage

    def get_percentage(self, min_v=9.0, max_v=12.512):
        voltage = self.get_voltage()
        percent = (voltage - min_v) / (max_v - min_v) * 100
        return max(0, min(100, percent))

    def get_current(self):
        raw = self.bus.read_i2c_block_data(self.address, self._REG_CURRENT, 2)
        value = (raw[0] << 8) | raw[1]
        if value > 32767:
            value -= 65535
        return value * self._current_lsb

    def is_charging(self, threshold=-5):
        current = self.get_current()
        return current > threshold
