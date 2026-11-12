#!/bin/sh
set -e

# Start Ollama daemon in background
/bin/ollama serve &
pid=$!

echo "Waiting for Ollama daemon to become ready..."
while ! curl -s http://localhost:11435/api/tags > /dev/null 2>&1; do
  sleep 1
done

MODEL="${OLLAMA_MODEL:-smollm2:135m}"
echo "Ensuring model '$MODEL' is pulled..."
ollama pull "$MODEL"

# Keep the background daemon in the foreground
wait $pid
