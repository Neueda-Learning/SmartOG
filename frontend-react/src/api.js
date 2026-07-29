const API_BASE_URL = "http://localhost:8080/api/payments";

function parseErrorMessage(rawText, status) {
  if (!rawText) {
    return `Request failed with status ${status}`;
  }

  try {
    const parsed = JSON.parse(rawText);
    if (parsed.message && parsed.errorCode) {
      return `${parsed.errorCode}: ${parsed.message}`;
    }
    return parsed.message || rawText;
  } catch {
    return rawText;
  }
}

async function request(path = "", options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(parseErrorMessage(text, response.status));
  }

  return response.json();
}

export function createPayment(payload) {
  return request("", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function listPayments(status, accountNumber) {
  const params = new URLSearchParams();
  if (status) {
    params.set("status", status);
  }
  if (accountNumber) {
    params.set("accountNumber", accountNumber);
  }
  const query = params.toString() ? `?${params.toString()}` : "";
  return request(query);
}

export function getPayment(paymentId) {
  return request(`/${paymentId}`);
}

export function getPaymentHistory(paymentId) {
  return request(`/${paymentId}/history`);
}

export function transitionPayment(paymentId, action) {
  return request(`/${paymentId}/${action}`, { method: "POST" });
}

export function failPayment(paymentId, payload) {
  return request(`/${paymentId}/fail`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}


