<div align="center">

<img src="https://img.shields.io/badge/DroidHack%20Pro-v2.0-00C853?style=for-the-badge&logoColor=white" alt="DroidHack Pro"/>

# 🛡️ DroidHack Pro

### Advanced Web Penetration Testing Toolkit for Android

[![Build Status](https://img.shields.io/github/actions/workflow/status/Zaman-Topu/Dh-Hackbar-Updated/release.yml?style=for-the-badge&logo=github&label=Build)](https://github.com/Zaman-Topu/Dh-Hackbar-Updated/actions)
[![Platform](https://img.shields.io/badge/Platform-Android%205.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue?style=for-the-badge)](https://developer.android.com/tools/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)
[![Original](https://img.shields.io/badge/Based%20On-DH--HackBar-red?style=for-the-badge&logo=github)](https://github.com/darknethaxor/DH-HackBar)

</div>

---

## 📖 What is DroidHack Pro?

**DroidHack Pro** is a fully revived and modernized Android penetration testing toolkit, based on the original **[DH-HackBar](https://github.com/darknethaxor/DH-HackBar)** by **Darknet Haxor**. The original app was an innovative tool that brought browser-based web security testing to Android — but it was abandoned and no longer worked on modern Android versions.

This project brings it back to life:
- ✅ Fully compatible with **Android 5.0 → Android 14 (API 21–34)**
- ✅ All deprecated Android Support libraries migrated to **AndroidX**
- ✅ Background tasks rewritten using modern **ExecutorService** (no more AsyncTask crashes)
- ✅ File sharing updated to use **FileProvider** (no more FileUriExposedException on API 24+)
- ✅ Original classic UI preserved — no visual changes from the original app
- ✅ **GitHub Actions CI/CD** automatically builds and releases APK on every push

---

## ✨ Features

### 🌐 Built-in Hack Browser
A full-featured browser powered by Android WebView, specifically designed for web security testing:
- URL manipulation and parameter injection
- Real-time progress indicator
- Page title display and favicon support
- Find-in-page functionality
- POST data injection support
- No-redirect mode
- View page source
- JavaScript console injection
- Undo / Redo URL history
- Copy & Paste URL shortcuts

### 💉 SQL Injection Toolkit
Pre-loaded with hundreds of tested payloads across multiple categories:

| Category | Description |
|---|---|
| **Basic SQLi** | Classic `OR 1=1`, ORDER BY, GROUP BY probes |
| **Union Select** | Column enumeration and data extraction |
| **Error Based** | EXTRACTVALUE, UPDATEXML, SLEEP-based detection |
| **XPath Injection** | Blind XPath extraction payloads |
| **MSSQL** | `xp_cmdshell`, `WAITFOR DELAY`, sysobjects enumeration |
| **PostgreSQL** | `pg_sleep`, `COPY TO PROGRAM`, version extraction |
| **DIOS** | Dump-In-One-Shot single query data extraction |

### 🚪 Bypass Payloads
| Category | Description |
|---|---|
| **Auth Bypass** | Login form bypass via SQLi vectors |
| **Union Select Bypass** | Case mixing, comment injection, URL encoding |
| **WAF Bypass** | Inline comments, hex encoding, Unicode tricks |
| **Order By Bypass** | Column order enumeration bypasses |

### 📂 File Inclusion
| Category | Description |
|---|---|
| **LFI** | `/etc/passwd`, PHP filters, proc/self/environ, zip wrappers |
| **RFI** | Remote shell inclusion via HTTP/FTP |
| **RCE** | Command injection separators and OS command payloads |

### 🔥 XSS Payloads
Reflected, stored, and DOM-based XSS vectors including:
- `<script>`, `<img onerror>`, `<svg onload>` tags
- HTML entity and URL-encoded bypasses
- JavaScript URI schemes
- Event-handler based payloads

### 🔍 Admin Panel Finder
Multi-path scanner that tests hundreds of common admin panel URLs against a target domain to discover hidden admin directories automatically.

### 📝 Custom Query Manager
Save, organize, and reuse your own custom SQL queries and payloads. Supports:
- Add / Edit / Delete entries
- Persistent storage across sessions
- One-tap injection into the URL bar

### 🔧 Encoding & Utilities
- URL Encode / Decode
- Base64 Encode / Decode
- Hex Encode / Decode
- MD5 Hash Generator
- Binary & ASCII conversion
- URL Balancer
- String extractor
- Writable path detection
- User privilege check
- User-Agent switcher

### 🌍 Web Tools
Integrated web-based tools via in-app browser:
- Hash Decrypter
- SQLi Scanner
- Reverse IP Lookup
- Sub-domain Checker
- Polygon tool

---

## 📥 Download & Installation

1. Go to the [**Releases**](https://github.com/Zaman-Topu/Dh-Hackbar-Updated/releases) page.
2. Download the latest `app-debug.apk` or `app-release-unsigned.apk`.
3. On your Android device, enable **"Install from Unknown Sources"** in Settings.
4. Install and launch **DroidHack Pro**.

> **Minimum requirement:** Android 5.0 (Lollipop) or higher.

---

## 🏗️ Build from Source

```bash
# Clone the repository
git clone https://github.com/Zaman-Topu/Dh-Hackbar-Updated.git
cd Dh-Hackbar-Updated

# Build debug APK (requires Android SDK & Gradle)
./gradlew assembleDebug

# APK output path:
# app/build/outputs/apk/debug/app-debug.apk
```

**Requirements:**
- Android Studio Giraffe or newer
- Android SDK with API 34
- JDK 17+

---

## 🤝 Credits & Acknowledgements

### Original Project
This project is a modernized fork of the original **DH-HackBar** tool:

> **[DH-HackBar](https://github.com/darknethaxor/DH-HackBar)** — by **[Darknet Haxor](https://github.com/darknethaxor)**  
> *"DH HackBar — Advanced Web Penetration Testing Tool"*

All original UI design, core feature concepts, and penetration testing logic belong to the original author. Full credit goes to **Darknet Haxor** and the Darknet Haxor community.

### DroidHack Pro Team

| Role | Name |
|---|---|
| **Original Developer** | Master Java (via Darknet Haxor) |
| **Modernizer & Maintainer** | Zaman Topu |
| **Contributor** | Frozen Flame |
| **Contributor** | Md Jubayer |

### What was changed / modernized?
- Migrated from Android Support Library → **AndroidX**
- Replaced deprecated `AsyncTask` → **ExecutorService + Handler**
- Fixed `FileUriExposedException` using **FileProvider** (Android 7+)
- Added `android:exported` attributes to all Activities (Android 12+ requirement)
- Fixed all layout IDs (`@id/` → `@+id/`) for proper AAPT compilation
- Added `android.useAndroidX=true` & `android.enableJetifier=true` to build config
- Added **GitHub Actions CI/CD** workflow for automated APK releases
- App name updated to **DroidHack Pro**

---

## ⚠️ Disclaimer

> **For Educational & Authorized Testing Purposes Only.**

DroidHack Pro is designed exclusively for:
- Security researchers
- Ethical hackers and penetration testers
- CTF (Capture the Flag) participants
- Students learning web application security

**The developers and contributors are NOT responsible for any misuse, unauthorized access, or illegal activities conducted using this tool. Always obtain explicit written permission before testing any target system. Unauthorized penetration testing is illegal and punishable by law.**

---

## 📄 License

This project is licensed under the **MIT License**.  
See the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ for the ethical hacking community**

[⬆ Back to Top](#-droidhack-pro)

</div>