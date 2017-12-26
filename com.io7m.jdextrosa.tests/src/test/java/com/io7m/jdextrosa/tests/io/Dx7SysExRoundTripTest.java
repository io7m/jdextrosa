/*
 * Copyright © 2017 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jdextrosa.tests.io;

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.Dx7ParseErrorListenerType;
import com.io7m.jdextrosa.io.Dx7SysExIO;
import com.io7m.jdextrosa.io.Dx7SysExReaderType;
import com.io7m.jdextrosa.io.Dx7SysExWriterType;
import com.io7m.jdextrosa.io.xml.Dx7ParserConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7WriterConfigurationException;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.tests.TestMemoryFilesystemExtension;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@ExtendWith(TestMemoryFilesystemExtension.class)
public final class Dx7SysExRoundTripTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7SysExRoundTripTest.class);

  private static InputStream resource(
    final String file)
    throws IOException
  {
    final String path = "/com/io7m/jdextrosa/tests/" + file;
    final InputStream stream =
      Dx7SysExRoundTripTest.class.getResourceAsStream(path);
    if (stream == null) {
      throw new NoSuchFileException(path);
    }
    return stream;
  }

  private static Path copyResourceToMemoryFS(
    final FileSystem fs,
    final String file)
    throws IOException
  {
    final Path root = fs.getRootDirectories().iterator().next();
    final Path memfs_path;
    try (final InputStream stream = resource(file)) {
      memfs_path = root.resolve(file);
      try (OutputStream output = Files.newOutputStream(memfs_path)) {
        stream.transferTo(output);
      }
    }
    return memfs_path;
  }

  @Test
  public void testRoundTrip(
    final FileSystem fs)
    throws Exception
  {
    copyResourceToMemoryFS(fs, "TEXTURES.SYX");

    final Path path0 = fs.getPath("/TEXTURES.SYX");

    final Dx7ParseErrorListenerType errors =
      error -> LOG.error("error: {}", error);

    final Vector<Dx7VoiceNamed> result0 = this.parse(errors, path0);

    final Path path1 =
      Files.createTempFile("dx7-sysex-", ".sysx");

    LOG.debug("path: {}", path1);
    this.write(result0, path1);

    final Vector<Dx7VoiceNamed> result1 = this.parse(errors, path1);
    final Vector<Dx7VoiceNamed> m0 = stripMetadata(result0);
    final Vector<Dx7VoiceNamed> m1 = stripMetadata(result1);

    for (int index = 0; index < m0.size(); ++index) {
      final Dx7VoiceNamed v0 = m0.get(index);
      final Dx7VoiceNamed v1 = m1.get(index);
      Assertions.assertEquals(v0, v1);
    }
  }

  private static Vector<Dx7VoiceNamed> stripMetadata(
    final Vector<Dx7VoiceNamed> voices)
  {
    return voices.map(
      voice -> Dx7VoiceNamed.of(voice.name(), voice.voice(), Optional.empty()));
  }

  private void write(
    final Vector<Dx7VoiceNamed> voices,
    final Path path)
    throws IOException, Dx7WriterConfigurationException
  {
    try (OutputStream stream = Files.newOutputStream(path)) {
      try (Dx7SysExWriterType p = Dx7SysExIO.createWriter(path.toUri(), stream)) {
        p.write(voices);
      }
      stream.flush();
    }
  }

  private Vector<Dx7VoiceNamed> parse(
    final Dx7ParseErrorListenerType errors,
    final Path path)
    throws IOException, Dx7ParserConfigurationException
  {
    try (InputStream stream = Files.newInputStream(path)) {
      final Dx7SysExReaderType p =
        Dx7SysExIO.createReader(errors, path.toUri(), stream);
      return p.parse();
    }
  }
}
