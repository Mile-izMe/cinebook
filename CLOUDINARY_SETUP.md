# Cloudinary image storage

The existing `/api/storage/presigned-url` endpoint, `objectKey` fields, MinIO services and MinIO dependency remain available. The frontend helper understands both MinIO signed POST policies and Cloudinary signed uploads.

Add these variables to the **backend** environment (Railway Variables, or the IDE's environment when running locally):

```dotenv
STORAGE_PROVIDER=cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

Restart the backend after configuring them. Do not put the API secret in FE variables or commit it. A local `.env` file is not automatically loaded by Spring Boot; configure the IDE to load it or export the variables before starting Java.

Without `STORAGE_PROVIDER`, the application continues using MinIO for local compatibility. `STORAGE_PROVIDER=minio` selects the original implementation. Cloudinary mode does not create a MinIO client or contact MinIO at startup.

Uploads use signed `public_id`, timestamp, allowed formats and `overwrite=false`. Cloudinary keys have a `cloudinary/` prefix so legacy MinIO keys are distinguishable. JPEG, PNG and WebP up to 5 MB are accepted; the FE checks size before uploading, and BE checks actual stored asset size/type before accepting an image reference. Cloudinary signatures last one hour. Cloudinary may temporarily store an oversized direct upload, but the backend rejects it when saving the profile or movie.

Avatar replacements use unique IDs to avoid stale cached images. BE checks ownership of avatar keys. Movie updates continue verifying new assets and deleting old images through the selected provider. Legacy MinIO assets are not deleted while Cloudinary is active; their existing URLs remain intact and still depend on the original MinIO host. TMDB URLs are unchanged. No database migration is required.

Verification: backend adapter and provider-context tests use a local mock HTTP server; FE multipart tests cover both providers. A real upload requires configured Cloudinary credentials.
