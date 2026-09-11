{{/*
Mirrors the Gradle `getServiceName()` convention (build-logic/src/main/kotlin/utils/Utils.kt):
strip the "-service" suffix. Used only for the Mongo resource names, to match the DNS names
already hardcoded in each service's MONGODB_URI (e.g. "user-mongodb", not "user-service-mongodb").
*/}}
{{- define "munchies-service.shortName" -}}
{{- .Values.serviceName | trimSuffix "-service" -}}
{{- end -}}
