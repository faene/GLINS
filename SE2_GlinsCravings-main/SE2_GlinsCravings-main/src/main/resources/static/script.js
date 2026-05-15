// ============================================================
//  GLINS CRAVINGS — Backend-Connected App State Manager
// ============================================================

const API_BASE = '/api';

// ── Session (current user from backend) ──
function requireLogin() {
    const user = getCurrentUser();
    if (!user) window.location.href = 'login.html';
}

function getCurrentUser() {
    try { 
        const r = sessionStorage.getItem('glinsUser'); 
        return r ? JSON.parse(r) : null; 
    } catch { 
        return null; 
    }
}

function setCurrentUser(user) {
    if (user) sessionStorage.setItem('glinsUser', JSON.stringify(user));
    else sessionStorage.removeItem('glinsUser');
}

function logout() {
    setCurrentUser(null);
    window.location.href = 'login.html';
}

// ── API helpers ──
async function apiGet(path) {
    const res = await fetch(`${API_BASE}${path}`);
    if (!res.ok) throw new Error(`GET ${path} failed`);
    return res.json();
}

async function apiPost(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    // Returning both status and data for granular error handling
    const data = await res.json();
    return { ok: res.ok, data };
}

async function apiPut(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiDelete(path) {
    const res = await fetch(`${API_BASE}${path}`, { method: 'DELETE' });
    return res.json();
}

// ── Products ──
async function getInventory() {
    try { return await apiGet('/products'); }
    catch { return []; }
}

async function getCategories() {
    try { return await apiGet('/categories'); }
    catch { return []; }
}

async function populateCategorySelects() {
    const categories = await getCategories();
    const names = categories
        .map(category => category?.categoryName || category)
        .filter(Boolean);

    const selects = document.querySelectorAll('[data-category-select]');
    selects.forEach(select => {
        const currentValue = select.value;
        select.innerHTML = '<option value="">All Items</option>';
        names.forEach(name => {
            const option = document.createElement('option');
            option.value = name;
            option.textContent = name;
            select.appendChild(option);
        });
        if (currentValue && names.includes(currentValue)) {
            select.value = currentValue;
        }
    });

    const categorySelect = document.getElementById('newItemCategory');
    if (categorySelect) {
        const currentValue = categorySelect.value;
        categorySelect.innerHTML = '';
        names.forEach(name => {
            const option = document.createElement('option');
            option.value = name;
            option.textContent = name;
            categorySelect.appendChild(option);
        });
        if (currentValue && names.includes(currentValue)) {
            categorySelect.value = currentValue;
        } else if (names.length > 0) {
            categorySelect.value = names[0];
        }
    }
}

async function addInventoryItem(item) {
    const body = {
        productCode: item.id,
        name: item.name,
        category: item.category,
        price: item.price,
        stock: item.stock,
        expirationDate: item.expiration
    };
    const result = await apiPost('/products', body);
    return result.data;
}

async function updateInventoryItem(id, updates) {
    return await apiPut(`/products/${id}`, updates);
}

async function deleteInventoryItem(id) {
    return await apiDelete(`/products/${id}`);
}

async function getLowStockItems() {
    try { return await apiGet('/products/low-stock'); }
    catch { return []; }
}

async function getNearExpiryItems() {
    try { return await apiGet('/products/near-expiry'); }
    catch { return []; }
}

async function getAlertCount() {
    try {
        const [low, exp] = await Promise.all([getLowStockItems(), getNearExpiryItems()]);
        return low.length + exp.length;
    } catch { return 0; }
}

async function initDashboardSearch({ inputId, buttonId, targetPage }) {
    const input = document.getElementById(inputId);
    const button = document.getElementById(buttonId);
    if (!input || !button) return;

    const products = await getInventory();
    const lookup = new Map();

    products.forEach(product => {
        if (!product) return;
        const idKey = String(product.id);
        const codeKey = String(product.productCode || '').trim().toLowerCase();
        const nameKey = String(product.name || '').trim().toLowerCase();

        lookup.set(idKey, product);
        if (codeKey) lookup.set(codeKey, product);
        if (nameKey) lookup.set(nameKey, product);
    });

    function resolveProduct(rawValue) {
        const query = String(rawValue || '').trim().toLowerCase();
        if (!query) return null;

        if (lookup.has(query)) {
            return lookup.get(query);
        }

        const exactLabel = products.find(product => {
            const label = `${product.productCode || product.id} — ${product.name || ''}`.toLowerCase();
            return label === query;
        });
        if (exactLabel) return exactLabel;

        return products.find(product => {
            const code = String(product.productCode || '').toLowerCase();
            const name = String(product.name || '').toLowerCase();
            return code.includes(query) || name.includes(query);
        }) || null;
    }

    function runSearch() {
        const product = resolveProduct(input.value);
        if (!product) {
            showToast('Product not found. Try a product code or exact product name.', 'warning');
            return;
        }
        window.location.href = `${targetPage}?focusId=${product.id}`;
    }

    button.addEventListener('click', runSearch);
    input.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            runSearch();
        }
    });
}

