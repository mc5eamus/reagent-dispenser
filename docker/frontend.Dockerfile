# Stage 1: Build the Angular application
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files for dependency caching
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY frontend/ .

# Build for production
RUN npm run build -- --configuration production

# Stage 2: Serve with nginx
FROM nginx:alpine

# Copy custom nginx configuration
COPY docker/nginx.conf /etc/nginx/nginx.conf

# Copy built Angular app from build stage
COPY --from=build /app/dist/reagent-dispenser-frontend /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
