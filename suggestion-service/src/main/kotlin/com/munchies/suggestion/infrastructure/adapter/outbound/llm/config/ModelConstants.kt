package com.munchies.suggestion.infrastructure.adapter.outbound.llm.config

import dev.langchain4j.model.ollama.OllamaChatModel
import java.time.Duration

object ModelConstants {
  val OLLAMA_BASE_URL: String = System.getenv("OLLAMA_BASE_URL") ?: "http://localhost:11435"

  const val CHAT_MODEL_SMOLLM: String = "smollm2:135m"
  const val CHAT_MODEL_QWEN: String = "qwen3.5:0.8b"
  const val EMBEDDING_MODEL: String = "mxbai-embed-large"

  const val MAX_PREDICT_TOKENS: Int = 128
  const val DEFAULT_TEMPERATURE: Double = 0.3
  const val DEFAULT_TIMEOUT: Int = 10

  val DEFAULT_MODEL =
    OllamaChatModel.builder()
      .baseUrl(OLLAMA_BASE_URL)
      .modelName(CHAT_MODEL_SMOLLM)
      .temperature(DEFAULT_TEMPERATURE)
      .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT.toLong()))
      .build()
}
