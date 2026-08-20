import type { OrderResponse, ProductView, Unit } from './types';

const BASE_URL = 'http://localhost:8080/api';

// api request genérica
async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(`Falha em ${path}: ${response.status} ${body}`);
  }
  return response.json() as Promise<T>;
}

// funcções
export const api = {
  listUnits: () => request<Unit[]>('/units'),

  menuByUnit: (unitId: number) => request<ProductView[]>(`/units/${unitId}/menu`),

  createOrder: (unitId: number, channel: string, items: { productId: number; quantity: number }[]) =>
    request<OrderResponse>('/orders', {
      method: 'POST',
      body: JSON.stringify({ unitId, channel, items }),
    }),

  getOrder: (orderId: number) => request<OrderResponse>(`/orders/${orderId}`),

  // notificação mock payment
  simulatePaymentConfirmation: (externalReference: string, approved: boolean) =>
    request<OrderResponse>('/payments/webhook', {
      method: 'POST',
      body: JSON.stringify({ externalReference, approved }),
    }),
};
