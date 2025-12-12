// Get admin info from localStorage
const ADMIN_ID = localStorage.getItem('adminId');
const ADMIN_USERNAME = localStorage.getItem('adminUsername');
const IS_LOGGED_IN = localStorage.getItem('isAdminLoggedIn');

// Redirect to login if not authenticated
if (!ADMIN_ID || !IS_LOGGED_IN || IS_LOGGED_IN !== 'true') {
    console.log('Not authenticated, redirecting to login...');
    localStorage.clear();
    window.location.replace('admin-login.html');
    throw new Error('Redirecting to login');
}

console.log('Admin authenticated:', ADMIN_USERNAME);
document.getElementById('adminUsername').textContent = ADMIN_USERNAME || 'Admin';

let allProperties = [];
let allLandlords = [];

// Load dashboard stats
async function loadDashboardStats() {
    try {
        const res = await fetch('/api/admin/dashboard/stats', {
            headers: { 'X-Admin-Id': ADMIN_ID }
        });
        const stats = await res.json();

        document.getElementById('totalProperties').textContent = stats.totalProperties;
        document.getElementById('activeProperties').textContent = stats.activeProperties;
        document.getElementById('pendingProperties').textContent = stats.pendingProperties;
        document.getElementById('totalLandlords').textContent = stats.totalLandlords;

        // Update total vacant rooms from backend
        document.getElementById('totalVacantRooms').textContent = stats.totalVacantRooms || 0;
        // Update student count
        document.getElementById('totalUsers').textContent = stats.totalStudents || 0;

        // Load additional features
        generateActivityFeed();
    } catch (err) {
        console.error('Error loading stats:', err);
    }
}

// Load landlords
async function loadLandlords() {
    try {
        const res = await fetch('/api/admin/landlords', {
            headers: { 'X-Admin-Id': ADMIN_ID }
        });
        allLandlords = await res.json();

        // ✅ ADD THIS: Load properties first if not already loaded
        if (allProperties.length === 0) {
            const propsRes = await fetch('/api/admin/properties', {
                headers: { 'X-Admin-Id': ADMIN_ID }
            });
            allProperties = await propsRes.json();
        }

        const tbody = document.getElementById('landlordsBody');
        tbody.innerHTML = '';

        allLandlords.forEach(landlord => {
            const row = tbody.insertRow();
            row.innerHTML = `
                <td>${landlord.landlordId}</td>
                <td>${landlord.name || 'N/A'}</td>
                <td>${landlord.email}</td>
                <td>${landlord.phone || 'N/A'}</td>
                <td><span class="badge ${landlord.status?.toLowerCase() || 'active'}">${landlord.status || 'ACTIVE'}</span></td>
                <td>${getPropertyCountForLandlord(landlord.landlordId)}</td>
                <td>
                    ${landlord.status !== 'SUSPENDED' ?
                `<button class="btn btn-suspend" onclick="suspendLandlord(${landlord.landlordId})">Suspend</button>` :
                `<button class="btn btn-approve" onclick="activateLandlord(${landlord.landlordId})">Activate</button>`
            }
                </td>
            `;
        });

        document.getElementById('landlordsLoading').style.display = 'none';
        document.getElementById('landlordsTable').style.display = 'table';
    } catch (err) {
        console.error('Error loading landlords:', err);
        document.getElementById('landlordsLoading').textContent = 'Failed to load landlords';
    }
}

// Load properties
async function loadProperties() {
    try {
        const res = await fetch('/api/admin/properties', {
            headers: { 'X-Admin-Id': ADMIN_ID }
        });
        allProperties = await res.json();

        console.log('Loaded properties:', allProperties);

        renderProperties(allProperties);

        // Calculate and display charts
        if (allProperties.length > 0) {
            calculatePropertiesByType(allProperties);
            calculatePropertiesByLocation(allProperties);
        }

        document.getElementById('propertiesLoading').style.display = 'none';
        document.getElementById('propertiesTable').style.display = 'table';
    } catch (err) {
        console.error('Error loading properties:', err);
        document.getElementById('propertiesLoading').textContent = 'Failed to load properties';
    }
}

