// src/lib/otel.ts

import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch';
import { ZoneContextManager } from '@opentelemetry/context-zone';
import { Resource } from '@opentelemetry/resources';
import { ATTR_SERVICE_NAME } from '@opentelemetry/semantic-conventions';

let isInitialized = false;

function makeOriginRegex(url?: string) {
  if (!url) return undefined;
  const escaped = url.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  return new RegExp(`^${escaped}`);
}

export function initializeOpenTelemetry() {
  if (typeof window === 'undefined' || isInitialized) {
    return;
  }

  const provider = new WebTracerProvider({
    resource: new Resource({
      [ATTR_SERVICE_NAME]: 'ui-admin-frontend',
    }),
  });

  provider.register({
    contextManager: new ZoneContextManager(),
  });

  const corsUrls: (RegExp)[] = [];

  const apiRegex = makeOriginRegex(process.env.NEXT_PUBLIC_API_URL);
  if (apiRegex) corsUrls.push(apiRegex);

  const fuenteDinamicaRegex = makeOriginRegex(
    process.env.NEXT_PUBLIC_FUENTE_DINAMICA_URL
  );
  if (fuenteDinamicaRegex) corsUrls.push(fuenteDinamicaRegex);

  registerInstrumentations({
    instrumentations: [
      new FetchInstrumentation({
        propagateTraceHeaderCorsUrls: corsUrls,
        clearTimingResources: true,
      }),
    ],
  });

  isInitialized = true;
  console.log('[OpenTelemetry] Initialized with CORS URLs:', corsUrls);
}
