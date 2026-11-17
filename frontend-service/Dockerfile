# Builds the Vue application, then serves it with nginx: the final image holds no Node.js.
FROM node:24.0.0-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.28-alpine
# Makes the nginx entrypoint export the DNS servers of the container as $NGINX_LOCAL_RESOLVERS,
# so that the same template works under both Docker and Kubernetes DNS.
ENV NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