function renderProperties(properties) {
    const tbody = document.getElementById('propertiesBody');
    tbody.innerHTML = '';

    properties.forEach(property => {
        const landlord = allLandlords.find(l => l.landlordId === property.landlordId);
        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${property.id}</td>
            <td>${property.name}</td>
            <td>${property.type}</td>
            <td>${property.location}</td>
            <td>Ksh ${property.price.toLocaleString()}</td>
            <td>${property.vacantRooms || 0}</td>
            <td>${landlord?.name || 'Unknown'}</td>
            <td><span class="badge ${property.status?.toLowerCase() || 'pending'}">${property.status || 'PENDING'}</span></td>
            <td>
                ${property.status !== 'APPROVED' ?
            `<button class="btn btn-approve" onclick="approveProperty(${property.id})">Approve</button>` : ''}
                ${property.status !== 'REJECTED' ?
            `<button class="btn btn-reject" onclick="rejectProperty(${property.id})">Reject</button>` : ''}
            </td>
        `;
    });
}

function filterProperties(status) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');

    if (status === 'all') {
        renderProperties(allProperties);
    } else {
        const filtered = allProperties.filter(p => p.status === status);
        renderProperties(filtered);
    }
}

function getPropertyCountForLandlord(landlordId) {
    return allProperties.filter(p => p.landlordId === landlordId).length;
}

// Approve property
async function approveProperty(propertyId) {
    if (!confirm('Approve this property?')) return;

    try {
        const res = await fetch(`/api/admin/properties/${propertyId}/approve`, {
            method: 'PUT',
            headers: { 'X-Admin-Id': ADMIN_ID }
        });

        if (res.ok) {
            alert('Property approved successfully!');
            await loadProperties();
            await loadDashboardStats();
        }
    } catch (err) {
        alert('Failed to approve property');
    }
}

// Reject property
async function rejectProperty(propertyId) {
    if (!confirm('Reject this property?')) return;

    try {
        const res = await fetch(`/api/admin/properties/${propertyId}/reject`, {
            method: 'PUT',
            headers: { 'X-Admin-Id': ADMIN_ID }
        });

        if (res.ok) {
            alert('Property rejected');
            await loadProperties();
            await loadDashboardStats();
        }
    } catch (err) {
        alert('Failed to reject property');
    }
}

// Suspend landlord
async function suspendLandlord(landlordId) {
    if (!confirm('Suspend this landlord? Their properties will be hidden.')) return;

    try {
        const res = await fetch(`/api/admin/landlords/${landlordId}/suspend`, {
            method: 'PUT',
            headers: { 'X-Admin-Id': ADMIN_ID }
        });

        if (res.ok) {
            alert('Landlord suspended');
            loadLandlords();
        }
    } catch (err) {
        alert('Failed to suspend landlord');
    }
}

// Activate landlord
async function activateLandlord(landlordId) {
    if (!confirm('Reactivate this landlord?')) return;

    try {
        const res = await fetch(`/api/admin/landlords/${landlordId}/activate`, {
            method: 'PUT',
            headers: { 'X-Admin-Id': ADMIN_ID }
        });

        if (res.ok) {
            alert('Landlord reactivated');
            loadLandlords();
        } else {
            alert('Failed to activate landlord');
        }
    } catch (err) {
        alert('Failed to activate landlord');
    }
}

// Show section
function showSection(section) {
    document.querySelectorAll('.sidebar nav a').forEach(a => a.classList.remove('active'));
    event.target.classList.add('active');

    document.getElementById('dashboardSection').style.display = 'none';
    document.getElementById('landlordsSection').style.display = 'none';
    document.getElementById('propertiesSection').style.display = 'none';

    if (section === 'dashboard') {
        document.getElementById('dashboardSection').style.display = 'block';
        loadDashboardStats();
        if (allProperties.length === 0) {
            loadLandlords().then(() => loadProperties());
        }
    } else if (section === 'landlords') {
        document.getElementById('landlordsSection').style.display = 'block';
        if (allLandlords.length === 0) loadLandlords();
    } else if (section === 'properties') {
        document.getElementById('propertiesSection').style.display = 'block';
        if (allProperties.length === 0) {
            loadLandlords().then(() => loadProperties());
        }
    }
}

// Generate activity feed
function generateActivityFeed() {
    const activities = [
        { type: 'success', text: 'Property "Sunset Apartments" approved', time: '2 minutes ago' },
        { type: 'warning', text: 'New property pending approval', time: '15 minutes ago' },
        { type: 'success', text: 'New landlord registered: John Doe', time: '1 hour ago' },
        { type: 'danger', text: 'Property "Green Villa" rejected', time: '2 hours ago' },
        { type: 'success', text: 'Landlord account activated', time: '3 hours ago' },
        { type: 'warning', text: 'Property "Blue Heights" requires review', time: '4 hours ago' },
        { type: 'success', text: '5 new students registered', time: '5 hours ago' },
        { type: 'success', text: 'Property "Ocean View" approved', time: '6 hours ago' }
    ];

    const feedHtml = activities.map(activity => `
        <div class="activity-item ${activity.type}">
            <div class="activity-text">${activity.text}</div>
            <div class="activity-time">${activity.time}</div>
        </div>
    `).join('');

    document.getElementById('activityFeed').innerHTML = feedHtml;
}

// Calculate properties by type
function calculatePropertiesByType(properties) {
    const typeCount = {};
    properties.forEach(prop => {
        const type = prop.type || 'Unknown';
        typeCount[type] = (typeCount[type] || 0) + 1;
    });

    const total = properties.length || 1;
    const colors = ['#667eea', '#4caf50', '#ff9800', '#f44336', '#2196f3'];
    let html = '';

    Object.entries(typeCount).forEach(([type, count], index) => {
        const percentage = ((count / total) * 100).toFixed(1);
        html += `
            <div class="chart-item">
                <div class="chart-label">
                    <div class="chart-color" style="background: ${colors[index % colors.length]}"></div>
                    <span>${type}</span>
                </div>
                <div class="chart-value">${count}</div>
            </div>
            <div class="chart-bar" style="width: ${percentage}%; background: ${colors[index % colors.length]}" data-percentage="${percentage}%"></div>
        `;
    });

    document.getElementById('propertiesByType').innerHTML = html || '<div class="empty-state">No data available</div>';
}

// Calculate properties by location
function calculatePropertiesByLocation(properties) {
    const locationCount = {};
    properties.forEach(prop => {
        const location = prop.location || 'Unknown';
        locationCount[location] = (locationCount[location] || 0) + 1;
    });

    const sortedLocations = Object.entries(locationCount)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 5);

    const total = properties.length || 1;
    const colors = ['#667eea', '#4caf50', '#ff9800', '#f44336', '#2196f3'];
    let html = '';

    sortedLocations.forEach(([location, count], index) => {
        const percentage = ((count / total) * 100).toFixed(1);
        html += `
            <div class="chart-item">
                <div class="chart-label">
                    <div class="chart-color" style="background: ${colors[index]}"></div>
                    <span>${location}</span>
                </div>
                <div class="chart-value">${count}</div>
            </div>
            <div class="chart-bar" style="width: ${percentage}%; background: ${colors[index]}" data-percentage="${percentage}%"></div>
        `;
    });

    document.getElementById('propertiesByLocation').innerHTML = html || '<div class="empty-state">No data available</div>';
}

// Load actual student count from backend
async function loadTotalUsers() {
    try {

        const res = await fetch('/api/admin/dashboard/stats', {
            headers: { 'X-Admin-Id': ADMIN_ID }
        });
        const stats = await res.json();

        document.getElementById('totalUsers').textContent = stats.totalStudents || 0;
    } catch (err) {
        console.error('Error loading student count:', err);
        document.getElementById('totalUsers').textContent = '0';
    }
}

// Logout
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.replace('admin-login.html');
    }
}

// Refresh activity feed every 60 seconds
setInterval(generateActivityFeed, 60000);

// Load initial data
loadDashboardStats();
loadLandlords().then(() => loadProperties());