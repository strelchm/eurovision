package ru.strelchm.gateway.service;

import org.springframework.stereotype.Service;

@Service
public class Crc16Coder {
  public int crc16(final byte[] buffer) {
    int crc = 0xFFFF;
    for (byte b : buffer) {
      crc = ((crc >>> 8) | (crc << 8)) & 0xffff;
      crc ^= (b & 0xff);//byte to int, trunc sign
      crc ^= ((crc & 0xff) >> 4);
      crc ^= (crc << 12) & 0xffff;
      crc ^= ((crc & 0xFF) << 5) & 0xffff;
    }
    crc &= 0xffff;
    return crc;

  }
}
