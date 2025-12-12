// Get property ID from URL
const urlParams = new URLSearchParams(window.location.search);
const propertyId = urlParams.get('id');

// DOM elements
const loadingState = document.getElementById('loadingState');
const errorState = document.getElementById('errorState');
const errorMessage = document.getElementById('errorMessage');
const mainContent = document.getElementById('mainContent');

// Property elements
const mainImage = document.getElementById('mainImage');
const imageCounter = document.getElementById('imageCounter');
const thumbnailContainer = document.getElementById('thumbnailContainer');
const propertyName = document.getElementById('propertyName');
const propertyLocation = document.getElementById('propertyLocation');
const propertyType = document.getElementById('propertyType');
const propertyPrice = document.getElementById('propertyPrice');
const pricePeriod = document.getElementById('pricePeriod');
const occupancySection = document.getElementById('occupancySection');
const maxOccupancy = document.getElementById('maxOccupancy');
const amenitiesList = document.getElementById('amenitiesList');
const billsList = document.getElementById('billsList');
const securitySection = document.getElementById('securitySection');
const securityDetails = document.getElementById('securityDetails');
const vacantCount = document.getElementById('vacantCount');

let currentImageIndex = 0;
let propertyImages = [];
let propertyData = null;

// Check if property ID exists
if (!propertyId) {
    showError('No property ID provided in URL');
} else {
    loadPropertyDetails();
}

async function loadPropertyDetails() {
    try {
        console.log('Loading property:', propertyId);

        // Fetch property details
        const response = await fetch(`/api/public/properties/${propertyId}`);

        if (!response.ok) {
            throw new Error(`Property not found (${response.status})`);
        }

        propertyData = await response.json();
        console.log('Property data:', propertyData);

        // Fetch vacant room count
        let vacantRoomCount = 0;
        try {
            const vacantResponse = await fetch(`/api/public/properties/${propertyId}/vacant-count`);
            if (vacantResponse.ok) {
                const vacantData = await vacantResponse.json();
                vacantRoomCount = vacantData.count || 0;
            }
        } catch (err) {
            console.warn('Could not fetch vacant room count:', err);
        }

        // Display property details
        displayPropertyDetails(propertyData, vacantRoomCount);

        // Hide loading, show content
        loadingState.style.display = 'none';
        mainContent.style.display = 'block';

    } catch (err) {
        console.error('Error loading property:', err);
        showError(err.message || 'Failed to load property details');
    }
}

function displayPropertyDetails(property, vacantRoomCount) {
    console.log('Displaying property details');

    // Set basic info
    propertyName.textContent = property.name || 'Unnamed Property';
    propertyLocation.textContent = `📍 ${property.location || 'Location not specified'}`;
    propertyType.textContent = formatPropertyType(property.type) || 'Property';
    propertyPrice.textContent = `Ksh ${formatPrice(property.price)}`;

    // Set period based on property type
    if (property.type === 'HOSTEL') {
        pricePeriod.textContent = 'per semester';
    } else {
        pricePeriod.textContent = 'per month';
    }

    // Show occupancy for hostels
    if (property.type === 'HOSTEL' && property.maxOccupancy) {
        occupancySection.style.display = 'block';
        maxOccupancy.textContent = `${property.maxOccupancy} people per room`;
    }

    // Display images
    if (property.images) {
        try {
            if (typeof property.images === 'string') {
                propertyImages = JSON.parse(property.images);
            } else if (Array.isArray(property.images)) {
                propertyImages = property.images;
            }

            if (propertyImages.length > 0) {
                displayImages();
            } else {
                propertyImages = ['pics-logos/placeholder.png'];
                displayImages();
            }
        } catch (e) {
            console.error('Error parsing images:', e);
            propertyImages = ['pics-logos/placeholder.png'];
            displayImages();
        }
    } else {
        propertyImages = ['pics-logos/placeholder.png'];
        displayImages();
    }

    // Display amenities
    if (property.amenities) {
        try {
            let amenitiesArray = property.amenities;
            if (typeof property.amenities === 'string') {
                amenitiesArray = JSON.parse(property.amenities);
            }

            if (Array.isArray(amenitiesArray) && amenitiesArray.length > 0) {
                amenitiesList.innerHTML = amenitiesArray
                    .map(amenity => `<span class="tag">✓ ${amenity}</span>`)
                    .join('');
            } else {
                amenitiesList.innerHTML = '<span class="tag">None specified</span>';
            }
        } catch (e) {
            amenitiesList.innerHTML = '<span class="tag">None specified</span>';
        }
    } else {
        amenitiesList.innerHTML = '<span class="tag">None specified</span>';
    }

    // Display bills
    if (property.bills) {
        try {
            let billsArray = property.bills;
            if (typeof property.bills === 'string') {
                billsArray = JSON.parse(property.bills);
            }

            if (Array.isArray(billsArray) && billsArray.length > 0) {
                const billIcons = { water: '💧', garbage: '🗑️', electricity: '⚡', wifi: '📶', security: '🔒', cleaning: '🧹' };
                billsList.innerHTML = billsArray
                    .map(bill => `<span class="tag">${billIcons[bill] || '✓'} ${bill}</span>`)
                    .join('');
            } else {
                billsList.innerHTML = '<span class="tag">None included</span>';
            }
        } catch (e) {
            billsList.innerHTML = '<span class="tag">None included</span>';
        }
    } else {
        billsList.innerHTML = '<span class="tag">None included</span>';
    }

    // Security Details
    if (property.securityDetails) {
        securityDetails.textContent = property.securityDetails;
    } else {
        securitySection.style.display = 'none';
    }

    // Vacant Room Count
    vacantCount.textContent = `${vacantRoomCount} vacant room${vacantRoomCount !== 1 ? 's' : ''} available`;

    // --- BOOKING LOGIC (MOVED INSIDE FUNCTION CORRECTLY) ---
    const user = JSON.parse(localStorage.getItem('user'));
    const bookingSection = document.getElementById('bookingSection');

    // Only show booking form if user is logged in AND is a student
    if (user && user.role === 'STUDENT') {
        bookingSection.style.display = 'block';

        // Set up the Book Button Listener
        const bookBtn = document.getElementById('bookBtn');
        // Clone to remove old listeners
        const newBtn = bookBtn.cloneNode(true);
        bookBtn.parentNode.replaceChild(newBtn, bookBtn);

        // PASS THE PROPERTY ID CORRECTLY
        newBtn.addEventListener('click', () => handleBooking(property.id));
    } else if (!user) {
        // Show login prompt if not logged in
        bookingSection.style.display = 'block';
        bookingSection.innerHTML = `<button class="btn-submit" onclick="window.location.href='login.html'">Login to Book this Room</button>`;
    }
} // <--- END OF displayPropertyDetails

