package utn.dds.agregador.config;

import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

public class MetricsConfig {
    private final PrometheusMeterRegistry registry;

    public MetricsConfig() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Registrar métricas de consumo de recursos JVM
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);

        // ProcessorMetrics puede fallar en algunos entornos de contenedores
        try {
            new ProcessorMetrics().bindTo(registry);
        } catch (Exception e) {
            System.err.println("Warning: No se pudieron registrar ProcessorMetrics: " + e.getMessage());
        }

        new JvmThreadMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);
        new JvmInfoMetrics().bindTo(registry);
    }

    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    public String scrape() {
        return registry.scrape();
    }
}