// ── Orders ──
async function getOrders() {
    try { return await apiGet('/orders'); }
    catch { return []; }
}

async function addOrder(order) {
    const body = {
        customerName: order.customer,
        items: order.items.map(i => ({
            productName: i.name,
            quantity: i.qty,
            unitPrice: i.price
        }))
    };
    const result = await apiPost('/orders', body);
    return result.data;
}

// ── Reports ──
async function getSalesReport(startDate, endDate) {
    let params = '';
    if (startDate) params += `?startDate=${startDate}`;
    if (endDate) params += startDate ? `&endDate=${endDate}` : `?endDate=${endDate}`;
    try { return await apiGet(`/reports/sales${params}`); }
    catch { return { totalSalesToday: 0, ordersProcessed: 0, topSeller: 'N/A', totalOrders: 0 }; }
}

// ── UI Helpers ──
function computeStatus(item) {
    const today = new Date();
    const exp = new Date(item.expirationDate || item.expiration);
    const diff = Math.ceil((exp - today) / (1000 * 60 * 60 * 24));
    
    if (item.stock === 0) return 'OUT_OF_STOCK';
    if (item.stock <= 5)  return 'LOW_STOCK';
    if (diff <= 7)        return 'NEAR_EXPIRY';
    return 'AVAILABLE';
}

function statusBadgeHtml(status) {
    const labels = {
        'AVAILABLE': 'Available', 'LOW_STOCK': 'Low Stock',
        'OUT_OF_STOCK': 'Out of Stock', 'NEAR_EXPIRY': 'Near Expiry'
    };
    const colors = { 
        'AVAILABLE': '#28a745', 'LOW_STOCK': '#ffc107', 
        'OUT_OF_STOCK': '#dc3545', 'NEAR_EXPIRY': '#fd7e14' 
    };
    const bg = colors[status] || '#999';
    const tc = status === 'LOW_STOCK' ? '#000' : '#fff';
    const label = labels[status] || status;
    return `<span style="background:${bg};color:${tc};padding:3px 10px;border-radius:8px;font-size:12px;font-weight:800;">${label}</span>`;
}

function peso(n) { return `₱${parseFloat(n).toFixed(2)}`; }

function showToast(msg, type = 'info') {
    let t = document.getElementById('gc-toast');
    if (!t) {
        t = document.createElement('div'); t.id = 'gc-toast';
        t.style.cssText = 'position:fixed;bottom:30px;left:50%;transform:translateX(-50%);padding:14px 28px;border-radius:14px;font-weight:800;font-size:14px;box-shadow:0 6px 20px rgba(0,0,0,.25);z-index:9999;transition:opacity .4s;opacity:0;pointer-events:none;border:2px solid #000;min-width:260px;text-align:center;';
        document.body.appendChild(t);
    }
    const cols = { success: '#28a745', error: '#dc3545', info: '#B86B77', warning: '#ffc107' };
    t.style.background = cols[type] || cols.info; 
    t.style.color = type === 'warning' ? '#000' : '#fff';
    t.textContent = msg; 
    t.style.opacity = '1'; 
    clearTimeout(t._h);
    t._h = setTimeout(() => { t.style.opacity = '0'; }, 2800);
}

// ── Restock tracking (so notifications can hide recently-restocked items) ──
function _getRestockedSet() {
    try {
        const raw = localStorage.getItem('gc_restocked');
        return raw ? JSON.parse(raw) : [];
    } catch { return []; }
}

