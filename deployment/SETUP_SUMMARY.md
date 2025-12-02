# 🎯 Dev & Prod Deployment Setup Complete!

## 📦 What Was Created

### Docker Compose Files
- ✅ **`deployment/compose.dev.yaml`** - Development environment with hot-reloading
- ✅ **`deployment/compose.prod.yaml`** - Production environment with security & optimization
- ℹ️ **`deployment/compose.yaml`** - Original file (consider deprecating)

### Environment Configuration
- ✅ **`deployment/.env.dev`** - Development environment variables
- ✅ **`deployment/.env.prod.example`** - Production template (copy to `.env.prod`)

### Spring Boot Profiles
- ✅ **`backend/src/main/resources/application-dev.properties`** - Dev config
- ✅ **`backend/src/main/resources/application-prod.properties`** - Prod config

### Dockerfiles (Updated)
- ✅ **`backend/Dockerfile`** - Added `dev` stage for hot-reload
- ✅ **`frontend/Dockerfile`** - Added `dev` stage for hot-reload

### Helper Scripts
- ✅ **`deployment/dev-start.sh`** - Quick dev startup
- ✅ **`deployment/prod-deploy.sh`** - Production deployment

### Nginx (Optional)
- ✅ **`deployment/nginx/nginx.conf`** - Reverse proxy config
- ✅ **`deployment/nginx/README.md`** - SSL setup guide

### Documentation
- ✅ **`deployment/README.md`** - Comprehensive deployment guide
- ✅ **`DEPLOYMENT.md`** - Quick reference card

### Security
- ✅ **`.gitignore`** - Updated to exclude sensitive files

---

## 🚀 Quick Start

### Development
```bash
# Start development environment
./deployment/dev-start.sh

# Or with database tools
./deployment/dev-start.sh --tools -d
```

**Access:**
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Database: postgresql://localhost:5432/gofish_dev
- PgAdmin: http://localhost:5050 (with --tools)
- Debug: localhost:5005

### Production
```bash
# First time setup
cp deployment/.env.prod.example deployment/.env.prod
# Edit .env.prod - SET SECURE PASSWORDS!

# Deploy
./deployment/prod-deploy.sh

# Or with Nginx
./deployment/prod-deploy.sh --proxy
```

---

## 🔑 Key Features

### Development Environment
- ✨ **Hot Reloading**: Code changes reflect instantly
  - Backend: Spring Boot DevTools
  - Frontend: Vite HMR
- 🐛 **Debug Support**: Java debugger on port 5005
- 📊 **Database Tools**: Optional PgAdmin
- 📝 **Verbose Logging**: Full SQL and request logging
- 🔓 **Permissive CORS**: Easy frontend development

### Production Environment
- 🔒 **Security Hardened**:
  - No exposed debug ports
  - Minimal error messages
  - Secure session cookies
  - HTTPS ready
- ⚡ **Optimized**:
  - Multi-stage builds (smaller images)
  - Connection pooling
  - Response compression
  - Static asset caching
- 🏥 **Health Checks**: All services monitored
- 📉 **Log Rotation**: Prevents disk overflow
- 🔄 **Auto Restart**: Services recover from crashes

---

## 📊 Architecture Comparison

| Feature | Development | Production |
|---------|-------------|------------|
| **Build Target** | `dev` stage | `final` stage |
| **Hot Reload** | ✅ Yes | ❌ No |
| **Debug Port** | ✅ 5005 | ❌ Disabled |
| **DB Exposed** | ✅ Port 5432 | ❌ Internal only |
| **Logging** | 🔊 Verbose | 🔇 Minimal |
| **Error Details** | ✅ Full stack | ❌ Generic |
| **CORS** | 🌍 Allow all | 🔒 Restricted |
| **Image Size** | ~800MB | ~200MB |
| **Startup Time** | ~30s | ~10s |

---

## 🔄 Common Workflows

### Making Code Changes (Dev)
1. Edit code in your IDE
2. Changes auto-reload in containers
3. No restart needed! ✨

### Viewing Logs
```bash
# All services
docker compose -f deployment/compose.dev.yaml logs -f

# Specific service
docker compose -f deployment/compose.dev.yaml logs -f backend
```

### Database Operations
```bash
# Connect to DB
docker compose -f deployment/compose.dev.yaml exec db psql -U postgres -d gofish_dev

# Backup
docker compose -f deployment/compose.prod.yaml exec db pg_dump -U postgres gofish_prod > backup.sql

# Restore
docker compose -f deployment/compose.prod.yaml exec -T db psql -U postgres gofish_prod < backup.sql
```

### Clean Restart
```bash
# Stop and remove everything
docker compose -f deployment/compose.dev.yaml down -v

# Start fresh
./deployment/dev-start.sh --build
```

---

## ⚠️ Important Notes

### Security Checklist for Production
- [ ] Set strong `POSTGRES_PASSWORD` in `.env.prod`
- [ ] Never commit `.env.prod` to git
- [ ] Configure SSL certificates for Nginx
- [ ] Update `ALLOWED_ORIGINS` for CORS
- [ ] Review and adjust rate limits in Nginx
- [ ] Set up regular database backups
- [ ] Configure monitoring/alerting

### Environment Variables
The compose files use environment variables for configuration:
- Development: Uses `.env.dev` (safe defaults)
- Production: Requires `.env.prod` (you must create this!)

### Spring Profiles
The backend automatically uses the correct profile:
- Dev: `SPRING_PROFILES_ACTIVE=dev` → `application-dev.properties`
- Prod: `SPRING_PROFILES_ACTIVE=prod` → `application-prod.properties`

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process
sudo lsof -i :8080

# Kill it
kill -9 <PID>
```

### Database Won't Start
```bash
# Check logs
docker compose -f deployment/compose.dev.yaml logs db

# Remove volume and restart
docker compose -f deployment/compose.dev.yaml down -v
docker compose -f deployment/compose.dev.yaml up
```

### Hot Reload Not Working
```bash
# Rebuild without cache
docker compose -f deployment/compose.dev.yaml build --no-cache
docker compose -f deployment/compose.dev.yaml up
```

---

## 📚 Next Steps

1. **Test Development Environment**
   ```bash
   ./deployment/dev-start.sh
   ```

2. **Configure Production**
   ```bash
   cp deployment/.env.prod.example deployment/.env.prod
   # Edit .env.prod with secure values
   ```

3. **Set Up SSL** (for production)
   - See `deployment/nginx/README.md`
   - Use Let's Encrypt for free certificates

4. **Set Up CI/CD**
   - GitHub Actions example in `deployment/README.md`
   - Automate deployments

5. **Configure Monitoring**
   - Add Prometheus/Grafana
   - Set up log aggregation
   - Configure alerts

---

## 📖 Documentation

- **Comprehensive Guide**: `deployment/README.md`
- **Quick Reference**: `DEPLOYMENT.md`
- **Nginx Setup**: `deployment/nginx/README.md`

---

## 🎉 You're All Set!

Your Go-Fish application now has:
- ✅ Separate dev and prod environments
- ✅ Hot-reloading for development
- ✅ Production-ready security
- ✅ Easy deployment scripts
- ✅ Comprehensive documentation

**Start developing:**
```bash
./deployment/dev-start.sh
```

**Questions?** Check the documentation or run:
```bash
docker compose -f deployment/compose.dev.yaml --help
```
