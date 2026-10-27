# samples

Small, deliberately vulnerable code for the SAST pipeline POC. **Never deploy any of this.**

> Part of a SAST pipeline built from open-source parts: **samples** (vulnerable code + ground truth) → [os-sast](https://github.com/ImohitI/os-sast) (scanner CLI) → [sast-platform](https://github.com/ImohitI/sast-platform) (ingest API, Kafka, consumer, Jenkins) → [sast-dashboard](https://github.com/ImohitI/sast-dashboard) (dashboard API + UI).  
> Design decisions: [DESIGN.md](https://github.com/ImohitI/sast-platform/blob/main/docs/DESIGN.md) · one scan followed end to end: [WALKTHROUGH.md](https://github.com/ImohitI/sast-platform/blob/main/docs/WALKTHROUGH.md)

Each language folder has planted bugs **and** bait: code that looks risky but is safe. [`expected.json`](expected.json) is the ground truth (file, sink line, CWE, `isVulnerable`), so `os-sast eval` can compute precision and recall.

| Folder | Planted bugs | Bait (safe) |
|--------|-------------|-------------|
| `python/` | SQL injection, command injection, path traversal | parameterized query, argument-list subprocess, basename + realpath check |
| `java/` | SQL injection, path traversal, command injection | `PreparedStatement` |
| `kotlin/` | SQL injection, MD5 as a checksum | `prepareStatement` |
| `php/` | SQL injection, XSS, command injection | `mysqli_prepare`, `htmlspecialchars` |
| `js/` | XSS, command injection, path traversal | `execFile` with an array, `sendFile` with `root` |

There are no "VULN" comments in the code on purpose: the AI triage step sees the surrounding lines, and a comment would give the answer away.

## Pipeline

[`Jenkinsfile`](Jenkinsfile): Poll SCM every 2 minutes → `os-sast scan` → `POST $INGEST_URL/api/scan-results` → fail unless HTTP 202. `result.json` is archived on every build.

The Jenkins job itself is created by `sast-platform/jenkins` (configuration as code), which mounts this repo into the container.

## Scan by hand

```bash
os-sast scan . --repo samples --build 1 --commit "$(git rev-parse --short HEAD)" --out result.json
os-sast eval result.json expected.json
```

## License

[Apache-2.0](LICENSE)
