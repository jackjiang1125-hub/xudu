# xudu-module-facecrop Optimization Notes

- Performance
  - Cache and reuse detectors via `ThreadLocal` to avoid contention.
  - Pre-filter by file extension and size to skip non-images and huge files.
  - Use single-image public API `cropSingle(src,dst)` for controller calls.
- Error Handling
  - Standardize responses with `FaceCropResponse` and `FaceCropStatus`.
  - Return explicit codes: `SRC_NOT_FOUND`, `INVALID_FORMAT`, `NO_FACE_DETECTED`, `EYE_VERIFY_FAILED`, `PROCESSING_ERROR`.
  - Log successes and failures with duration for observability.
- Validation & Limits
  - Validate image format (`jpg/jpeg/png/bmp`).
  - Enforce size limits in controller and service when fetching or decoding avatars.
- Memory Management
  - Stream downloads to disk with size cap; avoid loading giant Base64 into memory.
  - Release image buffers promptly; avoid retaining large arrays in static fields.
- Security
  - Normalize paths to prevent traversal; only write within `jeecg.path.upload`.
  - Sanitize target file names; keep extensions controlled.

These changes align with JeecgBoot’s Result-unified API style and keep integration minimal for `SysUserController` while improving robustness.