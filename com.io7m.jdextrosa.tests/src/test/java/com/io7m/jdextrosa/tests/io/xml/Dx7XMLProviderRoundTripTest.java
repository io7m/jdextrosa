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

package com.io7m.jdextrosa.tests.io.xml;

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.xml.Dx7ParserConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7WriterConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7XMLParserType;
import com.io7m.jdextrosa.io.xml.Dx7XMLParsers;
import com.io7m.jdextrosa.io.xml.Dx7XMLWriters;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterType;
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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@ExtendWith(TestMemoryFilesystemExtension.class)
public final class Dx7XMLProviderRoundTripTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7XMLProviderRoundTripTest.class);

  private static InputStream resource(
    final String file)
    throws IOException
  {
    final String path = "/com/io7m/jdextrosa/tests/" + file;
    final InputStream stream =
      Dx7XMLProviderRoundTripTest.class.getResourceAsStream(path);
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
    copyResourceToMemoryFS(fs, "textures.xml");

    final Dx7XMLParsers parsers = new Dx7XMLParsers();
    final Dx7XMLWriters writers = new Dx7XMLWriters();

    final Path root = fs.getPath("/");
    final Path path0 = fs.getPath("/textures.xml");

    final Vector<Dx7VoiceNamed> result0 =
      this.parse(parsers, root, path0);

    final Path path1 = Files.createTempFile("dx7-sysex-", ".xml");
    LOG.debug("path: {}", path1);
    this.write(writers, result0, path1);

    final Vector<Dx7VoiceNamed> result1 = this.parse(parsers, root, path1);
    Assertions.assertEquals(stripMetadata(result0), stripMetadata(result1));
  }

  private static Vector<Dx7VoiceNamed> stripMetadata(
    final Vector<Dx7VoiceNamed> voices)
  {
    return voices.map(
      voice -> Dx7VoiceNamed.of(voice.name(), voice.voice(), Optional.empty()));
  }

  private void write(
    final Dx7XMLWriters writers,
    final Vector<Dx7VoiceNamed> result0,
    final Path path1)
    throws IOException, Dx7WriterConfigurationException
  {
    try (OutputStream stream = Files.newOutputStream(path1)) {
      try (Dx7XMLWriterType p = writers.createWriter(
        Dx7XMLWriterRequest.of(
          URI.create("schema:com.io7m.jdextrosa:1.0"),
          path1.toUri(),
          stream))) {
        p.start();
        p.write(result0);
        p.finish();
      }
      stream.flush();
    }
  }

  private Vector<Dx7VoiceNamed> parse(
    final Dx7XMLParsers parsers,
    final Path root,
    final Path path0)
    throws IOException, Dx7ParserConfigurationException
  {
    final Vector<Dx7VoiceNamed> result0;
    try (InputStream stream = Files.newInputStream(path0)) {
      final Dx7XMLParserType p = parsers.create(
        Dx7XMLParserRequest.of(Optional.of(root), path0.toUri(), stream));
      final Validation<Seq<Dx7XMLParseError>, Vector<Dx7VoiceNamed>> r = p.parse();

      this.dump(r);
      Assertions.assertTrue(r.isValid());
      result0 = r.get();
    }
    return result0;
  }

  private void dump(
    final Validation<Seq<Dx7XMLParseError>, Vector<Dx7VoiceNamed>> r)
  {
    if (r.isValid()) {
      LOG.debug("result: {}", r.get());
    } else {
      r.getError().forEach(e -> LOG.debug("result: {}", e));
    }
  }
}
