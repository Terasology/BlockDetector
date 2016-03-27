BlockDetector
=============

This module adds a detector item that indicates the location of arbitrary blocks.

## Usage

```java
// (1) Create a new System extending BaseComponentSystem.
@RegisterSystem
public class ExampleDetectorSystem extends BaseComponentSystem {
    //(2) Inject all the necessary game classes here. Make sure to inject BlockDetectorSystem!
    @In
    private AudioManager audioManager;
    
    @In
    private BlockDetectorSystem blockDetectorSystem;

    private DetectorData data;

    // (3) Override the initialise() method.
    @Override
    public void initialise() {
        // (4) Set up your DetectorData here.
        data = new LinearAudioDetectorImpl("Core:shovel", Sets.newHashSet("core:Iris"), 32, audioManager, "BlockDetector:ScannerBeep", 200, 2000);
        
        //(5) Add a blockDetectorSystem hook.
        blockDetectorSystem.addDetector(data);
    }

    @Override
    public void shutdown() {
        // (6) Gracefully remove the detector on shutdown.
        blockDetectorSystem.removeDetector(data.getDetectorUri());
    }
}
```

## Credits

[ScannerBeep.ogg](assets/sounds/ScannerBeep.ogg) by kalisemorrison @ [freesound](https://www.freesound.org/people/kalisemorrison/sounds/202530/).
