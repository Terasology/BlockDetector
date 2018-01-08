BlockDetector
=============

This module implements an API for creating Detector items that indicate the location of arbitrary blocks and signal when a block is detected.

## Usage

See [EasterEggDetectorSystem](src/main/java/org/terasology/blockdetector/easter/EasterEggDetectorSystem.java) for an annotated implementation example!

## Credits

[ScannerBeep.ogg](assets/sounds/ScannerBeep.ogg) by kalisemorrison @ [freesound](https://www.freesound.org/people/kalisemorrison/sounds/202530/).


# Testing

This module also has tests for the BlockDetectorSystemImpl class. You can find the code [here](src/test/java/org/terasology/blockdetector/systems).
To run the test, simply run the code from the IDE :).
Unit Tests are available for these methods:
1) update
2) initialise
3) addDetector
4) detectBlocks
