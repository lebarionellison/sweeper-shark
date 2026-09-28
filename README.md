# Sweeper Shark

**Daemon OS — private email security**

Sweeper Shark is a local-first email threat scanner designed to help people identify phishing, spam, suspicious links, and sensitive requests without sending message contents to a remote scanning service.

Package: `com.daemonos.sweepershark`

## Sweeper Shark Core

The Core edition provides local email analysis:

- Import `.eml`, `.txt`, or `.html` messages
- Explainable threat scoring
- Suspicious-link detection
- Urgency and manipulation detection
- Credential and financial-request detection
- Local quarantine
- Local cleanup history
- Local cleanup reports

Message contents are analyzed on the device.

The current Core build does **not** connect to or delete messages from Gmail, Outlook, or another live mailbox.

## Sweeper Shark Pro

Sweeper Shark Pro is the commercial product layer planned around the local Core engine.

Pro features are intended to include:

- Live mailbox protection
- Automated threat handling
- Advanced Daemon Intelligence
- Sender and domain controls
- Advanced phishing and impersonation detection
- Multi-mailbox protection
- Extended cleanup history and analytics
- Commercial support

Pro functionality will be implemented separately from the F-Droid-compatible Core distribution.

## Privacy

Sweeper Shark is designed around local processing.

The Core application does not require:

- An account
- Google Play Services
- Advertising
- Analytics SDKs
- Mandatory cloud scanning
- Uploading email contents for analysis

## Threat scores

Threat scores are explainable signals, not guarantees.

A low score does not prove that a message is safe, and a high score does not by itself prove malicious intent. Unexpected requests should always be verified through a trusted channel.

## F-Droid

The Core distribution is being prepared for F-Droid compatibility.

The final release will include the applicable FOSS license, complete source, release metadata, icon assets, screenshots, and reproducible build configuration required for submission.

The commercial Pro product is separate from the F-Droid Core distribution.

## Build

Open the project in Android Studio and build the `app` module.

## Brand

Sweeper Shark is a Daemon OS product.

**Daemon OS — Intelligence for systems that need to understand themselves.**
