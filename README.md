# DSTChatApp (DST 4010) — Firebase Client–Server Chat App (Kotlin)

A real-time Android chat application built in Kotlin with Firebase as the backend. Multiple clients can sign in, join chat rooms, and exchange messages with near real-time synchronisation through Cloud Firestore listeners. The app also includes a per-room typing indicator to improve conversational flow.

## Course Information

- **Course:** DST 4010 — Distributed Systems  
- **Prepared for:** Dr. Stanley Githinji  
- **Project:** Client–Server Chat Application using Firebase (Kotlin)  
- **Date:** 02 February 2026  
- **Author:** *[Your Name]*  
- **Student ID:** *[Your Student ID]*  
- **Programme/Cohort:** *[Your Programme]*  

---

## Overview

DSTChatApp demonstrates distributed-systems behaviour in a mobile application context:

- Real-time publish/subscribe messaging via Firestore snapshot listeners
- Multi-client state convergence (eventual consistency under network delay)
- Shared backend security via Firebase Authentication and Firestore/Storage rules
- Persistent message history and room-based organisation

---

## Key Features

### Core Functionality
- **User authentication** using Firebase Authentication (Email/Password)
- **Chat room management**
  - View available rooms
  - Create a new room (rooms list updates immediately)
- **Real-time text messaging**
  - Send and receive messages within a room
  - Messages synchronise across **three or more** clients

### Extended Feature
- **Typing indicator**
  - Shows “*{user} is typing…*” per room
  - Designed with short expiry to avoid stale “typing” states

---

## Technology Stack

- **Language:** Kotlin
- **IDE:** Android Studio
- **Backend:** Firebase
  - Firebase Authentication (Email/Password)
  - Cloud Firestore (rooms + messages persistence/synchronisation)
  - Firebase Storage (media messages stored as URLs)
- **Testing/Recording:** TestProject Smart Recorder (user-flow performance runs)

---

## Architecture

### Client–Server Model (Firebase-Backed)
Clients act as Android frontends, while Firebase services provide backend capabilities.

**High-level flow**
1. Client sends a message
2. Message is stored in Firestore
3. Firestore listeners propagate updates to other connected clients
4. Clients render the updated message list

### Real-Time Synchronisation
Firestore snapshot listeners enable publish/subscribe style updates where message changes are pushed to subscribed clients without manual refresh.

### Consistency Notes
The system may experience brief delays during poor connectivity, but clients converge to the same room history once the network stabilises (eventual consistency).

---

## Data Model (Firestore)

This project uses three primary collections:

- **User**
- **Room**
- **Message**

**Relationships**
- A **User** can belong to many rooms and send many messages.
- A **Room** contains many messages.
- Messages are associated to users via `userId`/`senderId`.

**Typing state**
Typing status is maintained at the room level (e.g., a boolean state and related metadata). When true, the room is in “composition” state; when false, the user has stopped typing.

> You can implement typing expiry using timestamps (recommended) so that typing state automatically resets if a client disconnects unexpectedly.

---

## Security

DSTChatApp is designed to prevent unauthorised access:

- Users must authenticate before reading rooms/messages.
- Firestore rules should follow **least privilege**:
  - Allow reads/writes only to authenticated users
  - Enforce sender identity consistency (e.g., `senderId == request.auth.uid`)
  - Validate that a user is a permitted member of a room before granting access
- Storage access should be restricted:
  - Avoid public buckets
  - Prefer per-room storage paths
  - Allow access only to authorised users

### Threats and Practical Mitigations (Summary)

| Threat | Impact | Mitigation |
|---|---|---|
| Unauthorised access | Intrusion into rooms and messages | Require auth for all reads/writes; enforce room membership checks |
| Weak database rules | Increased risk of abuse/data tampering | Least-privilege Firestore rules; enforce `senderId` consistency |
| Data leakage in storage | Media exposed publicly | Restrict Storage reads; avoid public access; use per-room folders |
| Man-in-the-middle | Potential interception attempts | Rely on Firebase TLS; avoid insecure custom endpoints; pinning only if justified |

---

## Getting Started

### Prerequisites
- Android Studio (latest stable recommended)
- JDK (Android Studio bundled JDK is fine)
- A Firebase project with:
  - Authentication enabled (Email/Password)
  - Firestore database created
  - Firebase Storage enabled (if using media)

### Firebase Setup
1. Create a Firebase project in the Firebase Console.
2. Add an Android app to the Firebase project:
   - Use your package name exactly as in your Android manifest.
3. Download `google-services.json`.
4. Place `google-services.json` in:
