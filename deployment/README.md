# Go-Fish Deployment Guide

This directory contains Docker Compose configurations for deploying the Go-Fish application in different environments.

## 📁 Files Overview

- **`compose.dev.yaml`** - Development environment configuration
- **`compose.prod.yaml`** - Production environment configuration
- **`compose.yaml`** - Base/legacy configuration (deprecated, use dev/prod instead)
- **`.env.dev`** - Development environment variables
- **`.env.prod.example`** - Production environment variables template

## 🚀 Quick Start

### Development Environment

```bash
# Navigate to deployment directory
cd deployment

# Start all services
docker compose -f compose.dev.yaml up

# Start in detached mode
docker compose -f compose.dev.yaml up -d

# Start with optional tools (PgAdmin)
docker compose -f compose.dev.yaml --profile tools up

# View logs
docker compose -f compose.dev.yaml logs -f

# Stop services
docker compose -f compose.dev.yaml down

# Stop and remove volumes (clean slate)
docker compose -f compose.dev.yaml down -v
```

### Production Environment

```bash
# Navigate to deployment directory
cd deployment

# Copy and configure environment variables
cp .env.prod.example .env.prod
# Edit .env.prod and set secure passwords!

# Build and start services
docker compose -f compose.prod.yaml --env-file .env.prod up -d

# View logs
docker compose -f compose.prod.yaml logs -f

# Stop services
docker compose -f compose.prod.yaml down
```

## 🔧 Development Features

### Hot Reloading

- **Backend**: Spring Boot DevTools automatically reloads on code changes
- **Frontend**: Vite/React dev server with HMR (Hot Module Replacement)

### Debug Ports

- **Backend Debug**: Port 5005 (Java Debug Wire Protocol)
  - Connect your IDE debugger to `localhost:5005`
- **Frontend**: Port 3000 (main) + 5173 (Vite HMR)

### Database Access

- **PostgreSQL**: Exposed on `localhost:5432`
  - Database: `gofish_dev`
  - Username: `postgres`
  - Password: `dev_password`
- **PgAdmin** (optional): `http://localhost:5050`
  - Email: `admin@gofish.local`
  - Password: `admin`

### Volume Mounts

Development uses volume mounts for instant code updates:

- Backend: `../backend/src` → `/app/src`
- Frontend: `../frontend/src` → `/usr/src/app/src`

## 🏭 Production Features

### Security

- No exposed database ports (internal network only)
- No debug ports
- Minimal error messages
- Non-root user execution
- Health checks for all services

### Optimization

- Multi-stage builds for minimal image size
- Production dependencies only
- JVM tuning for backend
- Optimized Node.js production build

### Monitoring

- Health checks on all services
- Log rotation (max 10MB, 3 files)
- Startup probes with grace periods

## 🔑 Environment Variables

### Development (.env.dev)

Pre-configured with safe defaults for local development.

### Production (.env.prod)

**⚠️ IMPORTANT**: Never commit `.env.prod` to version control!

Required variables:

- `POSTGRES_PASSWORD` - Strong database password
- `API_URL` - Your production API URL

## 📊 Service Ports

### Development

| Service       | Port | Description       |
| ------------- | ---- | ----------------- |
| Frontend      | 3000 | React application |
| Frontend HMR  | 5173 | Vite hot reload   |
| Backend       | 8080 | Spring Boot API   |
| Backend Debug | 5005 | Java debugger     |
| PostgreSQL    | 5432 | Database          |
| PgAdmin       | 5050 | DB admin tool     |

### Production

| Service  | Port | Description       |
| -------- | ---- | ----------------- |
| Frontend | 3000 | React application |
| Backend  | 8080 | Spring Boot API   |

## 🛠️ Common Tasks

### Rebuild Services

```bash
# Development
docker compose -f compose.dev.yaml build
docker compose -f compose.dev.yaml up --build

# Production
docker compose -f compose.prod.yaml build
```

### View Service Logs

```bash
# All services
docker compose -f compose.dev.yaml logs -f

# Specific service
docker compose -f compose.dev.yaml logs -f backend
docker compose -f compose.dev.yaml logs -f frontend
docker compose -f compose.dev.yaml logs -f db
```

### Execute Commands in Containers

```bash
# Backend shell
docker compose -f compose.dev.yaml exec backend bash

# Frontend shell
docker compose -f compose.dev.yaml exec frontend sh

# Database shell
docker compose -f compose.dev.yaml exec db psql -U postgres -d gofish_dev
```

### Database Operations

```bash
# Create database backup
docker compose -f compose.prod.yaml exec db pg_dump -U postgres gofish_prod > backup.sql

# Restore database
docker compose -f compose.prod.yaml exec -T db psql -U postgres gofish_prod < backup.sql

# Access database CLI
docker compose -f compose.dev.yaml exec db psql -U postgres -d gofish_dev
```

### Clean Up

```bash
# Stop and remove containers
docker compose -f compose.dev.yaml down

# Remove containers and volumes (⚠️ deletes data!)
docker compose -f compose.dev.yaml down -v

# Remove containers, volumes, and images
docker compose -f compose.dev.yaml down -v --rmi all
```

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find process using port
sudo lsof -i :8080
sudo lsof -i :3000

# Kill process
kill -9 <PID>
```

### Database Connection Issues

```bash
# Check database health
docker compose -f compose.dev.yaml ps db

# View database logs
docker compose -f compose.dev.yaml logs db

# Restart database
docker compose -f compose.dev.yaml restart db
```

### Hot Reload Not Working

```bash
# Rebuild with no cache
docker compose -f compose.dev.yaml build --no-cache

# Check volume mounts
docker compose -f compose.dev.yaml config
```

### Permission Issues

```bash
# Fix file permissions
sudo chown -R $USER:$USER ../backend ../frontend
```

## 📝 Best Practices

### Development

1. Always use `compose.dev.yaml` for local development
2. Don't commit `.env` files with secrets
3. Use `docker compose down -v` to clean up between major changes
4. Enable PgAdmin profile when you need database management

### Production

1. Always set strong passwords in `.env.prod`
2. Use secrets management (Docker secrets, Vault, etc.)
3. Enable health checks and monitoring
4. Regular database backups
5. Use specific version tags instead of `latest`
6. Review logs regularly: `docker compose logs --tail=100`

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
- name: Deploy to Production
  run: |
    docker compose -f deployment/compose.prod.yaml pull
    docker compose -f deployment/compose.prod.yaml up -d
    docker compose -f deployment/compose.prod.yaml ps
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Node.js Docker Best Practices](https://github.com/nodejs/docker-node/blob/main/docs/BestPractices.md)
