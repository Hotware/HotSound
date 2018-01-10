# Short Introduction
Playing music with HotSound is much easier than playing with JavaSound while you still have all the control over most of the stuff. It doesn't rely on the Clip functionality to be easy to use and runs in a separate Thread if needed.

## Why HotSound?
Because playing Sound with Java isn't easy. It costs time to write that complex code and consumes precious coffee break time :P. No, just kidding. No need to write your own sound engine.

## Does HotSound support encoded Audiotypes?
Yes! If there is a JavaSound SPI any sound format can be played with HotSound. That's being done via .jar files that you have to add to your path.
(some of them are already available with this repository)

Known to work:
* MP3 (via JLayer from javazoom.org)
* OOG-Vorbis (via jogg and jorbis from javazoom.org)
* Flac (via jflac)

A tutorial on what you need to get them to work will follow.

## Examples:
Give HotSound a try and learn how to use it here:

https://github.com/Hotware/HotSoundExamples

## Contributors:
* Martin Braun <martinbraun123@aol.com> (Lead Developer)
* David Bauske <david.bauske@googlemail.com> (Testing, Code Review)
