// preciso explicar tipagem de interfaces?

export interface Unit {
  id: number;
  name: string;
  city: string;
  region: string;
  fullKitchen: boolean;
}

export interface ProductView {
  productId: number;
  name: string;
  price: number;
  stockQuantity: number;
}

export type OrderStatus =
  | 'CRIADO'
  | 'AGUARDANDO_PAGAMENTO'
  | 'PAGO'
  | 'EM_PREPARO'
  | 'PRONTO'
  | 'ENTREGUE'
  | 'CANCELADO';

export interface OrderItemView {
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface OrderResponse {
  id: number;
  unitId: number;
  channel: string;
  status: OrderStatus;
  total: number;
  items: OrderItemView[];
  paymentReference: string | null;
}
