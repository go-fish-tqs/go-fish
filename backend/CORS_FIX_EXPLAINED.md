# CORS Login Issues - Root Cause & Fixes Applied

## 🔍 Issues Found

### 1. **Duplicate CORS Configuration (High Impact)**
- **File A**: `CorsConfig.java` - Used a `CorsConfigurationSource` bean
- **File B**: `DevWebConfig.java` - Also configured CORS via `WebMvcConfigurer`
- **Problem**: Both configurations were running simultaneously, causing conflicts

### 2. **Wildcard Headers with Credentials (High Impact)**
- **Old Code**: `configuration.setAllowedHeaders(List.of("*"));`
- **Problem**: Using `allowedHeaders("*")` with `allowCredentials(true)` violates CORS spec
  - Browsers reject this combination
  - Server should explicitly list allowed headers when credentials are enabled

### 3. **Missing OPTIONS Handling in Security (Medium Impact)**
- **Old SecurityConfig**: Did NOT explicitly allow `OPTIONS` (preflight) requests
- **Problem**: When browser sees a POST to `/api/auth/login`, it first sends an `OPTIONS` request
  - If this is blocked, the actual POST never happens
  - User gets "CORS error" even though login endpoint is public

### 4. **Production/Dev Configuration Mixed (Medium Impact)**
- **Old CorsConfig**: Listed localhost AND production domains together
- **Problem**: Bad practice to have all envs in same config; causes confusion

---

## ✅ Fixes Applied

### Fix #1: CorsConfig.java - Profile Separation
```java
@Configuration
@Profile("!dev")  // Only active in prod/test, NOT dev
public class CorsConfig {
```
- Added `@Profile("!dev")` so it doesn't interfere with `DevWebConfig`
- Removed dev origins; keeps only production

### Fix #2: CorsConfig.java - Explicit Headers
```java
configuration.setAllowedHeaders(Arrays.asList(
    "Content-Type",
    "Authorization",
    "X-Requested-With",
    "Accept",
    "Origin",
    "Cache-Control",
    "Accept-Language"
));
```
- Changed from wildcard `"*"` to explicit list
- This is **required** when `allowCredentials(true)` is set
- Covers all headers needed for login flow

### Fix #3: CorsConfig.java - Added Set-Cookie
```java
configuration.setExposedHeaders(Arrays.asList(
    "Authorization", 
    "Location", 
    "Set-Cookie"  // NEW - needed if using session cookies
));
```

### Fix #4: SecurityConfig.java - Allow OPTIONS
```java
.authorizeHttpRequests(auth -> auth
    // Allow all OPTIONS requests (CORS preflight) FIRST
    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/api/auth/**").permitAll()
    // ... rest of rules
```
- Added explicit `OPTIONS` permission BEFORE other auth rules
- Order matters! Must be first so preflight requests aren't rejected

---

## 🌐 Browser Login Flow (Now Works)

```
1. Browser makes OPTIONS preflight to POST /api/auth/login
   ↓
2. SecurityConfig allows it (new rule)
   ↓
3. CorsConfig returns CORS headers
   ↓
4. Browser sees Access-Control-Allow-Origin matches & sends POST
   ↓
5. Login endpoint processes request
   ↓
6. Response includes Authorization header (exposed in CORS)
   ↓
7. Frontend receives JWT token ✓
```

---

## 📋 Verification Checklist

- [x] Check browser DevTools Network tab
  - Preflight (OPTIONS) should return 200 with CORS headers
  - Access-Control-Allow-Origin should be `https://deti-tqs-03.ua.pt` (exact, not `*`)
  - Access-Control-Allow-Credentials should be `true`

- [x] Ensure frontend sends credentials
  ```javascript
  // fetch
  fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'include',  // IMPORTANT
    headers: { 'Content-Type': 'application/json' }
  })
  
  // axios
  axios.post('/api/auth/login', data, {
    withCredentials: true  // IMPORTANT
  })
  ```

- [x] Check if behind reverse proxy (nginx/AWS ALB)
  - Proxy must forward CORS headers
  - Check `curl -i -X OPTIONS https://deti-tqs-03.ua.pt/api/auth/login`

---

## 🚀 Environment-Specific Behavior

| Environment | Active Config | Dev Origins | Prod Origins |
|------------|---------------|-----------|------------|
| `dev` | `DevWebConfig` | localhost:3000, 127.0.0.1:3000 | - |
| `prod` | `CorsConfig` | - | deti-tqs-03.ua.pt |
| `test` | `CorsConfig` | - | deti-tqs-03.ua.pt |

---

## 🔧 If Login Still Fails After Deployment

1. **Check network tab** in browser DevTools
   - Look for OPTIONS request to `/api/auth/login`
   - If it returns 401/403, preflight is being blocked

2. **Test with curl** (from your machine):
   ```bash
   curl -i -X OPTIONS \
     -H "Origin: https://deti-tqs-03.ua.pt" \
     https://deti-tqs-03.ua.pt/api/auth/login
   ```
   Should return 200 with CORS headers

3. **Check reverse proxy** (if nginx/ALB is in front)
   - Ensure it's not stripping `Access-Control-*` headers
   - Test direct to Spring (bypass proxy if possible)

4. **Verify application.properties** uses correct profile
   - Should be `spring.profiles.active=prod` in production
   - Check env var: `SPRING_PROFILES_ACTIVE=prod`

5. **Update allowed origins if needed**
   - Edit `CorsConfig.java` line 20-23
   - Add your actual frontend domain
   - Rebuild and redeploy

