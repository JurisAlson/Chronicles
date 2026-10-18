
import { useEffect, useRef, useState } from "react";
import "./ChronicleAssistant.css";

const API_URL =
  import.meta.env.VITE_CHRONICLE_API_URL ||
  "http://localhost:8080/api/chronicle-ai/ask";

function ChronicleAssistant() {
  const [isOpen, setIsOpen] = useState(false);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      messagesEndRef.current?.scrollIntoView({
        behavior: "smooth",
        block: "end",
      });
    }
  }, [messages, isOpen, isLoading]);

  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    }
  }, [isOpen]);

  async function handleSubmit(event) {
    event.preventDefault();

    const trimmedQuestion = question.trim();

    if (!trimmedQuestion || isLoading) return;

    setMessages((previous) => [
      ...previous,
      { role: "user", content: trimmedQuestion },
    ]);
    setQuestion("");
    setError("");
    setIsLoading(true);

    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ question: trimmedQuestion }),
      });

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        throw new Error(
          data?.message ||
            data?.error ||
            `The server returned HTTP ${response.status}.`
        );
      }

      if (typeof data?.answer !== "string") {
        throw new Error("The server returned an unexpected response.");
      }

      setMessages((previous) => [
        ...previous,
        {
          role: "assistant",
          content: data.answer,
          citations: Array.isArray(data.citations)
            ? data.citations
            : [],
          status: data.status,
        },
      ]);
    } catch (err) {
      setError(
        err.message === "Failed to fetch"
          ? "Couldn't connect to Chronicle's backend. Check that Spring Boot is running and that CORS allows the frontend origin."
          : err.message || "Something went wrong. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  }

  function startNewConversation() {
    if (isLoading) return;

    setMessages([]);
    setError("");
    setQuestion("");
    inputRef.current?.focus();
  }

  return (
    <div className="chronicle-assistant">
      {isOpen && (
        <section
          className="chronicle-chat"
          role="dialog"
          aria-label="Chronicle AI assistant"
          aria-modal="false"
        >
          <header className="chronicle-chat__header">
            <div className="chronicle-chat__identity">
              <div className="chronicle-chat__logo" aria-hidden="true">
                C
              </div>

              <div>
                <h2>Chronicle AI</h2>
                <p>Historical research assistant</p>
              </div>
            </div>

            <div className="chronicle-chat__actions">
              <button
                type="button"
                className="chronicle-icon-button"
                onClick={startNewConversation}
                disabled={isLoading}
                aria-label="Start a new conversation"
                title="New conversation"
              >
                +
              </button>

              <button
                type="button"
                className="chronicle-icon-button"
                onClick={() => setIsOpen(false)}
                aria-label="Minimize Chronicle AI"
                title="Minimize"
              >
                −
              </button>
            </div>
          </header>

          <div className="chronicle-chat__messages" aria-live="polite">
            {messages.length === 0 && (
              <div className="chronicle-chat__welcome">
                <span className="chronicle-chat__eyebrow">
                  YOUR RESEARCH COMPANION
                </span>

                <h3>History, with context.</h3>

                <p>
                  Ask a historical question and I'll look for supporting
                  evidence in Chronicle's knowledge base.
                </p>

                <button
                  type="button"
                  className="chronicle-suggestion"
                  onClick={() =>
                    setQuestion("Why did the Roman Republic collapse?")
                  }
                >
                  Why did the Roman Republic collapse?
                  <span aria-hidden="true">↗</span>
                </button>
              </div>
            )}

            {messages.map((message, index) => (
              <article
                className={`chronicle-message chronicle-message--${message.role}`}
                key={`${index}-${message.role}`}
              >
                <span className="chronicle-message__label">
                  {message.role === "user" ? "YOU" : "CHRONICLE AI"}
                </span>

                <p className="chronicle-message__content">
                  {message.content}
                </p>

                {message.role === "assistant" &&
                  message.citations.length > 0 && (
                    <details className="chronicle-citations">
                      <summary>
                        Sources ({message.citations.length})
                      </summary>

                      <ol>
                        {message.citations.map((citation, citationIndex) => (
                          <li key={`${citationIndex}-${citation}`}>
                            {citation}
                          </li>
                        ))}
                      </ol>
                    </details>
                  )}

                {message.role === "assistant" &&
                  message.status === "insufficient_evidence" && (
                    <p className="chronicle-message__notice">
                      The available sources may not be enough to answer this
                      question reliably.
                    </p>
                  )}
              </article>
            ))}

            {isLoading && (
              <div
                className="chronicle-chat__loading"
                role="status"
                aria-label="Chronicle AI is researching"
              >
                <span />
                <span />
                <span />
                <p>Searching historical sources...</p>
              </div>
            )}

            {error && (
              <div className="chronicle-chat__error" role="alert">
                <p>{error}</p>
                <button
                  type="button"
                  onClick={() => inputRef.current?.focus()}
                >
                  Try again
                </button>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          <form
            className="chronicle-chat__composer"
            onSubmit={handleSubmit}
          >
            <label className="chronicle-sr-only" htmlFor="chronicle-question">
              Ask Chronicle AI a question
            </label>

            <textarea
              id="chronicle-question"
              ref={inputRef}
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Ask a historical question..."
              rows={2}
              maxLength={4000}
              disabled={isLoading}
              onKeyDown={(event) => {
                if (
                  event.key === "Enter" &&
                  !event.shiftKey &&
                  !event.nativeEvent.isComposing
                ) {
                  event.preventDefault();
                  event.currentTarget.form?.requestSubmit();
                }
              }}
            />

            <div className="chronicle-chat__composer-footer">
              <span>Enter to send · Shift + Enter for a new line</span>

              <button
                type="submit"
                disabled={!question.trim() || isLoading}
                aria-label="Send question"
              >
                ↑
              </button>
            </div>
          </form>
        </section>
      )}

      <button
        type="button"
        className={`chronicle-fab ${isOpen ? "chronicle-fab--open" : ""}`}
        onClick={() => setIsOpen((previous) => !previous)}
        aria-label={isOpen ? "Close Chronicle AI" : "Open Chronicle AI"}
        aria-expanded={isOpen}
        title="Ask Chronicle AI"
      >
        {isOpen ? (
          <span aria-hidden="true">×</span>
        ) : (
          <>
            <span className="chronicle-fab__letter" aria-hidden="true">
              C
            </span>
            <span className="chronicle-fab__tooltip">Ask Chronicle AI</span>
          </>
        )}
      </button>
    </div>
  );
}

export default ChronicleAssistant;
