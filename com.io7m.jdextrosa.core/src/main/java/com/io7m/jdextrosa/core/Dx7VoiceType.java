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

package com.io7m.jdextrosa.core;

import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeInclusiveI;
import org.immutables.value.Value;

@DxImmutableStyleType
@Value.Immutable
public interface Dx7VoiceType
{
  Dx7Operator operator1();

  Dx7Operator operator2();

  Dx7Operator operator3();

  Dx7Operator operator4();

  Dx7Operator operator5();

  Dx7Operator operator6();

  @Value.Default
  default Dx7AlgorithmID algorithm()
  {
    return Dx7AlgorithmID.of(1);
  }

  default int algorithmExternal()
  {
    return this.algorithm().id() - 1;
  }

  @Value.Default
  default int feedback()
  {
    return 0;
  }

  @Value.Default
  default int transpose()
  {
    return 0;
  }

  default int transposeExternal()
  {
    return this.transpose() + 24;
  }

  @Value.Default
  default int pitchEnvelopeR1Rate()
  {
    return 99;
  }

  @Value.Default
  default int pitchEnvelopeR2Rate()
  {
    return 99;
  }

  @Value.Default
  default int pitchEnvelopeR3Rate()
  {
    return 99;
  }

  @Value.Default
  default int pitchEnvelopeR4Rate()
  {
    return 99;
  }

  @Value.Default
  default int pitchEnvelopeR1Level()
  {
    return 50;
  }

  @Value.Default
  default int pitchEnvelopeR2Level()
  {
    return 50;
  }

  @Value.Default
  default int pitchEnvelopeR3Level()
  {
    return 50;
  }

  @Value.Default
  default int pitchEnvelopeR4Level()
  {
    return 50;
  }

  @Value.Default
  default int lfoPitchModulationDepth()
  {
    return 0;
  }

  @Value.Default
  default int lfoPitchModulationSensitivity()
  {
    return 0;
  }

  @Value.Default
  default int lfoAmplitudeModulationDepth()
  {
    return 0;
  }

  @Value.Default
  default int lfoSpeed()
  {
    return 0;
  }

  @Value.Default
  default int lfoDelay()
  {
    return 0;
  }

  @Value.Default
  default LFOWave lfoWave() { return LFOWave.LFO_TRIANGLE; }

  @Value.Default
  default boolean lfoKeySync() { return true; }

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.feedback(),
      "Feedback",
      RangeInclusiveI.of(0, 7),
      "Valid feedback values");

    RangeCheck.checkIncludedInInteger(
      this.transpose(),
      "Transpose",
      RangeInclusiveI.of(-24, 24),
      "Valid transpose values");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR1Level(),
      "Pitch R1 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR1Rate(),
      "Pitch R1 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR2Level(),
      "Pitch R2 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR2Rate(),
      "Pitch R2 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR3Level(),
      "Pitch R3 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR3Rate(),
      "Pitch R3 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR4Level(),
      "Pitch R4 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.pitchEnvelopeR4Rate(),
      "Pitch R4 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.lfoPitchModulationDepth(),
      "LFO Pitch Modulation Depth",
      RangeInclusiveI.of(0, 99),
      "Valid pitch modulation depth values");

    RangeCheck.checkIncludedInInteger(
      this.lfoPitchModulationSensitivity(),
      "LFO Pitch Modulation Sensitivity",
      RangeInclusiveI.of(0, 7),
      "Valid pitch modulation sensitivity values");

    RangeCheck.checkIncludedInInteger(
      this.lfoAmplitudeModulationDepth(),
      "LFO Amplitude Modulation Depth",
      RangeInclusiveI.of(0, 99),
      "Valid amplitude modulation depth values");

    RangeCheck.checkIncludedInInteger(
      this.lfoSpeed(),
      "LFO Speed",
      RangeInclusiveI.of(0, 99),
      "Valid LFO speed values");

    RangeCheck.checkIncludedInInteger(
      this.lfoDelay(),
      "LFO Delay",
      RangeInclusiveI.of(0, 99),
      "Valid LFO delay values");
  }

  enum LFOWave
  {
    LFO_TRIANGLE(0),
    LFO_SAW_DOWN(1),
    LFO_SAW_UP(2),
    LFO_SQUARE(3),
    LFO_SINE(4),
    LFO_SAMPLE_HOLD(5);

    private final int mode;

    LFOWave(
      final int m)
    {
      this.mode = m;
    }

    public static LFOWave ofInteger(
      final int value)
    {
      switch (value) {
        case 0: return LFO_TRIANGLE;
        case 1: return LFO_SAW_DOWN;
        case 2: return LFO_SAW_UP;
        case 3: return LFO_SQUARE;
        case 4: return LFO_SINE;
        case 5: return LFO_SAMPLE_HOLD;
        default: {
          throw new IllegalArgumentException("No LFO waveform for value: " + value);
        }
      }
    }

    public int external()
    {
      return this.mode;
    }
  }
}
