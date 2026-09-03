{{/* Service name: nameOverride or the release name */}}
{{- define "microservice.name" -}}
{{- .Values.nameOverride | default .Release.Name -}}
{{- end -}}

{{/* OTEL_SERVICE_NAME: tracing.serviceName or the service name */}}
{{- define "microservice.tracingServiceName" -}}
{{- .Values.tracing.serviceName | default (include "microservice.name" .) -}}
{{- end -}}

{{- define "microservice.labels" -}}
app: {{ include "microservice.name" . }}
tier: backend
{{- end -}}
