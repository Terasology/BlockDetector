Block Detector Tests
===================

This tests are developed to test some methods in the BlockDetectorSystemImpl Class. The methods that are tested are update(), initialise, addDetector and detectBlocks.

### detectorTest
This test method is to test whether the method addDetector is functioning or not. We use the same method that CaveDetectorSystem do add a detector. Then we want to assert if the map contains the same data we want to be stored there. When we try to get the values, it is started and ended with '[' and ']'. Therefore, we use string data type so we can use substring to remove them. Then we assert that both values (expected which is the data and actual which is the map value substring) are equal

### detectBlockTest
Inside this method, the detectedBlocks set will be added with a block if a block is detected. First, we create a dummy local player to make the system runs and does not throw null exception. Next is we place a block with worldprovider.setBlock -- and then we run the method and check if the detectedBlocks set is not null (filled with our block)

### updateTest
This updateTest is created to test if the timeSinceLastUpdate is increased by delta. To make the system runs, we create a dummy local player so that the localPlayer data is not null and does not throw a null exception

### initialise
This method should return the Detectors in not null condition. It's just running the method and check if Detectors is not null

### detectorTest
This detector test works most-likely like cave/easteregg detector. it places a new block at a current position and run the addDetector method. This test method runs it and check if the detector data is added to the map.
