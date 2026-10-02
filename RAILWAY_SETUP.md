# CineBook backend on Railway

Use the Dockerfile with Java 21, SPRING_PROFILES_ACTIVE=prod and PORT=8080.
One replica in Singapore; start with 0.5 vCPU and 1 GB maximum RAM.
The JVM heap is capped at 384 MB. Actual usage determines billing.
Healthcheck: /api/cities. Do not enable sleeping for the booking backend.

Required environment variables:
- SPRING_DATASOURCE_URL=jdbc:postgresql://<Postgres private host>:5432/<database>
- SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD (Railway references)
- SPRING_DATA_REDIS_HOST, SPRING_DATA_REDIS_PORT, SPRING_DATA_REDIS_PASSWORD (Upstash TCP credentials)
- SPRING_DATA_REDIS_USERNAME=default (TLS enabled by prod profile)
- SPRING_RABBITMQ_HOST, SPRING_RABBITMQ_PORT=5671, SPRING_RABBITMQ_USERNAME,
  SPRING_RABBITMQ_PASSWORD, SPRING_RABBITMQ_VIRTUAL_HOST (CloudAMQP; TLS enabled)
- MINIO_ENDPOINT=https://<public Bucket domain> (also used to sign browser uploads)
- MINIO_ACCESS_KEY, MINIO_SECRET_KEY (Railway references to Bucket credentials)
- MINIO_BUCKET=cinebook; MINIO_PUBLIC_BASE_URL=https://<public Bucket domain>/cinebook
- STORAGE_PROVIDER=minio
- JWT_ACCESS_SECRET, MOCK_PAYMENT_SECRET (independent random secrets)
- MAIL_USER, MAIL_PASS (Gmail SMTP app password), MAIL_HOST=smtp.gmail.com, MAIL_PORT=587
- FRONTEND_URL=<actual Vercel origin, without trailing slash>

For the first administrator, enable BOOTSTRAP_ADMIN_ENABLED=true and provide
BOOTSTRAP_ADMIN_EMAIL, BOOTSTRAP_ADMIN_PASSWORD (at least 12 characters), BOOTSTRAP_ADMIN_PHONE.
Disable bootstrap and remove its password after first successful startup.
Existing users are never promoted or assigned a new password.

Flyway creates the schema, roles, genres and cities. It does not import local bookings/users.
Upstash supports CONFIG SET notify-keyspace-events Ex; keep it for expiry notifications.
Use the compact frontend seed scripts only after startup. Sample review users must be verified.
Do not commit secrets, .env files or cloud connection URLs with embedded passwords.
