package com.io7m.jdextrosa.io;

import org.apache.commons.io.input.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

final class Dx7InputStream extends CountingInputStream
{
  Dx7InputStream(
    final InputStream stream)
  {
    super(Objects.requireNonNull(stream, "Stream"));
  }

  int readByte()
    throws IOException
  {
    final int x = this.read();
    if (x == -1) {
      throw new IOException(
        "Unexpected EOF at position 0x" +
          Long.toUnsignedString(this.getByteCount(), 16));
    }
    return x & 0xff;
  }
}
