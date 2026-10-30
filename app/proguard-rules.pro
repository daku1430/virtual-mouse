# ProGuard rules for Virtual Mouse
# Keep accessibility service
-keep class com.virtualmouse.app.accessibility.MouseAccessibilityService { *; }

# Keep tile service
-keep class com.virtualmouse.app.settings.MouseTileService { *; }

# Keep view classes
-keep class com.virtualmouse.app.TrackpadView { *; }
-keep class com.virtualmouse.app.CursorView { *; }
-keep enum com.virtualmouse.app.CursorType { *; }

# Keep custom views
-keepclassmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}