function displayImages() {
    if (propertyImages.length === 0) return;

    mainImage.src = propertyImages[currentImageIndex];
    mainImage.alt = propertyData?.name || 'Property image';
    mainImage.onerror = function() { this.src = 'pics-logos/placeholder.png'; };

    imageCounter.textContent = `${currentImageIndex + 1} / ${propertyImages.length}`;

    // FIXED: Correct HTML image tag syntax here
    thumbnailContainer.innerHTML = propertyImages
        .map((img, idx) => `
            <img src="${img}" 
                 alt="Thumbnail ${idx + 1}" 
                 class="thumbnail ${idx === currentImageIndex ? 'active' : ''}"
                 onclick="changeImage(${idx})"
                 onerror="this.src='pics-logos/placeholder.png'">
        `)
        .join('');
}

function changeImage(index) {
    currentImageIndex = index;
    displayImages();
}

function formatPrice(price) {
    if (!price) return '0';
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

function formatPropertyType(type) {
    const typeMap = { 'SINGLE_ROOM': 'Single Room', 'BEDSITTER': 'Bedsitter', 'HOSTEL': 'Hostel', 'ONE_BEDROOM': 'One Bedroom', 'OTHER': 'Other' };
    return typeMap[type] || type || 'Property';
}

function showError(message) {
    loadingState.style.display = 'none';
    errorState.style.display = 'block';
    errorMessage.textContent = message;
}

// Handle booking logic
async function handleBooking(propertyId) {
    const startInput = document.getElementById('bookStart');
    const endInput = document.getElementById('bookEnd');
    const msg = document.getElementById('bookMsg');
    const user = JSON.parse(localStorage.getItem('user'));

    if (!startInput.value || !endInput.value) {
        alert("Please select both start and end dates.");
        return;
    }

    const bookingData = {
        studentId: user.id,
        propertyId: propertyId,
        startDate: startInput.value,
        endDate: endInput.value
    };

    try {
        const response = await fetch('http://localhost:8080/api/booking/book', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token') // Ensure token exists
            },
            body: JSON.stringify(bookingData)
        });

        if (response.ok) {
            msg.textContent = "✅ Booking request sent successfully!";
            msg.style.color = "green";
            document.getElementById('bookBtn').disabled = true;
        } else {
            const errorText = await response.text();
            msg.textContent = "❌ Booking failed: " + errorText;
            msg.style.color = "red";
        }
    } catch (error) {
        console.error("Booking error:", error);
        msg.textContent = "❌ Error connecting to server.";
        msg.style.color = "red";
    }
}

// Handle contact form submission
const contactForm = document.getElementById('contactForm');
if (contactForm) {
    contactForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = {
            propertyId: propertyId,
            propertyName: propertyData?.name || 'Unknown Property',
            name: document.getElementById('contactName').value,
            email: document.getElementById('contactEmail').value,
            phone: document.getElementById('contactPhone').value,
            message: document.getElementById('contactMessage').value
        };

        try {
            const response = await fetch('/api/inquiries', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            if (!response.ok) throw new Error('Failed to submit inquiry');

            alert('✅ Your inquiry has been submitted successfully!');
            contactForm.reset();

        } catch (err) {
            alert('❌ Failed to submit inquiry.');
        }
    });
}