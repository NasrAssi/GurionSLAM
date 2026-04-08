# GurionSLAM

A concurrent, microservices-based robot **Simultaneous Localization and Mapping (SLAM)** simulation written in Java. The system fuses data from multiple sensors — cameras, LiDAR, and GPS/IMU — across parallel threads using a custom message-bus framework to build a real-time global landmark map.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Output](#output)
- [Running the Tests](#running-the-tests)
- [Technologies](#technologies)

---

## Overview

GurionSLAM simulates a robot navigating an environment equipped with:

- **Camera sensors** — detect objects and assign IDs/descriptions
- **LiDAR sensors** — generate point clouds (3D coordinates) for each detected object
- **GPS/IMU** — tracks the robot's position (pose) at each tick

All sensors run as independent microservices on separate threads. A central **FusionSLAM** engine receives their outputs, transforms local coordinates into a global frame using the robot's current pose, and builds a landmark map of the environment.

---

## Architecture

The system is built on a custom publish-subscribe **MessageBus** (singleton) that connects all microservices:

```
TimeService  ──TickBroadcast──►  CameraService
                                  LiDarService
                                  PoseService

CameraService ──DetectObjectsEvent──► LiDarService
LiDarService  ──TrackedObjectsEvent──► FusionSlamService
PoseService   ──PoseEvent──────────► FusionSlamService
```

- **Events** are delivered to exactly one subscriber (round-robin load balancing)
- **Broadcasts** are delivered to all subscribers simultaneously
- Each service runs its own message loop — fully non-blocking and thread-safe

### Microservices

| Service | Role |
|---|---|
| `TimeService` | Global clock — broadcasts a tick every N milliseconds |
| `CameraService` | Reads camera data, sends `DetectObjectsEvent` per detected frame |
| `LiDarService` | Receives camera detections, matches LiDAR cloud points, sends `TrackedObjectsEvent` |
| `PoseService` | Reads GPS/IMU data, sends `PoseEvent` per tick |
| `FusionSlamService` | Transforms tracked objects into global coordinates and updates the landmark map |

---

## Project Structure

```
src/
├── main/java/bgu/spl/mics/
│   ├── MessageBus.java          # Interface for the message bus
│   ├── MessageBusImpl.java      # Thread-safe singleton implementation
│   ├── MicroService.java        # Abstract base for all services
│   ├── Future.java              # Thread-safe result container
│   ├── application/
│   │   ├── GurionRockRunner.java        # Main entry point
│   │   ├── messages/                    # Event & broadcast message types
│   │   ├── objects/                     # Domain model (Camera, LiDAR, FusionSlam, ...)
│   │   └── services/                    # All microservice implementations
└── test/java/bgu/spl/mics/
    ├── FutureTest.java
    ├── MessageBusImplTest.java
    └── application/objects/
        ├── CameraTest.java
        └── FusionSlamTest.java

example_input/                   # Sample simulation input
example_input_2/                 # Second sample input
example_input_with_error/        # Sample with a sensor crash scenario
```

---

## How It Works

1. **Startup** — `GurionRockRunner` parses the config file, loads sensor data, creates all services, and starts them on separate threads.
2. **Ticking** — `TimeService` fires a `TickBroadcast` every N milliseconds. All sensors react to each tick.
3. **Detection pipeline**:
   - Camera detects objects at tick `T` and sends them at tick `T + cameraFrequency`
   - LiDAR receives the detection, matches it against its cloud point database, and sends tracked objects at tick `T + lidarFrequency`
4. **Fusion** — `FusionSlamService` holds tracked objects until a matching robot pose arrives for the same timestamp, then applies a 2D rigid-body transformation (rotation + translation) to convert local sensor coordinates into the global map frame.
5. **Landmark update** — If a landmark already exists, its coordinates are averaged with the new observation. New landmarks are added directly.
6. **Termination** — Each sensor signals done via `TerminatedBroadcast`. When all sensors have terminated, FusionSLAM finalises the map and the simulation ends.
7. **Error handling** — If a camera detects an `ERROR` entry, it broadcasts a `CrashedBroadcast`. All services shut down and an error report is written instead of the normal output.

---

## Getting Started

### Prerequisites

- Java 8+
- Maven 3.6+

### Build

```bash
mvn compile
```

### Run

```bash
mvn package
java -jar target/spl225ass2-1.0.jar <path/to/configuration_file.json>
```

Example:

```bash
java -jar target/spl225ass2-1.0.jar example_input/configuration_file.json
```

---

## Configuration

The configuration file is a JSON file with the following structure:

```json
{
  "Cameras": {
    "CamerasConfigurations": [
      { "id": 1, "frequency": 2, "camera_key": "camera1" }
    ],
    "camera_datas_path": "./camera_data.json"
  },
  "LiDarWorkers": {
    "LidarConfigurations": [
      { "id": 1, "frequency": 2 }
    ],
    "lidars_data_path": "./lidar_data.json"
  },
  "poseJsonFile": "./pose_data.json",
  "TickTime": 1,
  "Duration": 30
}
```

| Field | Description |
|---|---|
| `TickTime` | Duration of each simulation tick, in seconds |
| `Duration` | Total number of ticks to run |
| `frequency` | Number of ticks of delay before a sensor sends its data |
| `camera_key` | Key used to look up this camera's data in `camera_datas_path` |

---

## Output

### Normal termination — `output.json`

```json
{
  "systemRuntime": 22,
  "numDetectedObjects": 13,
  "numTrackedObjects": 13,
  "numLandmarks": 7,
  "landMarks": {
    "Wall_1": {
      "id": "Wall_1",
      "description": "Wall",
      "coordinates": [{ "x": -1.26, "y": 3.12 }, { "x": -1.30, "y": 2.94 }]
    }
  }
}
```

### Crash — `error_output.json`

```json
{
  "error": "Camera disconnected",
  "faultySensor": "Camera1",
  "lastCamerasFrame": { "..." },
  "lastLiDarWorkerTrackersFrame": { "..." },
  "poses": [ "..." ],
  "statistics": { "..." }
}
```

---

## Running the Tests

```bash
mvn test
```

Test coverage includes:

- `FutureTest` — `get()`, `get(timeout)`, `resolve()`, `isDone()`
- `MessageBusImplTest` — register/unregister, event/broadcast routing, round-robin, blocking `awaitMessage`
- `CameraTest` — constructor, `detect()` at valid/invalid times, error detection
- `FusionSlamTest` — adding new landmarks, updating existing landmarks, averaging coordinates

---

## Technologies

- **Java 8** — core language
- **Java Concurrency** — `ConcurrentHashMap`, `LinkedBlockingQueue`, `AtomicInteger`, `volatile`
- **Gson 2.11** — JSON parsing and serialisation
- **JUnit Jupiter 5** — unit testing
- **Maven** — build and dependency management
