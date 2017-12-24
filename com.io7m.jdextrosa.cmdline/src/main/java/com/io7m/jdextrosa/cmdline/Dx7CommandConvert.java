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

package com.io7m.jdextrosa.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.jdextrosa.core.Dx7VoiceMetadata;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.Dx7SysExIO;
import com.io7m.jdextrosa.io.Dx7SysExReaderType;
import com.io7m.jdextrosa.io.xml.Dx7ParserConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7WriterConfigurationException;
import com.io7m.jdextrosa.io.xml.Dx7XMLParserType;
import com.io7m.jdextrosa.io.xml.Dx7XMLParsers;
import com.io7m.jdextrosa.io.xml.Dx7XMLWriters;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParseError;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLParserRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterRequest;
import com.io7m.jdextrosa.io.xml.spi.Dx7XMLWriterType;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Parameters(
  commandNames = "convert",
  commandDescription = "Convert the given file(s)")
final class Dx7CommandConvert extends Dx7CommandRoot
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Dx7CommandConvert.class);

  private final Dx7XMLParsers xml_parsers;
  private final Dx7XMLWriters xml_writers;

  @Parameter(
    names = "-file-input",
    required = true,
    description = "The input file")
  private Path file_input;

  @Parameter(
    names = "-format-input",
    required = false,
    description = "The input file format",
    converter = Dx7Format.Converter.class)
  private Dx7Format format_input;

  @Parameter(
    names = "-format-output",
    required = false,
    description = "The output file format",
    converter = Dx7Format.Converter.class)
  private Dx7Format format_output;

  @Parameter(
    names = "-file-output",
    required = true,
    description = "The output file")
  private Path file_output;

  Dx7CommandConvert()
  {
    this.xml_parsers = new Dx7XMLParsers();
    this.xml_writers = new Dx7XMLWriters();
  }

  private static Vector<Dx7VoiceNamed> updateMetadata(
    final Path file,
    final Vector<Dx7VoiceNamed> voices)
  {
    return voices.map(
      voice -> Dx7VoiceNamed.of(
        voice.name(),
        voice.voice(),
        Optional.of(
          Dx7VoiceMetadata.builder()
            .setSource(file.toUri())
            .setId(URI.create(file.toUri().toString() + "/" + makeNameSafe(voice)))
            .build())));
  }

  private static String makeNameSafe(
    final Dx7VoiceNamed voice)
  {
    try {
      return URLEncoder.encode(voice.name(), "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static Dx7Format inferFileFormat(
    final Path path,
    final Dx7Format format)
  {
    if (format != null) {
      return format;
    }

    final String path_upper = path.toString().toUpperCase();
    if (path_upper.endsWith(".SYSX")) {
      return Dx7Format.DX7_FORMAT_BINARY_SYSEX_32_VOICE;
    }
    if (path_upper.endsWith(".SYX")) {
      return Dx7Format.DX7_FORMAT_BINARY_SYSEX_32_VOICE;
    }
    if (path_upper.endsWith(".DX7")) {
      return Dx7Format.DX7_FORMAT_BINARY_SYSEX_32_VOICE;
    }
    if (path_upper.endsWith(".XML")) {
      return Dx7Format.DX7_FORMAT_XML;
    }

    throw new IllegalArgumentException(
      "Could not infer file format from file name: " + path);
  }

  @Override
  public Status execute()
    throws Exception
  {
    if (super.execute() == Status.FAILURE) {
      return Status.FAILURE;
    }

    final Path file_abs_input =
      this.file_input.toAbsolutePath();
    final Path file_abs_output =
      this.file_output.toAbsolutePath();

    this.format_input =
      inferFileFormat(file_abs_input, this.format_input);
    this.format_output =
      inferFileFormat(file_abs_output, this.format_output);

    final Vector<Dx7VoiceNamed> voices =
      updateMetadata(
        file_abs_input,
        this.parse(file_abs_input, this.format_input));

    this.write(voices, file_abs_output, this.format_output);
    return Status.SUCCESS;
  }

  private void write(
    final Vector<Dx7VoiceNamed> voices,
    final Path file,
    final Dx7Format format)
    throws IOException, Dx7WriterConfigurationException
  {
    switch (format) {
      case DX7_FORMAT_XML:
        this.writeXML(voices, file);
        break;
      case DX7_FORMAT_BINARY_SYSEX_32_VOICE:
        this.writeSysEx32(voices, file);
        break;
    }
  }

  private void writeSysEx32(
    final Vector<Dx7VoiceNamed> voices,
    final Path file)
  {
    throw new UnimplementedCodeException();
  }

  private void writeXML(
    final Vector<Dx7VoiceNamed> voices,
    final Path file)
    throws IOException, Dx7WriterConfigurationException
  {
    try (OutputStream stream = Files.newOutputStream(file)) {
      final Dx7XMLWriterRequest request =
        Dx7XMLWriterRequest.builder()
          .setFile(file.toUri())
          .setSchema(URI.create("schema:com.io7m.jdextrosa:1.0"))
          .setStream(stream)
          .build();

      try (Dx7XMLWriterType writer = this.xml_writers.createWriter(request)) {
        writer.start();
        writer.write(voices);
        writer.finish();
      }
    }
  }

  private Vector<Dx7VoiceNamed> parse(
    final Path file,
    final Dx7Format format)
    throws IOException, Dx7ParserConfigurationException
  {
    switch (format) {
      case DX7_FORMAT_XML: {
        return this.parseXML(file);
      }
      case DX7_FORMAT_BINARY_SYSEX_32_VOICE: {
        return this.parseSysEx32(file);
      }
    }
    throw new UnreachableCodeException();
  }

  private Vector<Dx7VoiceNamed> parseSysEx32(
    final Path file)
    throws IOException
  {
    try (InputStream stream = Files.newInputStream(file)) {
      final Dx7SysExReaderType reader =
        Dx7SysExIO.createReader(e -> {
          switch (e.severity()) {
            case WARNING: {
              LOG.warn("{}", e.show());
              break;
            }
            case ERROR: {
              LOG.error("{}", e.show());
              break;
            }
          }
        }, file.toUri(), stream);

      return reader.parse();
    }
  }

  private Vector<Dx7VoiceNamed> parseXML(
    final Path file)
    throws IOException, Dx7ParserConfigurationException
  {
    try (InputStream stream = Files.newInputStream(file)) {
      final Path parent = file.getParent();

      final Dx7XMLParserType parser =
        this.xml_parsers.create(Dx7XMLParserRequest.builder()
                                  .setBaseDirectory(parent)
                                  .setFile(file.toUri())
                                  .setStream(stream)
                                  .build());

      final Validation<Seq<Dx7XMLParseError>, Vector<Dx7VoiceNamed>> result =
        parser.parse();

      if (result.isValid()) {
        return result.get();
      }

      result.getError().forEach(e -> {
        switch (e.severity()) {
          case WARNING: {
            LOG.warn("{}", e.show());
            break;
          }
          case ERROR: {
            LOG.error("{}", e.show());
            break;
          }
        }
      });

      throw new IOException("At least one parse error occurred.");
    }
  }
}
