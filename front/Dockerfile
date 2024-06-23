FROM node:22-alpine3.19 as build

WORKDIR /app
COPY . .
RUN npm ci
RUN npm run build


FROM nginx:latest
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist/front/browser /usr/share/nginx/html