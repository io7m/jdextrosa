package com.io7m.jdextrosa.io;

import com.io7m.jdextrosa.core.Dx7VoiceNamed;
import io.vavr.collection.Vector;

public interface Dx7SysExReaderType
{
  Vector<Dx7VoiceNamed> parse();
}
