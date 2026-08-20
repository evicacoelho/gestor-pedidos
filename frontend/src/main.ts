import { api } from './api';
import type { OrderResponse, ProductView, Unit } from './types';


const app = document.querySelector<HTMLDivElement>('#app')!;

const state: {
  units: Unit[];
  selectedUnit: Unit | null;
  menu: ProductView[];
  cart: Map<number, number>;
  currentOrder: OrderResponse | null;
} = {
  units: [],
  selectedUnit: null,
  menu: [],
  cart: new Map(),
  currentOrder: null,
};

// listar absolutamente todas as unidades
async function init() {
  state.units = await api.listUnits();
  render();
}

// selecionar uma unidade buscar o cardápio e resetar o carrinho
async function selectUnit(unit: Unit) {
  state.selectedUnit = unit;
  state.menu = await api.menuByUnit(unit.id);
  state.cart = new Map();
  render();
}

// adicionar remover item carrinho
function toggleCartItem(productId: number, delta: number) {
  const current = state.cart.get(productId) ?? 0;
  const next = Math.max(0, current + delta);
  if (next === 0) {
    state.cart.delete(productId);
  } else {
    state.cart.set(productId, next);
  }
  render();
}

// criar pedido
async function submitOrder() {
  if (!state.selectedUnit || state.cart.size === 0) return;
  const items = Array.from(state.cart.entries()).map(([productId, quantity]) => ({ productId, quantity }));
  state.currentOrder = await api.createOrder(state.selectedUnit.id, 'APP', items);
  render();
}

// simulação pagamento
async function simulatePayment(approved: boolean) {
  if (!state.currentOrder?.paymentReference) return;
  state.currentOrder = await api.simulatePaymentConfirmation(state.currentOrder.paymentReference, approved);
  render();
}

// atualizar status
async function refreshOrderStatus() {
  if (!state.currentOrder) return;
  state.currentOrder = await api.getOrder(state.currentOrder.id);
  render();
}

// renderizações
// screen
function render() {
  app.innerHTML = '';
  app.appendChild(renderUnitPicker());
  if (state.selectedUnit) {
    app.appendChild(renderMenu());
  }
  if (state.currentOrder) {
    app.appendChild(renderOrderTracking());
  }
}

// unit picker
function renderUnitPicker(): HTMLElement {
  const section = document.createElement('section');
  const title = document.createElement('h2');
  title.textContent = 'Escolha a unidade';
  section.appendChild(title);

  const list = document.createElement('ul');
  for (const unit of state.units) {
    const item = document.createElement('li');
    const button = document.createElement('button');
    button.textContent = `${unit.name} (${unit.city})`;
    button.disabled = state.selectedUnit?.id === unit.id;
    button.onclick = () => selectUnit(unit);
    item.appendChild(button);
    list.appendChild(item);
  }
  section.appendChild(list);
  return section;
}

// menu
function renderMenu(): HTMLElement {
  const section = document.createElement('section');
  const title = document.createElement('h2');
  title.textContent = `Cardapio - ${state.selectedUnit!.name}`;
  section.appendChild(title);

  const list = document.createElement('ul');
  for (const product of state.menu) {
    const quantity = state.cart.get(product.productId) ?? 0;
    const item = document.createElement('li');
    item.textContent = `${product.name} - R$ ${product.price.toFixed(2)} (estoque: ${product.stockQuantity}) `;

    const minus = document.createElement('button');
    minus.textContent = '-';
    minus.onclick = () => toggleCartItem(product.productId, -1);

    const qtySpan = document.createElement('span');
    qtySpan.textContent = ` ${quantity} `;

    const plus = document.createElement('button');
    plus.textContent = '+';
    plus.onclick = () => toggleCartItem(product.productId, 1);

    item.append(minus, qtySpan, plus);
    list.appendChild(item);
  }
  section.appendChild(list);

  const orderButton = document.createElement('button');
  orderButton.textContent = 'Fazer pedido';
  orderButton.disabled = state.cart.size === 0;
  orderButton.onclick = () => submitOrder();
  section.appendChild(orderButton);

  return section;
}

// acompanhar order
function renderOrderTracking(): HTMLElement {
  const order = state.currentOrder!;
  const section = document.createElement('section');
  const title = document.createElement('h2');
  title.textContent = `Pedido #${order.id} - status: ${order.status}`;
  section.appendChild(title);

  const total = document.createElement('p');
  total.textContent = `Total: R$ ${order.total.toFixed(2)}`;
  section.appendChild(total);

  if (order.status === 'AGUARDANDO_PAGAMENTO') {
    const note = document.createElement('p');
    note.textContent = 'Aguardando confirmacao do gateway de pagamento (simule abaixo):';
    section.appendChild(note);

    const approve = document.createElement('button');
    approve.textContent = 'Simular pagamento aprovado';
    approve.onclick = () => simulatePayment(true);

    const decline = document.createElement('button');
    decline.textContent = 'Simular pagamento recusado';
    decline.onclick = () => simulatePayment(false);

    section.append(approve, decline);
  }

  const refresh = document.createElement('button');
  refresh.textContent = 'Atualizar status';
  refresh.onclick = () => refreshOrderStatus();
  section.appendChild(refresh);

  return section;
}

init();
