# ==============================================================================
# Dockerfile untuk GEARA CRM Suku Cadang - Cloud Public Production Backend
# Siap dideploy ke Google Cloud Run, AWS App Runner/EC2, Render, atau Railway
# ==============================================================================

FROM node:20-alpine

# Set working directory di dalam container
WORKDIR /usr/src/app

# Salin package manifest
COPY package*.json ./

# Salin server script dan database lokal json
COPY server.js ./
COPY data/ ./data/

# Environment Variables
ENV NODE_ENV=production
ENV PORT=8080

# Expose port container (Cloud Run default port: 8080)
EXPOSE 8080

# Jalankan server
CMD ["node", "server.js"]
