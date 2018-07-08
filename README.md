# be-aware

## Introduction

A tool to remind a player to check his minimap (or anything else) every few seconds.

## Code Samples

Default properties, can be overwritten in a file called settings.properties that should be created in the same folder as the jar
```
#name of the .wav file to load from the resources to play
sound=ba_bum
#the interval to loop the sound
interval=2
```

## Installation

Download the sources and run the `shadowJar` gradle task. The finished .jar can be as in `build/libs/be-aware-all.jar`.