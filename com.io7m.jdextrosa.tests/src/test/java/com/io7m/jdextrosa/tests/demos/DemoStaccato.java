package com.io7m.jdextrosa.tests.demos;

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.jdextrosa.io.Dx7SysExIO;
import com.io7m.jdextrosa.io.Dx7SysExReaderType;
import com.io7m.jdextrosa.io.Dx7SysExWriterType;
import com.io7m.jdextrosa.transforms.Dx7Staccato;
import com.io7m.jdextrosa.transforms.Dx7StaccatoParameters;
import com.io7m.jdextrosa.transforms.Dx7StaccatoParametersType;
import io.vavr.collection.Vector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DemoStaccato
{
  private DemoStaccato()
  {

  }

  public static void main(
    final String[] args)
    throws IOException
  {
    final Path file_in =
      Paths.get(args[0]).toAbsolutePath();
    final Path file_out =
      Paths.get(args[1]).toAbsolutePath();

    try (InputStream stream_in = Files.newInputStream(file_in)) {
      try (OutputStream stream_out = Files.newOutputStream(file_out)) {
        final Dx7SysExReaderType reader =
          Dx7SysExIO.createReader(error -> {
          }, file_in.toUri(), stream_in);
        try (Dx7SysExWriterType writer = Dx7SysExIO.createWriter(file_out.toUri(), stream_out)) {
          final Vector<Dx7VoiceNamed> voices = reader.parse().map(voice -> {
            final Dx7Staccato staccato =
              Dx7Staccato.create(
                voice.voice(),
                Dx7StaccatoParameters.builder()
                  .setAffect(Dx7StaccatoParametersType.AffectOperators.AFFECT_CARRIERS)
                  .setModifyAttack(false)
                  .setModifyRelease(true)
                  .build());
            return voice.withVoice(staccato.apply());
          });

          writer.write(voices);
        }
      }
    }
  }
}
