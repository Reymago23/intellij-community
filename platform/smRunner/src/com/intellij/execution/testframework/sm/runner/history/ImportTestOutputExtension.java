/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.execution.testframework.sm.runner.history;

import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Supplier;

/**
 * Extension to import test output from xml file, e.g. generated by ant task (https://github.com/windyroad/JUnit-Schema)
 */
public interface ImportTestOutputExtension {
  ExtensionPointName<ImportTestOutputExtension> EP_NAME = ExtensionPointName.create("com.intellij.importTestOutput");

  /**
   * When extension can parse xml file under reader, then it should return corresponding handler. Otherwise return null.
   *
   * When no custom extension accepts the xml, xml would be parsed as it was exported by IDEA itself {@link ImportTestOutputExtension#findHandler(Supplier, GeneralTestEventsProcessor)}
   *
   * @return handler if xml contains tests output in recognised format,
   *         otherwise null
   */
  @Nullable
  DefaultHandler createHandler(final Reader reader, GeneralTestEventsProcessor processor) throws IOException;

  @NotNull
  static DefaultHandler findHandler(final Supplier<Reader> readerSupplier, GeneralTestEventsProcessor processor) {
    for (ImportTestOutputExtension extension : Extensions.getExtensions(EP_NAME)) {
      Reader reader = readerSupplier.get();
      if (reader == null) continue;
      try {
        DefaultHandler handler = extension.createHandler(reader, processor);
        if (handler != null) return handler;
      }
      catch (IOException ignored) {
      }
    }
    return new ImportedTestContentHandler(processor);
  }
}
