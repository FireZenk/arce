# ARCE: Android remote command executor

The idea behind this project is to provide a way to interact with Android apps remotely from a web browser.
By extending the commads supported in `arce-server` and its interpreter at `arce-app` the posibilities on android interactions are limitless.

**arce-app**: Sample implementation of ARCE in Android

**arce-client**: Websocket client library for Android

**arce-server**: Ktor based websocket server

## Configuration

Specify your desktop URL at `arce-app` -> `WebSocket(YOUR_URL_HERE)`

If you want to change the nickName for your Android device do it at `arce-app` -> `Main.NICKNAME`

## How to

- Run server by launch `main` method on `Server.kt`
  - Send a receive commands at desktop opening the url `http://YOUR_IP_HERE:8080/static/index.html`
- Run the Android App normally with Android Studio

## Customisation

Define new commands into `Server.kt`

Interpret new commands previously defined into the Server in the Android code into the method `processCommand`

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/I2I13KE80)
