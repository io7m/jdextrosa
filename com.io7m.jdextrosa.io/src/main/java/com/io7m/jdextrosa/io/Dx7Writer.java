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

package com.io7m.jdextrosa.io;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jdextrosa.core.Dx7Operator;
import com.io7m.jdextrosa.core.Dx7Voice;
import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import com.io7m.junreachable.UnreachableCodeException;
import io.vavr.collection.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

import static com.io7m.jdextrosa.core.Dx7OperatorType.LevelScalingCurve;

final class Dx7Writer implements Dx7SysExWriterType
{
  private static final Logger LOG = LoggerFactory.getLogger(Dx7Writer.class);

  private final URI uri;
  private final OutputStream stream;

  Dx7Writer(
    final URI in_uri,
    final OutputStream in_stream)
  {
    this.uri = Objects.requireNonNull(in_uri, "URI");
    this.stream = Objects.requireNonNull(in_stream, "Stream");
  }

  private static int packLevelCurves(
    final LevelScalingCurve left,
    final LevelScalingCurve right)
  {
    return (left.external() << 2) | (right.external() & 0b11);
  }

  @Override
  public void write(
    final Vector<Dx7VoiceNamed> voices)
    throws IOException
  {
    LOG.debug("writing {} voices", Integer.valueOf(voices.size()));

    this.stream.write(0xf0);
    this.stream.write(0x43);
    this.stream.write(0x00);
    this.stream.write(0x09);

    final int count = voices.size();
    final int size = count * 128;

    this.stream.write((size >> 7) & 0b0111_1111);
    this.stream.write(size & 0b0111_1111);

    int checksum = 0;
    final byte[] buffer = new byte[128];
    for (final Dx7VoiceNamed voice : voices) {
      this.packVoice(voice, buffer);
      checksum = Dx7Checksum.checksumAdd(checksum, buffer);
      this.stream.write(buffer);
    }

    this.stream.write(Dx7Checksum.checksumFinish(checksum));
    this.stream.write(0xf7);
  }

  private void packVoice(
    final Dx7VoiceNamed voice_named,
    final byte[] buffer)
  {
    LOG.debug("write: voice {}", voice_named.name());

    int position = 0;
    final Dx7Voice voice = voice_named.voice();
    for (int op_index = 6; op_index >= 1; --op_index) {
      switch (op_index) {
        case 1:
          this.packOp(voice.operator1(), position, buffer);
          break;
        case 2:
          this.packOp(voice.operator2(), position, buffer);
          break;
        case 3:
          this.packOp(voice.operator3(), position, buffer);
          break;
        case 4:
          this.packOp(voice.operator4(), position, buffer);
          break;
        case 5:
          this.packOp(voice.operator5(), position, buffer);
          break;
        case 6:
          this.packOp(voice.operator6(), position, buffer);
          break;
        default:
          throw new UnreachableCodeException();
      }
      position += 17;
    }

    Invariants.checkInvariantI(
      position,
      position == 102,
      x -> "Must have written 102 octets");

    buffer[102] = (byte) voice.pitchEnvelopeR1Rate();
    buffer[103] = (byte) voice.pitchEnvelopeR2Rate();
    buffer[104] = (byte) voice.pitchEnvelopeR3Rate();
    buffer[105] = (byte) voice.pitchEnvelopeR4Rate();
    buffer[106] = (byte) voice.pitchEnvelopeR1Level();
    buffer[107] = (byte) voice.pitchEnvelopeR2Level();
    buffer[108] = (byte) voice.pitchEnvelopeR3Level();
    buffer[109] = (byte) voice.pitchEnvelopeR4Level();
    buffer[110] = (byte) voice.algorithm().external();
    buffer[111] = (byte) (voice.feedback() | (voice.oscillatorKeySync() ? 1 : 0) << 3);
    buffer[112] = (byte) voice.lfoSpeed();
    buffer[113] = (byte) voice.lfoDelay();
    buffer[114] = (byte) voice.lfoPitchModulationDepth();
    buffer[115] = (byte) voice.lfoAmplitudeModulationDepth();
    buffer[116] = (byte)
      (voice.lfoPitchModulationSensitivityPacked() | voice.lfoWave().packed() | (voice.lfoKeySync() ? 1 : 0));
    buffer[117] = (byte) voice.transposeExternal();
    buffer[118] = (byte) voice_named.name().charAt(0);
    buffer[119] = (byte) voice_named.name().charAt(1);
    buffer[120] = (byte) voice_named.name().charAt(2);
    buffer[121] = (byte) voice_named.name().charAt(3);
    buffer[122] = (byte) voice_named.name().charAt(4);
    buffer[123] = (byte) voice_named.name().charAt(5);
    buffer[124] = (byte) voice_named.name().charAt(6);
    buffer[125] = (byte) voice_named.name().charAt(7);
    buffer[126] = (byte) voice_named.name().charAt(8);
    buffer[127] = (byte) voice_named.name().charAt(9);
  }

  private void packOp(
    final Dx7Operator op,
    final int position,
    final byte[] buffer)
  {
    buffer[position + 0] = (byte) (op.envelopeR1Rate());
    buffer[position + 1] = (byte) (op.envelopeR2Rate());
    buffer[position + 2] = (byte) (op.envelopeR3Rate());
    buffer[position + 3] = (byte) (op.envelopeR4Rate());
    buffer[position + 4] = (byte) (op.envelopeR1Level());
    buffer[position + 5] = (byte) (op.envelopeR2Level());
    buffer[position + 6] = (byte) (op.envelopeR3Level());
    buffer[position + 7] = (byte) (op.envelopeR4Level());
    buffer[position + 8] = (byte) (op.levelScalingBreakpoint());
    buffer[position + 9] = (byte) (op.levelScalingLeftDepth());
    buffer[position + 10] = (byte) (op.levelScalingRightDepth());
    buffer[position + 11] =
      (byte) (packLevelCurves(
        op.levelScalingLeftCurve(),
        op.levelScalingRightCurve()));

    buffer[position + 12] = (byte) (op.oscillatorFrequencyDetunePacked() | op.rateScaling());
    buffer[position + 13] = (byte) (op.velocitySensitivityPacked() | op.lfoAmplitudeModulationSensitivity());
    buffer[position + 14] = (byte) (op.outputLevel());
    buffer[position + 15] = (byte) (op.oscillatorFrequencyCoarsePacked() | op.oscillatorMode().external());
    buffer[position + 16] = (byte) (op.oscillatorFrequencyFine());
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.flush();
    this.stream.close();
  }
}
