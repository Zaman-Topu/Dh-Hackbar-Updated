<div align="center">

<img src="https://img.shields.io/badge/DroidHack%20Pro-v2.0-00C853?style=for-the-badge&logoColor=white" alt="DroidHack Pro"/>

# 🛡️ DroidHack Pro

### Advanced Web Penetration Testing Toolkit for Android

[![Build Status](https://img.shields.io/github/actions/workflow/status/Zaman-Topu/Dh-Hackbar-Updated/release.yml?style=for-the-badge&logo=github&label=Build)](https://github.com/Zaman-Topu/Dh-Hackbar-Updated/actions)
[![Platform](https://img.shields.io/badge/Platform-Android%205.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue?style=for-the-badge)](https://developer.android.com/tools/releases/platforms)
[![UI Framework](https://img.shields.io/badge/UI-Material%20Design%203-blueviolet?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

</div>

---

## 📖 What is DroidHack Pro?

**DroidHack Pro** is a fully revived, modernized, and highly optimized Android penetration testing toolkit. Originally based on the classic **[DH-HackBar](https://github.com/darknethaxor/DH-HackBar)** by Darknet Haxor, this project brings browser-based web security testing to modern Android devices with a stunning new UI and powerful new features.

This is the ultimate mobile toolkit for Ethical Hackers, Bug Bounty Hunters, and Security Researchers who want to test Web Applications on the go.

### 🌟 What's New in v2.0 (The Pro Update)
- 🎨 **Google Material Design 3 (Material You):** The entire app UI has been overhauled. It now features sleek *Suggestion Chips*, beautiful *Dynamic Colors* (adapts to your device's wallpaper), and flawless layout proportions.
- 🍪 **Built-in Cookie Editor:** A fully functional native Cookie Editor! View, add, edit, and delete cookies on the fly for any domain. Essential for session hijacking tests and auth bypasses.
- 🔓 **Advanced WebView Security Overrides:** Fully enabled **Mixed Content Mode** (HTTP inside HTTPS) and custom DOM storage, ensuring no payloads or external injected scripts get blocked during your pentest.
- 🚀 **Zero UI Lag / ANR:** Fixed massive legacy layout loops and infinite measuring bugs. The app is now insanely fast and buttery smooth.
- 📐 **Vector Graphics:** Replaced all pixelated old Holo icons with pure Google Material SVG VectorDrawables.

---

## ✨ Full Feature List

### 🌐 Pentesting Browser (The HackBar)
A full-featured browser powered by a heavily customized Android WebView, engineered specifically for injecting payloads:
- URL manipulation and direct parameter injection
- **[NEW] Built-in Native Cookie Editor (Add/Edit/Delete)**
- **[NEW] Mixed Content Execution (Allows HTTP scripts inside HTTPS)**
- Real-time progress indicator
- Find-in-page functionality
- POST data injection support
- No-redirect mode (Intercept Headers)
- View raw page source
- JavaScript console injection (`javascript:alert(1)`)
- Custom User-Agent switcher (Pre-loaded with multiple device UAs)

### 💉 SQL Injection Toolkit (SQLi)
Pre-loaded with hundreds of tested payloads across multiple database architectures:
- **Basic SQLi:** Classic `OR 1=1`, ORDER BY, GROUP BY probes
- **Union Select:** Column enumeration and data extraction
- **Error Based:** EXTRACTVALUE, UPDATEXML, SLEEP-based detection
- **XPath Injection:** Blind XPath extraction payloads
- **MSSQL:** `xp_cmdshell`, `WAITFOR DELAY`, sysobjects enumeration
- **PostgreSQL:** `pg_sleep`, `COPY TO PROGRAM`, version extraction
- **DIOS:** Dump-In-One-Shot single query data extraction (MySQL, MSSQL, Oracle)

### 🚪 Bypass Payloads
- **Auth Bypass:** Login form bypass via SQLi vectors (`admin' --`)
- **Union Select Bypass:** Case mixing, inline comments (`/*!UNION*/`), URL encoding
- **WAF Bypass:** Hex encoding, Unicode tricks, statement obfuscation
- **Order By Bypass:** Column order enumeration bypasses

### 📂 File Inclusion (LFI / RFI / RCE)
- **LFI (Local File Inclusion):** `/etc/passwd`, PHP base64 filters, `proc/self/environ`, zip wrappers
- **RFI (Remote File Inclusion):** Remote shell execution via HTTP/FTP
- **RCE (Remote Code Execution):** Command injection separators (`|`, `;`, `&&`) and OS command payloads

### 🔥 Cross-Site Scripting (XSS)
Reflected, stored, and DOM-based XSS vectors including:
- `<script>`, `<img onerror>`, `<svg onload>` tags
- HTML entity and URL-encoded bypasses
- JavaScript URI schemes and Event-handler based payloads

### 🔍 Admin Panel Finder & Scanner
Multi-path scanner that tests hundreds of common admin panel URLs (e.g., `/admin`, `/cpanel`, `/wp-admin`) against a target domain to discover hidden admin directories automatically.

### 📝 Custom Query Manager
Save, organize, and reuse your own custom SQL queries and payloads.
- Add / Edit / Delete custom payloads.
- Persistent storage across sessions.
- One-tap injection directly into the URL bar.

### 🔧 Encoding & Utilities
- Base64 Encode / Decode
- Hex Encode / Decode
- MD5 Hash Generator
- Binary & ASCII conversion
- URL Balancer
- String extractor & Writable path detection
- User privilege check

---

## 📥 Download & Installation

1. Go to the [**Releases**](https://github.com/Zaman-Topu/Dh-Hackbar-Updated/releases) page.
2. Download the latest `app-debug.apk` (Recommended) or `app-release-unsigned.apk`.
3. On your Android device, enable **"Install from Unknown Sources"** in Settings.
4. Install and launch **DroidHack Pro**.

> **Compatibility:** Android 5.0 (Lollipop) up to Android 14 (API 34).

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
This project is a modernized and completely overhauled fork of the original **DH-HackBar** tool:

> **[DH-HackBar](https://github.com/darknethaxor/DH-HackBar)** — by **[Darknet Haxor](https://github.com/darknethaxor)**  
> *"DH HackBar — Advanced Web Penetration Testing Tool"*

All original core feature concepts and penetration testing payload logic belong to the original author. Full credit goes to **Darknet Haxor** and the Darknet Haxor community.

### DroidHack Pro Team
| Role | Name |
|---|---|
| **Original Developer** | Master Java (via Darknet Haxor) |
| **Modernizer & Maintainer** | Zaman Topu |
| **Contributor** | Frozen Flame |
| **Contributor** | Md Jubayer |

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