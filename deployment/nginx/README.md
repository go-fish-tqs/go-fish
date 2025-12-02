# Nginx Configuration

This directory contains Nginx reverse proxy configuration for production deployment.

## SSL Certificates

Place your SSL certificates in the `ssl/` directory:
- `cert.pem` - SSL certificate
- `key.pem` - Private key

### Generate Self-Signed Certificate (Development/Testing Only)

```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ssl/key.pem \
  -out ssl/cert.pem \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

### Let's Encrypt (Production)

Use Certbot to obtain free SSL certificates:

```bash
# Install certbot
sudo apt-get install certbot

# Obtain certificate
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# Copy certificates
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ssl/key.pem
```

## Configuration

Edit `nginx.conf` and update:
- `server_name` - Replace with your actual domain
- Rate limiting zones (adjust as needed)
- SSL certificate paths (if different)

## Usage

Start with Nginx proxy:

```bash
docker compose -f compose.prod.yaml --profile proxy up -d
```

## Security Notes

⚠️ **Never commit SSL certificates to version control!**

The `ssl/` directory is gitignored by default.
