# Multi-stage build for React admin application
FROM node:24-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code and build
COPY . .
RUN npm run build

# Production stage with Nginx
FROM nginx:alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built React app
COPY --from=build /app/build /usr/share/nginx/html

# Create non-root user
RUN addgroup -g 1001 -S nginx && \
    adduser -u 1001 -S nginx -G nginx

# Change ownership
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d

# Switch to non-root user
USER nginx

# Expose port
EXPOSE 3001

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:3001 || exit 1

CMD ["nginx", "-g", "daemon off;"]
