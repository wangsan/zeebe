/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.broker.exporter.repo;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueFactory;
import io.zeebe.broker.exporter.context.ExporterConfiguration;
import io.zeebe.exporter.api.Exporter;
import java.util.Map;

public class ExporterDescriptor {
  private final ExporterConfiguration configuration;
  private final Class<? extends Exporter> exporterClass;

  public ExporterDescriptor(
      final String id,
      final Class<? extends Exporter> exporterClass,
      final ConfigObject args) {
    this.exporterClass = exporterClass;
    this.configuration = new ExporterConfiguration(id, args);
  }

  public ExporterDescriptor(
    final String id,
    final Class<? extends Exporter> exporterClass,
    final Map<String, Object> args) {
    this(id, exporterClass, ConfigValueFactory.fromMap(args));
  }

  public Exporter newInstance() throws ExporterInstantiationException {
    try {
      return exporterClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ExporterInstantiationException(getId(), e);
    }
  }

  public ExporterConfiguration getConfiguration() {
    return configuration;
  }

  public String getId() {
    return configuration.getId();
  }
}
