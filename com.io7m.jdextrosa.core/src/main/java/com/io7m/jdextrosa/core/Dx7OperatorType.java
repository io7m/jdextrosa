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

/**
 * A DX7 operator.
 */

@DxImmutableStyleType
@Value.Immutable
public interface Dx7OperatorType
{
  /**
   * @return The operator ID
   */

  @Value.Parameter
  Dx7OperatorID id();

  /**
   * @return {@code true} iff the operator is enabled
   */

  @Value.Default
  default boolean isEnabled()
  {
    return true;
  }

  /**
   * @return The coarse oscillator frequency
   */

  @Value.Default
  default int oscillatorFrequencyCoarse()
  {
    return 1;
  }

  /**
   * @return The coarse oscillator frequency as it appears within a SysEx
   * message
   */

  @Value.Derived
  default int oscillatorFrequencyCoarsePacked()
  {
    return this.oscillatorFrequencyCoarse() << 1;
  }

  /**
   * @return The fine oscillator frequency
   */

  @Value.Default
  default int oscillatorFrequencyFine()
  {
    return 0;
  }

  /**
   * @return The detune amount for the oscillator
   */

  @Value.Default
  default int oscillatorFrequencyDetune()
  {
    return 0;
  }

  /**
   * @return The detune amount for the oscillator as it would appear in a SysEx
   * message
   */

  @Value.Derived
  default int oscillatorFrequencyDetuneExternalized()
  {
    return this.oscillatorFrequencyDetune() + 7;
  }

  /**
   * @return The (packed) detune amount for the oscillator as it would appear in
   * a SysEx message
   */

  @Value.Derived
  default int oscillatorFrequencyDetunePacked()
  {
    return this.oscillatorFrequencyDetuneExternalized() << 3;
  }

  /**
   * @return The oscillator mode
   */

  @Value.Default
  default OscillatorMode oscillatorMode()
  {
    return OscillatorMode.RATIO;
  }

  /**
   * @return The output level for the oscillator
   */

  @Value.Default
  default int outputLevel()
  {
    return 99;
  }

  /**
   * @return The R1 level for the envelope
   */

  @Value.Default
  default int envelopeR1Level()
  {
    return 99;
  }

  /**
   * @return The R1 rate for the envelope
   */

  @Value.Default
  default int envelopeR1Rate()
  {
    return 99;
  }

  /**
   * @return The R2 level for the envelope
   */

  @Value.Default
  default int envelopeR2Level()
  {
    return 99;
  }

  /**
   * @return The R2 rate for the envelope
   */

  @Value.Default
  default int envelopeR2Rate()
  {
    return 99;
  }

  /**
   * @return The R3 level for the envelope
   */

  @Value.Default
  default int envelopeR3Level()
  {
    return 99;
  }

  /**
   * @return The R3 rate for the envelope
   */

  @Value.Default
  default int envelopeR3Rate()
  {
    return 99;
  }

  /**
   * @return The R4 level for the envelope
   */

  @Value.Default
  default int envelopeR4Level()
  {
    return 0;
  }

  /**
   * @return The R4 rate for the envelope
   */

  @Value.Default
  default int envelopeR4Rate()
  {
    return 99;
  }

  /**
   * @return The velocity sensitivity for the operator
   */

  @Value.Default
  default int velocitySensitivity()
  {
    return 0;
  }

  /**
   * @return The velocity sensitivity as it would appear in a SysEx message
   */

  @Value.Derived
  default int velocitySensitivityPacked()
  {
    return this.velocitySensitivity() << 2;
  }

  /**
   * @return The amount that the LFO affects the amplitude of the operator
   */

  @Value.Default
  default int lfoAmplitudeModulationSensitivity()
  {
    return 0;
  }

  /**
   * @return The breakpoint used for level scaling
   */

  @Value.Default
  default int levelScalingBreakpoint()
  {
    /*
     * C3
     */
    return 0x27;
  }

  /**
   * @return The level scaling for all notes left of the breakpoint
   */

  @Value.Default
  default int levelScalingLeftDepth()
  {
    return 99;
  }

  /**
   * @return The level scaling for all notes right of the breakpoint
   */

  @Value.Default
  default int levelScalingRightDepth()
  {
    return 99;
  }

  /**
   * @return The scaling curve for all notes left of the breakpoint
   */

  @Value.Default
  default LevelScalingCurve levelScalingLeftCurve()
  {
    return LevelScalingCurve.LINEAR_NEGATIVE;
  }

  /**
   * @return The scaling curve for all notes right of the breakpoint
   */

  @Value.Default
  default LevelScalingCurve levelScalingRightCurve()
  {
    return LevelScalingCurve.LINEAR_NEGATIVE;
  }

  /**
   * @return The rate scaling amount
   */

  @Value.Default
  default int rateScaling()
  {
    return 0;
  }

  /**
   * Check preconditions for the type.
   */

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

  /**
   * The level scaling curve type.
   */

  enum LevelScalingCurve
  {
    /**
     * The negative linear curve
     */

    LINEAR_NEGATIVE(0),

    /**
     * The negative exponential curve
     */

    EXPONENTIAL_NEGATIVE(1),

    /**
     * The positive exponential curve
     */

    EXPONENTIAL_POSITIVE(2),

    /**
     * The positive linear curve
     */

    LINEAR_POSITIVE(3);

    private final int mode;

    LevelScalingCurve(
      final int m)
    {
      this.mode = m;
    }

    /**
     * @param value The integer value for the curve
     *
     * @return The curve associated with the given integer value
     */

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

    /**
     * @return The integer value for the curve
     */

    public int external()
    {
      return this.mode;
    }
  }

  /**
   * The oscillator mode.
   */

  enum OscillatorMode
  {
    /**
     * The pitch of the oscillator is a ratio of the base frequency
     */

    RATIO(0),

    /**
     * The pitch of the oscillator is fixed
     */

    FIXED(1);

    private final int mode;

    OscillatorMode(
      final int m)
    {
      this.mode = m;
    }

    /**
     * @param value The integer value for the oscillator mode
     *
     * @return The mode associated with the given integer value
     */

    public static OscillatorMode ofInteger(
      final int value)
    {
      switch (value) {
        case 0:
          return RATIO;
        case 1:
          return FIXED;
        default: {
          throw new IllegalArgumentException("No oscillator mode for value: " + value);
        }
      }
    }

    /**
     * @return The integer value for the mode
     */

    public int external()
    {
      return this.mode;
    }
  }
}
