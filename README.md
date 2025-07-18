# Hello Spotify- Foreground Voice Assitant for Spotify

Lightweight, foreground service(compatible on Android 8.0+) that adds "Hello Spotify" voice commands to the native Spotify app. Activates only when Spotify is open and kills itself when the app is closed

This is designed to be invisible(almost) and efficient, providing hands-free music control without any dedicated UI.

The project is particularly a personal one since it was an issue that I faced especially when driving car, bike or in any situation where my hands are not free. Although cars already come with Android Auto support that does this in-built through the "hey google" cmd but many of them are only active when the phone is connected through USB plus many low end cars don't come with Android Auto(Bikes still don't come with Android Auto)

## Tech Stack

### Language: Kotlin
### Wake-Word: Porcupine
### Speech-Recognition: Native Android SpeechRecognizer

#### check out local.properties.template to setup the Spotify Web API and PipVoice API