function markRestocked(id) {
    if (!id) return;
    try {
        const now = Date.now();
        const arr = _getRestockedSet();
        // arr is array of {id, ts}
        const exists = arr.find(r => r.id === id);
        if (!exists) {
            arr.push({ id: id, ts: now });
            localStorage.setItem('gc_restocked', JSON.stringify(arr));
            try { window.dispatchEvent(new Event('gc_restocked')); } catch(e){}
        } else {
            // update timestamp
            exists.ts = now;
            localStorage.setItem('gc_restocked', JSON.stringify(arr));
        }
    } catch (e) { console.warn('markRestocked failed', e); }
}

function _saveRestocked(arr) {
    try { localStorage.setItem('gc_restocked', JSON.stringify(arr)); } catch(e){}
}

// Remove entries older than TTL or entries that are currently low-stock again
function cleanupRestocked(currentLowStockIds = []) {
    try {
        const TTL = 1000 * 60 * 60 * 12; // 12 hours
        const now = Date.now();
        let arr = _getRestockedSet();
        arr = arr.filter(r => {
            if (!r || !r.id) return false;
            if (currentLowStockIds.includes(r.id)) return false; // back to low stock -> remove flag
            if ((now - (r.ts || 0)) > TTL) return false; // expired
            return true;
        });
        _saveRestocked(arr);
        return arr.map(x => x.id);
    } catch(e) { return []; }
}

function isRestocked(id) {
    try {
        const arr = _getRestockedSet();
        return arr.some(r => r.id === id);
    } catch { return false; }
}

// ── AUTHENTICATION LOGIC ──

async function handleLogin() {
    const u = document.getElementById('username')?.value?.trim();
    const p = document.getElementById('password')?.value?.trim();
    if (!u || !p) { showToast('Please enter username and password.', 'error'); return; }

    try {
        const { ok, data } = await apiPost('/login', { username: u, password: p });
        if (ok && data.success) {
            const role = data.data;
            setCurrentUser({ username: u, role: role });
            showToast('Login successful!', 'success');
            const target = role === 'ADMIN' ? 'admin-dashboard.html' : 'employee-dashboard.html';
            setTimeout(() => { window.location.href = target; }, 800);
        } else {
            showToast(data.message || 'Invalid username or password.', 'error');
        }
    } catch (err) {
        showToast('Connection error. Is the backend running?', 'error');
    }
}

function initRegistration() {
    const btn = document.getElementById('signup-btn');
    if (!btn) return;

    btn.addEventListener('click', async function () {
        // 1. Capture Data from inputs
        const fullName = document.getElementById('reg-fullname')?.value?.trim();
        const username = document.getElementById('reg-username')?.value?.trim();
        const rawPassword = document.getElementById('reg-password')?.value?.trim();
        const role = document.getElementById('reg-role')?.value;

        // 2. Validation
        if (!fullName || !username || !rawPassword || !role || role === "" || role === "Role") { 
            showToast('Please fill out all fields and select a role.', 'error'); 
            return; 
        }
        if (rawPassword.length < 4) {
            showToast('Password must be at least 4 characters.', 'error');
            return;
        }

        // 3. Prepare the package for Backend
        const userData = {
            fullName: fullName,
            username: username,
            password: rawPassword, 
            role: role
        };

        // 4. The Backend Handshake
        try {
            const { ok, data } = await apiPost('/register', userData);

            if (ok) {
                showToast('Account created successfully! Redirecting to login...', 'success');
                setTimeout(() => { window.location.href = 'login.html'; }, 1200);
            } else {
                // This handles errors like "Username already exists"
                showToast(data.message || 'Registration failed.', 'error');
            }
        } catch (error) {
            console.error("Connection error:", error);
            showToast('Cannot connect to the server. Is the backend running?', 'error');
        }
    });
}

// ── Initialize App ──
document.addEventListener('DOMContentLoaded', () => {
    initRegistration();
    populateCategorySelects();
    
    // Attach login listener if login button exists on page
    const loginBtn = document.getElementById('login-btn');
    if (loginBtn) loginBtn.addEventListener('click', handleLogin);
});