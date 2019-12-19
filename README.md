# Navigator

Android Multi-module navigator, trying to find a way to navigate into a modularized androud multi module project

# Import

```groovy
implementation "com.github.florent37:navigator:0.0.3"
```

# Multi module sample

```
project
  |
  \--app
  |
  \--routing
  |
  \--splash
  |
  \--home
  |
  \--user
```

- `splash` should know only `routing`
- `home` should know only `routing`
- `user` should know only `routing`
- `app` should not know only `routing` to start a splash
- `routing` should not know others modules

# Define routes 

In a dedicated module, created shared routes into a `Route` object

in module `routing` :
```kotlin
object Routes {

    object Splash : Route("/")
    
    object Home : Route("/home")
    
    object User : RouteWithParams<UserParams>("/user") {
        class UserParams(val userId: String) : Params
    }
}
```

# Bind routes

You need to associate an intent to each `Route` in your feature's module

in module `splash` :
```kotlin
Routes.Splash.register { context ->
     Intent(context, SplashActivity::class.java)
}
```

If you want an android module to register automatically its route, 
you can bind using applicationprovider's auto providers :

```kotlin
//automatically launched after application's onCreate()
class RouteProvider : Provider() {
    override fun provide() {
        Routes.Splash.register { context ->
            Intent(context, SplashActivity::class.java)
        }
    }
}
```

then declare it in your module's AndroidManifest.xml

```xml
<provider
      android:authorities="${applicationId}.splash.DependenciesProvider"
      android:name=".DependenciesProvider"/>
```

# Push

You can push a route from an activity (or fragment) using 

```kotlin
Navigator.of(this).push(Routes.Home)
```

You can also change the route from anywhere (eg: an android ViewModel) using `Navigator.current()`

```kotlin
Navigator.current()?.push(Routes.Home)
```

## A route with parameters

```kotlin
Navigator.of(this).push(Routes.User, UserParams(userId= "3"))
```

# Pop

```kotlin
Navigator.of(this).pop()
```

# PushReplacement

If you want to replace the current screen

```kotlin
Navigator.current()?.pushReplacement(Routes.Splash)
```