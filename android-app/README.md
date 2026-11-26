# DB3K Blog - Android App

Native Android application for the DB3K Blog (DRIZ BIKERS 3000) with Ducati-style push notifications.

## Features

- 📱 **Native Kotlin** - Built with modern Android development practices
- 🏁 **Ducati-style Push Notifications** - Custom notification channels with Ducati red branding, vibration patterns, and action buttons
- 📖 **Offline-first** - Room database for caching posts and notifications
- 🔄 **Auto-sync** - Background sync with Cloudflare Pages backend
- 🎨 **Material 3 Design** - Dark theme with Ducati red accent colors
- ⚡ **FCM Integration** - Firebase Cloud Messaging for reliable push delivery

## Architecture

- **MVVM** with Hilt dependency injection
- **Room** for local persistence
- **Retrofit + Moshi** for API communication
- **DataStore** for preferences
- **WorkManager** for background tasks
- **Navigation Component** for navigation

## Push Notifications (Ducati Style)

The app implements custom push notification channels:

| Channel | Color | Vibration Pattern | Use Case |
|---------|-------|-------------------|----------|
| New Posts | Ducati Red (#E31837) | Heartbeat (200ms, 100ms, 200ms) | New blog articles |
| Race Results | Orange (#FF6B00) | Heartbeat | Race results published |
| Events | Green (#00E676) | Heartbeat | Upcoming events/rides |

### Notification Features
- Custom notification channels per type
- Ducati red accent colors
- Action buttons (Open, Share)
- Large icon with Ducati shield
- Vibration pattern mimicking Ducati engine heartbeat
- Priority HIGH for immediate delivery
- Full-screen intent support (Android 10+)

## Backend API

The app communicates with the DB3K Blog backend hosted on Cloudflare Pages:

- **Base URL**: `https://db3k-blog.pages.dev/`
- **Endpoints**:
  - `GET /api/posts` - List all posts
  - `GET /api/posts/{slug}` - Get single post
  - `POST /api/push/register` - Register FCM token
  - `POST /api/push/unregister` - Unregister FCM token

## Requirements

- Android 7.0 (API 24) or higher
- Google Play Services for FCM
- Internet permission

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Configuration

1. **Firebase**: Add your `google-services.json` to `app/`
2. **FCM**: Configure FCM Service Account in Cloudflare Pages secrets
3. **API**: Update `BASE_URL` in `NetworkModule.kt` if needed

## Project Structure

```
app/src/main/
├── java/com/db3k/blog/
│   ├── data/
│   │   ├── db/           # Room database, DAOs
│   │   ├── model/        # Data models (Post, Notification)
│   │   ├── preferences/  # DataStore preferences
│   │   └── repository/   # Repository layer
│   ├── di/               # Hilt modules
│   ├── messaging/        # FCM service, workers
│   ├── network/          # Retrofit API service
│   ├── ui/
│   │   ├── home/         # HomeFragment
│   │   ├── posts/        # PostsFragment, PostDetailFragment
│   │   ├── events/       # EventsFragment
│   │   ├── notifications/# NotificationsFragment
│   │   └── settings/     # SettingsFragment
│   └── utils/            # Extensions
├── res/
│   ├── layout/           # XML layouts
│   ├── drawable/         # Vector drawables, icons
│   ├── values/           # Colors, strings, themes
│   ├── navigation/       # Navigation graph
│   ├── menu/             # Menus
│   └── anim/             # Animations
└── AndroidManifest.xml
```

## Ducati Design System

### Colors
- **Primary**: `#E31837` (Ducati Red)
- **Primary Dark**: `#C00000`
- **Background**: `#0D0D0D` (Near Black)
- **Surface**: `#1A1A1A` (Dark Gray)
- **Text Primary**: `#FFFFFF`
- **Text Secondary**: `#B0B0B0`

### Typography
- **Headlines**: Roboto Bold
- **Body**: Roboto Regular
- **Labels**: Roboto Medium

### Components
- Rounded corners (8-24dp)
- Elevation shadows
- Carbon fiber pattern overlays
- Red accent borders on cards

## License

Proprietary - DB3K Blog / DRIZ BIKERS 3000