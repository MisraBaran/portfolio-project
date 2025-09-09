import { useEffect, useState } from "react";
import {
  ResponsiveContainer,
  PieChart, Pie, Cell, Tooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
} from "recharts";

// Backend adresi: .env ile VITE_API_URL verirsen onu kullanır, yoksa localhost
const API = (import.meta.env && import.meta.env.VITE_API_URL) || "http://localhost:8080/api";

function fmtTRY(n) {
  return new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(Number(n || 0));
}

export default function App() {
  // Auth state
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [mode, setMode] = useState("login"); // 'login' | 'register'
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // Portfolio state
  const [items, setItems] = useState([]);
  const [symbol, setSymbol] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [buyPrice, setBuyPrice] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Yetkili fetch
  const authFetch = async (url, options = {}) => {
    const headers = {
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
    const res = await fetch(url, { ...options, headers });
    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || `HTTP ${res.status}`);
    }
    return res;
  };

  // Listeyi yükle
  const load = async () => {
    if (!token) return;
    try {
      setLoading(true);
      const res = await authFetch(`${API}/stocks`);
      const data = await res.json();
      setItems(data || []);
      setError("");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  // Hisse ekle
  const addStock = async (e) => {
    e.preventDefault();
    try {
      await authFetch(`${API}/stocks`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          symbol,
          quantity: Number(quantity),
          buyPrice: Number(buyPrice),
        }),
      });
      setSymbol("");
      setQuantity(1);
      setBuyPrice("");
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  // Hisse sil
  const remove = async (id) => {
    try {
      await authFetch(`${API}/stocks/${id}`, { method: "DELETE" });
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  // Giriş/Kayıt
  const doAuth = async (path) => {
    try {
      const res = await fetch(`${API}/auth/${path}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!res.ok) throw new Error(await res.text());
      const data = await res.json(); // { token: "..." }
      localStorage.setItem("token", data.token);
      setToken(data.token);
      setEmail("");
      setPassword("");
      setError("");
      await load();
    } catch (e) {
      setError(e.message || "Giriş/Kayıt hatası");
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken("");
    setItems([]);
  };

  // 10 sn'de bir yenile
  useEffect(() => {
    if (!token) return;
    load();
    const id = setInterval(load, 10_000);
    return () => clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  // Auth ekranı
  if (!token) {
    return (
      <div style={{ maxWidth: 420, margin: "40px auto", fontFamily: "system-ui" }}>
        <h1>Portföy • Giriş</h1>
        <div style={{ marginBottom: 12 }}>
          <button
            onClick={() => setMode("login")}
            style={{ marginRight: 8, fontWeight: mode === "login" ? "bold" : "normal" }}
          >
            Giriş
          </button>
          <button
            onClick={() => setMode("register")}
            style={{ fontWeight: mode === "register" ? "bold" : "normal" }}
          >
            Kayıt
          </button>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <input
            placeholder="E-posta"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoFocus
          />
          <input
            type="password"
            placeholder="Şifre (min 6)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button onClick={() => doAuth(mode)}>{mode === "login" ? "Giriş Yap" : "Kayıt Ol"}</button>
          {error && <div style={{ color: "crimson" }}>{error}</div>}
          <p style={{ color: "#666", fontSize: 12 }}>
            Test için ör: <code>sude2@example.com / 123456</code>
          </p>
        </div>
      </div>
    );
  }

  // Toplamlar
  const totals = items.reduce(
    (acc, s) => {
      const val = Number(s.currentPrice) * Number(s.quantity);
      const pnl = (Number(s.currentPrice) - Number(s.buyPrice)) * Number(s.quantity);
      acc.value += val;
      acc.pnl += pnl;
      return acc;
    },
    { value: 0, pnl: 0 }
  );

  // --- GRAFİK VERİSİ: sembol birleştirme ve renkler ---
  const agg = Object.values(
    (items || []).reduce((acc, s) => {
      const sym = s.symbol;
      const val = Number(s.currentPrice) * Number(s.quantity);
      const pnl = (Number(s.currentPrice) - Number(s.buyPrice)) * Number(s.quantity);
      if (!acc[sym]) acc[sym] = { symbol: sym, value: 0, pnl: 0 };
      acc[sym].value += val;
      acc[sym].pnl += pnl;
      return acc;
    }, {})
  );
  const colors = ["#2563eb", "#f59e0b", "#10b981", "#ef4444", "#8b5cf6", "#14b8a6", "#e11d48", "#84cc16"];

  // Portföy ekranı
  return (
    <div style={{ maxWidth: 960, margin: "24px auto", fontFamily: "system-ui" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline" }}>
        <h1>Portföyüm</h1>
        <div>
          <span style={{ color: "#666", marginRight: 12 }}>{email || "Oturum açık"}</span>
          <button onClick={logout}>Çıkış</button>
        </div>
      </div>

      {error && <div style={{ color: "crimson", margin: "8px 0" }}>{error}</div>}

      <form onSubmit={addStock} style={{ display: "flex", gap: 8, marginBottom: 16, flexWrap: "wrap" }}>
        <input
          placeholder="Sembol (ASELS)"
          value={symbol}
          onChange={(e) => setSymbol(e.target.value.trim().toUpperCase())}
          required
        />
        <input
          type="number"
          min="1"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          required
        />
        <input
          type="number"
          step="0.01"
          min="0.01"
          placeholder="Alış Fiyatı"
          value={buyPrice}
          onChange={(e) => setBuyPrice(e.target.value)}
          required
        />
        <button>Ekle</button>
      </form>

      {loading ? (
        <p>Yükleniyor…</p>
      ) : items.length === 0 ? (
        <p>Henüz kayıt yok. Yukarıdan bir hisse ekle.</p>
      ) : (
        <table border="1" cellPadding="8" style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              <th>Sembol</th>
              <th>Adet</th>
              <th>Alış</th>
              <th>Son</th>
              <th>Değer</th>
              <th>Kâr/Zarar</th>
              <th>İşlem</th>
            </tr>
          </thead>
          <tbody>
            {items.map((s) => {
              const value = Number(s.currentPrice) * Number(s.quantity);
              const pnl = (Number(s.currentPrice) - Number(s.buyPrice)) * Number(s.quantity);
              return (
                <tr key={s.id}>
                  <td>{s.symbol}</td>
                  <td>{s.quantity}</td>
                  <td>{fmtTRY(s.buyPrice)}</td>
                  <td>{fmtTRY(s.currentPrice)}</td>
                  <td>{fmtTRY(value)}</td>
                  <td style={{ color: pnl >= 0 ? "green" : "crimson" }}>{fmtTRY(pnl)}</td>
                  <td>
                    <button onClick={() => remove(s.id)}>Sil</button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      <p style={{ marginTop: 12 }}>
        Toplam Değer: <b>{fmtTRY(totals.value)}</b> — Toplam K/Z:{" "}
        <b style={{ color: totals.pnl >= 0 ? "green" : "crimson" }}>{fmtTRY(totals.pnl)}</b>
      </p>

      {/* === Grafikler: tablonun ALTINA eklendi === */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginTop: 16 }}>
        {/* Portföy Dağılımı (Pasta) */}
        <div style={{ border: "1px solid #e5e7eb", borderRadius: 8, padding: 12 }}>
          <h3 style={{ marginTop: 0 }}>Portföy Dağılımı</h3>
          {agg.length === 0 ? (
            <p>Grafik için kayıt ekleyin.</p>
          ) : (
            <div style={{ height: 260 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={agg} dataKey="value" nameKey="symbol" innerRadius={50} outerRadius={90}>
                    {agg.map((_, i) => (
                      <Cell key={i} fill={colors[i % colors.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v, n) => (n === "value" ? fmtTRY(v) : v)} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        {/* Sembol Bazlı K/Z (Bar) */}
        <div style={{ border: "1px solid #e5e7eb", borderRadius: 8, padding: 12 }}>
          <h3 style={{ marginTop: 0 }}>Sembol Bazlı K/Z</h3>
          {agg.length === 0 ? (
            <p>Grafik için kayıt ekleyin.</p>
          ) : (
            <div style={{ height: 260 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={agg} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="symbol" />
                  <YAxis />
                  <Tooltip formatter={(v) => fmtTRY(v)} />
                  <Bar dataKey="pnl">
                    {agg.map((e, i) => (
                      <Cell key={i} fill={e.pnl >= 0 ? "#16a34a" : "#dc2626"} />
                    ))}
                  </Bar>
                  <Legend />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

