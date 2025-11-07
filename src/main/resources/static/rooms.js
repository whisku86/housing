// rooms.js

// API Base URL
const API_BASE = '/api/public/properties';

// Load properties when page loads
document.addEventListener('DOMContentLoaded', () => {
    loadAllProperties();
});

// Fetch all properties grouped by type
async function loadAllProperties() {
    try {
        const response = await fetch(API_BASE);
        if (!response.ok) {
            throw new Error('Failed to fetch properties');
        }

        const properties = await response.json();

        // Group properties by type
        const grouped = groupByType(properties);

        // Render each category
        renderCategory('SINGLE_ROOM', grouped['SINGLE_ROOM'] || [], 'Single Rooms');
        renderCategory('BEDSITTER', grouped['BEDSITTER'] || [], 'Bedsitters');
        renderCategory('HOSTEL', grouped['HOSTEL'] || [], 'Hostels');
        renderCategory('ONE_BEDROOM', grouped['ONE_BEDROOM'] || [], '1 Bedroom');
        renderCategory('OTHER', grouped['OTHER'] || [], 'Other');
    } catch (error) {
        console.error('Error loading properties:', error);
        showError('Unable to load properties. Please try again later.');
    }
}

// Group properties by type
function groupByType(properties) {
    return properties.reduce((acc, property) => {
        const type = property.type;
        if (!acc[type]) {
            acc[type] = [];
        }
        acc[type].push(property);
        return acc;
    }, {});
}

// Render a category section
function renderCategory(type, properties, title) {
    // Find the section for this category
    const sections = document.querySelectorAll('.category');
    let targetSection = null;

    sections.forEach(section => {
        const heading = section.querySelector('h2');
        if (heading && heading.textContent === title) {
            targetSection = section;
        }
    });

    if (!targetSection) {
        console.warn(`Section for ${title} not found`);
        return;
    }

    const grid = targetSection.querySelector('.room-grid');

    if (properties.length === 0) {
        grid.innerHTML = '<p style="grid-column: 1/-1; text-align:center; color:#666;">No properties available in this category yet.</p>';
        return;
    }

    // Clear existing content
    grid.innerHTML = '';

    // Render each property
    properties.forEach(property => {
        const card = createPropertyCard(property);
        grid.appendChild(card);
    });
}

// Create a property card element
function createPropertyCard(property) {
    const card = document.createElement('div');
    card.className = 'room-card';

    // Get first image or use placeholder
    let imageUrl = 'pics-logos/placeholder.png';
    if (property.images) {
        try {
            const images = typeof property.images === 'string'
                ? JSON.parse(property.images)
                : property.images;
            if (images && images.length > 0) {
                imageUrl = images[0];
            }
        } catch (e) {
            console.error('Error parsing images for property', property.id, e);
        }
    }

    // Format price with appropriate period based on property type
    const priceValue = `Ksh ${property.price.toLocaleString()}`;
    const pricePeriod = property.type === 'HOSTEL' ? 'semester' : 'month';
    const priceDisplay = `${priceValue} / ${pricePeriod}`;

    card.innerHTML = `
        <img src="${imageUrl}" 
             alt="${property.name}" 
             onerror="this.onerror=null; this.src='pics-logos/placeholder.png';" 
             onload="this.classList.add('loaded')" />
        <div class="room-info">
            <h3>${property.name}</h3>
            <p>📍 ${property.location} | ${priceDisplay}</p>
            <button class="view-btn" onclick="viewPropertyDetails(${property.id})">View Details</button>
        </div>
    `;

    return card;
}

// View property details

function viewPropertyDetails(propertyId) {

    window.location.href = `property-details.html?id=${propertyId}`;
}

// Show error message
function showError(message) {
    const main = document.querySelector('.rooms-section');
    if (main) {
        main.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #f44336;">
                <h2>⚠️ ${message}</h2>
                <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer;">
                    Retry
                </button>
            </div>
        `;
    }
}

// Optional: Search functionality
function searchProperties(query) {
    fetch(`${API_BASE}/search?location=${encodeURIComponent(query)}`)
        .then(res => res.json())
        .then(properties => {
            // Render search results
            console.log('Search results:', properties);
        })
        .catch(err => console.error('Search error:', err));
}