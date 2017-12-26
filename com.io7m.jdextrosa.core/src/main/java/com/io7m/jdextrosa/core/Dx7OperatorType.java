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
public interface Dx7OperatorType
{
  @Value.Parameter
  Dx7OperatorID id();

  @Value.Default
  default boolean isEnabled()
  {
    return true;
  }

  @Value.Default
  default int oscillatorFrequencyCoarse()
  {
    return 1;
  }

  @Value.Default
  default int oscillatorFrequencyCoarsePacked()
  {
    return this.oscillatorFrequencyCoarse() << 1;
  }

  @Value.Default
  default int oscillatorFrequencyFine()
  {
    return 0;
  }

  @Value.Default
  default int oscillatorFrequencyDetune()
  {
    return 0;
  }

  @Value.Derived
  default int oscillatorFrequencyDetuneExternalized()
  {
    return this.oscillatorFrequencyDetune() + 7;
  }

  @Value.Derived
  default int oscillatorFrequencyDetunePacked()
  {
    return this.oscillatorFrequencyDetuneExternalized() << 3;
  }

  @Value.Default
  default OscillatorMode oscillatorMode()
  {
    return OscillatorMode.RATIO;
  }

  @Value.Default
  default int outputLevel()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR1Level()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR1Rate()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR2Level()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR2Rate()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR3Level()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR3Rate()
  {
    return 99;
  }

  @Value.Default
  default int envelopeR4Level()
  {
    return 0;
  }

  @Value.Default
  default int envelopeR4Rate()
  {
    return 99;
  }

  @Value.Default
  default int velocitySensitivity()
  {
    return 0;
  }

  @Value.Derived
  default int velocitySensitivityPacked()
  {
    return this.velocitySensitivity() << 2;
  }

  @Value.Default
  default int lfoAmplitudeModulationSensitivity()
  {
    return 0;
  }

  @Value.Default
  default int levelScalingBreakpoint()
  {
    return 0x27; // C3
  }

  @Value.Default
  default int levelScalingLeftDepth()
  {
    return 99;
  }

  @Value.Default
  default int levelScalingRightDepth()
  {
    return 99;
  }

  @Value.Default
  default LevelScalingCurve levelScalingLeftCurve()
  {
    return LevelScalingCurve.LINEAR_NEGATIVE;
  }

  @Value.Default
  default LevelScalingCurve levelScalingRightCurve()
  {
    return LevelScalingCurve.LINEAR_NEGATIVE;
  }

  @Value.Default
  default int rateScaling()
  {
    return 0;
  }

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.levelScalingBreakpoint(),
      "Level scaling breakpoint",
      RangeInclusiveI.of(0, 99),
      "Valid breakpoints");

    RangeCheck.checkIncludedInInteger(
      this.levelScalingLeftDepth(),
      "Level scaling left depth",
      RangeInclusiveI.of(0, 99),
      "Valid level scaling depths");

    RangeCheck.checkIncludedInInteger(
      this.levelScalingRightDepth(),
      "Level scaling right depth",
      RangeInclusiveI.of(0, 99),
      "Valid level scaling depths");

    RangeCheck.checkIncludedInInteger(
      this.velocitySensitivity(),
      "Velocity sensitivity",
      RangeInclusiveI.of(0, 7),
      "Valid sensitivities");

    RangeCheck.checkIncludedInInteger(
      this.lfoAmplitudeModulationSensitivity(),
      "Amplitude modulation sensitivity",
      RangeInclusiveI.of(0, 3),
      "Valid sensitivities");

    RangeCheck.checkIncludedInInteger(
      this.oscillatorFrequencyCoarse(),
      "Oscillator coarse frequency",
      RangeInclusiveI.of(0, 31),
      "Valid frequencies");

    RangeCheck.checkIncludedInInteger(
      this.oscillatorFrequencyFine(),
      "Oscillator fine frequency",
      RangeInclusiveI.of(0, 99),
      "Valid frequencies");

    RangeCheck.checkIncludedInInteger(
      this.oscillatorFrequencyDetune(),
      "Oscillator detune",
      RangeInclusiveI.of(-7, 7),
      "Valid detunes");

    RangeCheck.checkIncludedInInteger(
      this.outputLevel(),
      "Output Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR1Level(),
      "R1 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR1Rate(),
      "R1 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR2Level(),
      "R2 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR2Rate(),
      "R2 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR3Level(),
      "R3 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR3Rate(),
      "R3 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR4Level(),
      "R4 Level",
      RangeInclusiveI.of(0, 99),
      "Valid levels");

    RangeCheck.checkIncludedInInteger(
      this.envelopeR4Rate(),
      "R4 Rate",
      RangeInclusiveI.of(0, 99),
      "Valid rates");

    RangeCheck.checkIncludedInInteger(
      this.rateScaling(),
      "Rate scaling",
      RangeInclusiveI.of(0, 7),
      "Valid rate scaling values");
  }

  enum LevelScalingCurve
  {
    LINEAR_NEGATIVE(0),
    EXPONENTIAL_NEGATIVE(1),
    EXPONENTIAL_POSITIVE(2),
    LINEAR_POSITIVE(3);

    private final int mode;

    LevelScalingCurve(
      final int m)
    {
      this.mode = m;
    }

    public static LevelScalingCurve ofInteger(
      final int value)
    {
      switch (value) {
        case 0:
          return LINEAR_NEGATIVE;
        case 1:
          return EXPONENTIAL_NEGATIVE;
        case 2:
          return EXPONENTIAL_POSITIVE;
        case 3:
          return LINEAR_POSITIVE;
        default: {
          throw new IllegalArgumentException(
            "No level scaling curve for value: " + value);
        }
      }
    }

    public int external()
    {
      return this.mode;
    }
  }

  enum OscillatorMode
  {
    RATIO(0),
    FIXED(1);

    private final int mode;

    OscillatorMode(
      final int m)
    {
      this.mode = m;
    }

    public static OscillatorMode ofInteger(
      final int value)
    {
      switch (value) {
        case 0:
          return RATIO;
        case 1:
          return FIXED;
        default: {
          throw new IllegalArgumentException("No oscillator for value: " + value);
        }
      }
    }

    public int external()
    {
      return this.mode;
    }
  }
}
