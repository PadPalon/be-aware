# be-aware

## Introduction

A tool to remind a player to check his minimap (or anything else) every few seconds.

## Code Samples

Default properties, can be overwritten in a file called settings.properties that should be created in the same folder as the jar
```
#name of the .wav file to load from the resources to play
sound=Ba_Bum
#the interval to loop the sound
interval=10
#the initial volume
volume=50
```

## Running the reminder

Start whichever script works for you in `/bin`, which means `be-aware.bat` for Windows and `be-aware` for basically everything else.
Everything is labeled and the configuration panel can be expanded. The save button creates a new settings.properties file or adjusts
it to contain the current configuration values. The current volume is saved automatically whenever it is changed.

## Building from sources

Download the sources and run one of `shadowJar`, `shadowDistZip` or `shadowDistTar` with Gradle (or `assemble` for everything).
The finished .jar can be as in `build/distributions/`.