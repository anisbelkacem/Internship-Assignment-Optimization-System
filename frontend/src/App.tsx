import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const [message, setMessage] = useState<string>("Loading...");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await fetch("/api/hello");
        if (!res.ok) {
          throw new Error(`Backend error: ${res.status}`);
        }
        const text = await res.text();
        setMessage(text);
      } catch (err) {
        console.error(err);
        setError(err instanceof Error? err.message ?? "Unknown error" : "Unknown error");
      }
    };

    load();
  }, []);

  return (
    <div style={{ fontFamily: "sans-serif", padding: "2rem" }}>
      <h1>Hello from React + TypeScript!</h1>
      <p>This text is rendered by the frontend.</p>

      <h2>Backend says:</h2>
      {error ? (
        <p style={{ color: "red" }}>Error: {error}</p>
      ) : (
        <p>
          <code>{message}</code>
        </p>
      )}

      <p>
        Backend endpoint: <code>GET /api/hello</code>
      </p>
    </div>
  );
}

export default App;
