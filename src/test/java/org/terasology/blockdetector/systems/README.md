Block Detector Tests
===================

This tests are developed to test some methods in the BlockDetectorSystemImpl Class. The methods that are tested are update(), initialise and detectBlocks.

### detectBlockTest
Inside this method, the detectedBlocks set will be added with a block if a block is detected near the player. From here, we place a block right at the player position. That's why we use getPlayerPosition to get current position. Next is we place a block with worldprovider.setBlock.... and then we run the method and check if the detectedBlocks set is not null (filled with our block)

### updateTest
This updateTest is created to test if the timeSinceLastUpdate is increased by delta

### initialise
This method should return the Detectors in not null condition. It's just running the method and check if Detectors is not null
