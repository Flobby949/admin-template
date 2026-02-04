# ------------------------------------------------------------------------------
# Stage 1: Build Phase
# Uses Node.js 20 on Alpine Linux for a lightweight build environment.
# ------------------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package definition files first to leverage Docker layer caching.
# This ensures 'npm install' is only re-run if dependencies change.
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy the rest of the application source code
COPY . .

# Build the application for production (outputs to /app/dist)
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 2: Production Runtime
# Uses Nginx on Alpine to serve the static assets.
# ------------------------------------------------------------------------------
FROM nginx

# Remove the default Nginx configuration file
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the build artifacts from the builder stage to the Nginx web root
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80 for traffic
EXPOSE 80

# Start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
