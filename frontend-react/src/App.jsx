import { useEffect, useMemo, useState } from "react";
import {
  createPayment,
  failPayment,
  getPaymentHistory,
  listPayments,
  transitionPayment
} from "./api";

const initialForm = {
  sourceAccount: "",
  destinationAccount: "",
  amount: "",
  currency: "USD",
  reference: ""
};

export default function App() {
  const [form, setForm] = useState(initialForm);
  const [payments, setPayments] = useState([]);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [history, setHistory] = useState([]);
  const [statusFilter, setStatusFilter] = useState("");
  const [accountInput, setAccountInput] = useState("");
  const [message, setMessage] = useState("Ready");
  const [messageType, setMessageType] = useState("info");
  const [loading, setLoading] = useState(false);

  const canTransition = useMemo(() => {
    if (!selectedPayment) {
      return {};
    }
    return {
      validate: selectedPayment.status === "CREATED",
      send: selectedPayment.status === "VALIDATED",
      complete: selectedPayment.status === "SENT",
      fail: ["CREATED", "VALIDATED", "SENT"].includes(selectedPayment.status)
    };
  }, [selectedPayment]);

  useEffect(() => {
    void loadPayments();
  }, []);

  function setFeedback(text, type = "info") {
    setMessage(text);
    setMessageType(type);
  }

  async function withLoading(action) {
    try {
      setLoading(true);
      await action();
    } catch (error) {
      setFeedback(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  function onFormChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function parseAmount(amountValue) {
    const amount = Number.parseFloat(amountValue);
    if (Number.isNaN(amount)) {
      throw new Error("Amount must be a valid number");
    }
    return amount;
  }

  async function handleCreatePayment(event) {
    event.preventDefault();

    if (form.sourceAccount === form.destinationAccount) {
      setFeedback("Source and destination accounts must be different", "error");
      return;
    }

    await withLoading(async () => {
      // Auto-generate a unique idempotency key so users never have to supply one
      const idempotencyKey = crypto.randomUUID();
      const payload = { ...form, amount: parseAmount(form.amount), idempotencyKey };
      const payment = await createPayment(payload);
      setSelectedPayment(payment);
      setAccountInput(payment.sourceAccount);
      setHistory(await getPaymentHistory(payment.id));
      setFeedback(`Payment saved with id ${payment.id}`, "success");
      setForm((prev) => ({ ...prev, amount: "", reference: "" }));
      const data = await listPayments(statusFilter || "", accountInput.trim());
      setPayments(data);
    });
  }

  async function loadPayments(status = statusFilter) {
    await withLoading(async () => {
      const data = await listPayments(status || "", accountInput.trim());
      setPayments(data);
      setFeedback(`Loaded ${data.length} payment(s)`, "info");
    });
  }

  async function loadPaymentByAccount() {
    if (!accountInput.trim()) {
      setFeedback("Enter an account number first", "error");
      return;
    }

    await withLoading(async () => {
      const data = await listPayments(statusFilter || "", accountInput.trim());
      setPayments(data);
      if (data.length === 0) {
        setSelectedPayment(null);
        setHistory([]);
        setFeedback(`No payments found for account ${accountInput.trim()}`, "info");
        return;
      }
      const latest = data[0];
      setSelectedPayment(latest);
      setHistory(await getPaymentHistory(latest.id));
      setFeedback(`Loaded ${data.length} payment(s) for account ${accountInput.trim()}`, "success");
    });
  }

  async function doTransition(action) {
    if (!selectedPayment) {
      setFeedback("Load or create a payment first", "error");
      return;
    }

    await withLoading(async () => {
      const updated = await transitionPayment(selectedPayment.id, action);
      setSelectedPayment(updated);
      setHistory(await getPaymentHistory(updated.id));
      setFeedback(`Payment moved to ${updated.status}`, "success");
      const data = await listPayments(statusFilter || "", accountInput.trim());
      setPayments(data);
    });
  }

  async function doFail() {
    if (!selectedPayment) {
      setFeedback("Load or create a payment first", "error");
      return;
    }
    if (!canTransition.fail) {
      setFeedback("Current status cannot transition to FAILED", "error");
      return;
    }

    await withLoading(async () => {
      const updated = await failPayment(selectedPayment.id, {
        errorCode: "NETWORK_ERROR",
        errorMessage: "Manual failure from frontend"
      });
      setSelectedPayment(updated);
      setHistory(await getPaymentHistory(updated.id));
      setFeedback(`Payment moved to ${updated.status}`, "success");
      const data = await listPayments(statusFilter || "", accountInput.trim());
      setPayments(data);
    });
  }

  async function handleSelectPayment(payment) {
    await withLoading(async () => {
      setSelectedPayment(payment);
      setAccountInput(payment.sourceAccount);
      setHistory([]);
      const historyData = await getPaymentHistory(payment.id);
      setHistory(historyData);
      setFeedback(`Loaded ${historyData.length} history item(s) for payment ${payment.id}`, "info");
    });
  }

  return (
    <main className="app">
      <h1>Payments Processing Platform</h1>
      <p className="hint">Team SmartOG</p>

      <section className="card">
        <h2>Create Payment</h2>
        <form className="grid" onSubmit={handleCreatePayment}>
          <input name="sourceAccount" placeholder="Source Account (8-20 digits)" value={form.sourceAccount} onChange={onFormChange} pattern="[0-9]{8,20}" title="Source account must be 8-20 digits" required />
          <input name="destinationAccount" placeholder="Destination Account (8-20 digits)" value={form.destinationAccount} onChange={onFormChange} pattern="[0-9]{8,20}" title="Destination account must be 8-20 digits" required />
          <input name="amount" type="number" min="0.01" max="1000000" step="0.01" placeholder="Amount" value={form.amount} onChange={onFormChange} onWheel={(e) => e.preventDefault()} required />
          <select name="currency" value={form.currency} onChange={onFormChange}>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
          </select>
          <input name="reference" placeholder="Reference (optional)" value={form.reference} onChange={onFormChange} />
          <button disabled={loading} type="submit">{loading ? "Creating..." : "Create"}</button>
        </form>
      </section>

      <section className="card">
        <h2>Query Payment</h2>
        <div className="row">
          <input placeholder="Account Number" value={accountInput} onChange={(e) => setAccountInput(e.target.value)} />
          <button disabled={loading} onClick={loadPaymentByAccount}>Load by Account</button>
        </div>
      </section>

      <section className="card">
        <h2>Payment List</h2>
        <div className="row">
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">All Statuses</option>
            <option value="CREATED">CREATED</option>
            <option value="VALIDATED">VALIDATED</option>
            <option value="SENT">SENT</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="FAILED">FAILED</option>
          </select>
          <button disabled={loading} onClick={() => loadPayments(statusFilter)}>Refresh List</button>
        </div>
        <div className="list">
          {payments.map((payment) => (
            <button key={payment.id} className="list-item" onClick={() => void handleSelectPayment(payment)}>
              <strong>{payment.sourceAccount}</strong> -&gt; <strong>{payment.destinationAccount}</strong> | {payment.amount} {payment.currency} | {payment.status}
            </button>
          ))}
        </div>
      </section>

      <section className="card">
        <h2>Selected Payment</h2>
        {selectedPayment ? (
          <>
            <div className="detail-grid">
              <div className="detail-item">
                <span className="detail-label">Payment ID</span>
                <span>{selectedPayment.id}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Status</span>
                <span>{selectedPayment.status}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Amount</span>
                <span>{selectedPayment.amount} {selectedPayment.currency}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Source Account</span>
                <span>{selectedPayment.sourceAccount}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Destination Account</span>
                <span>{selectedPayment.destinationAccount}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Reference</span>
                <span>{selectedPayment.reference || "-"}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Settlement Reference</span>
                <span>{selectedPayment.settlementReference || "-"}</span>
              </div>
              <div className="detail-item detail-item-wide">
                <span className="detail-label">Idempotency Key</span>
                <span>{selectedPayment.idempotencyKey || "-"}</span>
              </div>
            </div>
            <div className="row">
              <button disabled={loading || !canTransition.validate} onClick={() => doTransition("validate")}>Validate</button>
              <button disabled={loading || !canTransition.send} onClick={() => doTransition("send")}>Send</button>
              <button disabled={loading || !canTransition.complete} onClick={() => doTransition("complete")}>Complete</button>
              <button className="danger" disabled={loading || !canTransition.fail} onClick={doFail}>Fail</button>
            </div>
            <p className={`message ${messageType}`}>{loading ? "Loading..." : message}</p>
          </>
        ) : (
          <p>No payment selected</p>
        )}
      </section>

      <section className="card">
        <h2>Status History</h2>
        {history.length === 0 ? (
          <p>No history</p>
        ) : (
          <div className="history-list">
            {history.map((item, index) => (
              <div className="history-item" key={`${item.id ?? "history"}-${index}`}>
                <p className="history-transition">
                  <strong>{item.fromStatus ?? "-"}</strong> -&gt; <strong>{item.toStatus ?? item.status ?? "-"}</strong>
                </p>
                <p className="history-meta">{item.changedAt ?? item.createdAt ?? item.timestamp ?? "Unknown time"}</p>
              </div>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}


