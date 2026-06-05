# Voice AI

A self-hosted voice processing stack running on Docker.

## Services

### Whisper ASR

Automatic Speech Recognition (ASR) powered by [OpenAI Whisper](https://github.com/openai/whisper), served via the [`onerahmet/openai-whisper-asr-webservice`](https://github.com/ahmetoner/whisper-asr-webservice) image.

- **URL:** `http://localhost:9000`
- **API Docs (Swagger):** `http://localhost:9000/docs`

#### Configuration

| Environment Variable | Default | Description |
|---|---|---|
| `ASR_MODEL` | `base` | Whisper model size: `tiny`, `base`, `small`, `medium`, `large-v3` |
| `ASR_ENGINE` | `openai_whisper` | Inference engine: `openai_whisper` or `faster_whisper` |

> **Note:** Larger models (`medium`, `large-v3`) are more accurate but require more RAM and are slower on CPU.

#### GPU Support (NVIDIA)

GPU acceleration is available but disabled by default. To enable it, uncomment the `deploy` block in `docker-compose.yaml`:

```yaml
deploy:
  resources:
    reservations:
      devices:
        - driver: nvidia
          count: all
          capabilities: [gpu]
```

## Getting Started

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/)
- (Optional) NVIDIA GPU with [NVIDIA Container Toolkit](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html) for GPU acceleration

### Start the stack

```bash
docker compose up -d
```

### Stop the stack

```bash
docker compose down
```

## Usage

### Transcribe audio via API

```bash
curl -X POST "http://localhost:9000/asr" \
  -F "audio_file=@/path/to/audio.mp3" \
  -F "output=json"
```

### Supported output formats

- `json` — structured JSON with word-level timestamps
- `text` — plain text transcript
- `srt` — SubRip subtitle format
- `vtt` — WebVTT subtitle format
- `tsv` — tab-separated values
