// landlord-dashboard.js

let currentPropertyId = null;

// Get landlord ID from localStorage
const LANDLORD_ID = localStorage.getItem('landlordId');
if (!LANDLORD_ID) {
    alert('Please log in first!');
    window.location.href = 'login-landlord.html';
}

// Load properties on page load
document.addEventListener('DOMContentLoaded', () => {
    fetchProperties();

    // Initialize property type change handler
    const propTypeSelect = document.getElementById('propType');
    if (propTypeSelect) {
        propTypeSelect.addEventListener('change', handlePropertyTypeChange);
        // Set initial state
        handlePropertyTypeChange();
    }
});

// Fetch and display properties
async function fetchProperties() {
    try {
        const res = await fetch(`/api/landlord/properties`, {
            headers: { 'X-Landlord-Id': LANDLORD_ID }
        });
        const properties = await res.json();
        await renderProperties(properties);
    } catch (err) {
        console.error('Error loading properties:', err);
        alert('Failed to load properties');
    }
}

// Render properties with vacant count
async function renderProperties(properties) {
    const tbody = document.getElementById('propertiesBody');
    tbody.innerHTML = '';

    for (const p of properties) {
        let vacantCount = 'Loading...';

        try {
            const vacantRes = await fetch(`/api/landlord/properties/${p.id}/vacant-count`, {
                headers: { 'X-Landlord-Id': LANDLORD_ID }
            });

            if (vacantRes.ok) {
                vacantCount = await vacantRes.json();
            } else {
                vacantCount = 'N/A';
            }
        } catch (err) {
            console.error(`Error fetching vacant count for property ${p.id}:`, err);
            vacantCount = 'Error';
        }

        // Parse images
        let imageHtml = 'No images';
        if (p.images) {
            try {
                const images = JSON.parse(p.images);
                if (images.length > 0) {
                    imageHtml = `<img src="${images[0]}" style="width:50px;height:50px;object-fit:cover;" alt="Property">`;
                }
            } catch (e) {
                console.error('Error parsing images', e);
            }
        }
        // Parse and display bills
        let billsHtml = 'None';
        if (p.bills) {
            try {
                const bills = typeof p.bills === 'string' ? JSON.parse(p.bills) : p.bills;
                if (bills.length > 0) {
                    billsHtml = bills.map(b => {
                        const icons = {
                            water: '💧',
                            garbage: '🗑️',
                            electricity: '⚡',
                            wifi: '📶',
                            security: '🔒',
                            cleaning: '🧹'
                        };
                        return `<span style="display:inline-block; margin:2px; padding:2px 6px; background:#e3f2fd; border-radius:3px; font-size:11px;">${icons[b] || '✓'} ${b}</span>`;
                    }).join('');
                }
            } catch (e) {
                console.error('Error parsing bills', e);
                billsHtml = 'Error';
            }
        }

        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${p.id}</td>
            <td>${imageHtml}</td>
            <td>${p.name}</td>
            <td>${p.type}</td> 
            <td>${p.location || 'N/A'}</td>
            <td>Vacant: <strong>${vacantCount}</strong></td>
            <td>KSh ${p.price.toLocaleString()}</td>
             <td style="max-width: 200px;">${billsHtml}</td>
            <td>
                <button onclick="viewRooms(${p.id})" class="btn-view">View Rooms</button>
                <button onclick="editProperty(${p.id})" class="btn-edit">Edit</button>
                <button onclick="deleteProperty(${p.id})" class="btn-delete">Delete</button>
            </td>
        `;
    }
}
// Handle property type change - Show/hide fields based on type
function handlePropertyTypeChange() {
    const propertyType = document.getElementById('propType')?.value;
    const maxOccupancyGroup = document.getElementById('maxOccupancyGroup');
    const maxOccupancyInput = document.getElementById('propMaxOccupancy');
    const priceLabel = document.getElementById('priceLabel');
    const priceInput = document.getElementById('propPrice');
    const priceHint = document.getElementById('priceHint');

    console.log('🔄 Property type changed to:', propertyType);

    if (!propertyType) {
        // No type selected yet
        if (maxOccupancyGroup) maxOccupancyGroup.style.display = 'none';
        return;
    }

    if (propertyType === 'HOSTEL') {
        // Show max occupancy field for hostels
        if (maxOccupancyGroup) {
            maxOccupancyGroup.style.display = 'block';
            console.log('✅ Showing max occupancy field');
        }
        if (maxOccupancyInput) {
            maxOccupancyInput.required = true;
            maxOccupancyInput.value = maxOccupancyInput.value || 2;
        }

        // Change price label and hint for hostels
        if (priceLabel) {
            priceLabel.innerHTML = 'Price Per Semester (KES): <span style="color: #e74c3c; font-size: 12px;">*Required</span>';
        }
        if (priceHint) {
            priceHint.textContent = 'Total amount per student per semester (4-5 months)';
        }
        if (priceInput) {
            priceInput.placeholder = 'e.g., 10000';
            priceInput.min = '1000';
        }
        console.log('Switched to HOSTEL mode (per semester pricing)');

    } else {
        // Hide max occupancy for other types
        if (maxOccupancyGroup) {
            maxOccupancyGroup.style.display = 'none';
            console.log('Hiding max occupancy field');
        }
        if (maxOccupancyInput) {
            maxOccupancyInput.required = false;
            maxOccupancyInput.value = 1;
        }

        // Change price label for monthly rent
        if (priceLabel) {
            priceLabel.innerHTML = 'Price Per Month (KES): <span style="color: #e74c3c; font-size: 12px;">*Required</span>';
        }
        if (priceHint) {
            priceHint.textContent = 'Monthly rent amount';
        }
        if (priceInput) {
            priceInput.placeholder = 'e.g., 5000';
            priceInput.min = '500';
        }
        console.log('✅ Switched to MONTHLY mode');
    }
}

// Open "Add Property" modal
function openAddPropertyModal() {
    document.getElementById('modalTitle').textContent = 'Add New Property';
    document.getElementById('propertyForm').reset();
    // Uncheck all bill checkboxes
    document.getElementById('billWater').checked = false;
    document.getElementById('billGarbage').checked = false;
    document.getElementById('billElectricity').checked = false;
    document.getElementById('billWifi').checked = false;
    document.getElementById('billSecurity').checked = false;
    document.getElementById('billCleaning').checked = false;

    document.getElementById('imageUploadSection').style.display = 'none'; // Hide image section initially
    currentPropertyId = null;

    // Reset form state based on default type
    handlePropertyTypeChange();

    document.getElementById('propertyModal').style.display = 'block';
}

// Close modal
function closeModal() {
    document.getElementById('propertyModal').style.display = 'none';
    document.getElementById('imageUploadSection').style.display = 'none';
    currentPropertyId = null;
}

// Edit property - populate modal with existing data
async function editProperty(propertyId) {
    try {
        const res = await fetch(`/api/landlord/properties/${propertyId}`, {
            headers: { 'X-Landlord-Id': LANDLORD_ID }
        });

        if (!res.ok) {
            alert('Failed to load property details');
            return;
        }

        const prop = await res.json();

        // Fill modal form with existing data
        document.getElementById('propName').value = prop.name;
        document.getElementById('propLocation').value = prop.location || '';
        document.getElementById('propType').value = prop.type;
        document.getElementById('propMaxOccupancy').value = prop.maxOccupancy;
        document.getElementById('propPrice').value = prop.price;

        // Trigger type change to show/hide appropriate fields
        handlePropertyTypeChange();


        // Load amenities as comma-separated string
        if (prop.amenities && Array.isArray(prop.amenities)) {
            document.getElementById('propAmenities').value = prop.amenities.join(', ');
        } else {
            document.getElementById('propAmenities').value = '';
        }

        // Load security details
        document.getElementById('propSecurity').value = prop.securityDetails || '';

        // Pre-check bills checkboxes
        const bills = prop.bills ? (typeof prop.bills === 'string' ? JSON.parse(prop.bills) : prop.bills) : [];
        document.getElementById('billWater').checked = bills.includes('water');
        document.getElementById('billGarbage').checked = bills.includes('garbage');
        document.getElementById('billElectricity').checked = bills.includes('electricity');
        document.getElementById('billWifi').checked = bills.includes('wifi');
        document.getElementById('billSecurity').checked = bills.includes('security');
        document.getElementById('billCleaning').checked = bills.includes('cleaning');

        // Set current property ID for update
        currentPropertyId = propertyId;

        // Show image upload section for editing
        document.getElementById('imageUploadSection').style.display = 'block';

        // Update modal title
        document.getElementById('modalTitle').textContent = 'Edit Property';

        // Show modal
        document.getElementById('propertyModal').style.display = 'block';
    } catch (err) {
        console.error('Error loading property:', err);
        alert('Failed to load property details');
    }
}

// Delete property
async function deleteProperty(propertyId) {
    if (!confirm('Are you sure you want to delete this property and all its rooms? This action cannot be undone.')) {
        return;
    }

    try {
        const res = await fetch(`/api/landlord/properties/${propertyId}`, {
            method: 'DELETE',
            headers: { 'X-Landlord-Id': LANDLORD_ID }
        });

        if (res.ok) {
            alert('Property deleted successfully!');
            fetchProperties();
        } else {
            const error = await res.json();
            alert('Failed to delete property: ' + (error.message || 'Unknown error'));
        }
    } catch (err) {
        console.error('Error deleting property:', err);
        alert('Network error while deleting property');
    }
}

// Handle property form submit (Create OR Update)
document.getElementById('propertyForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    console.log('=== FORM SUBMISSION DEBUG ===');


    const propertyType = document.getElementById('propType').value;

    // Validate hostel-specific fields
    if (propertyType === 'HOSTEL') {
        const maxOccupancy = parseInt(document.getElementById('propMaxOccupancy').value);
        if (!maxOccupancy || maxOccupancy < 1) {
            alert('Please enter max occupancy per room for hostels');
            return;
        }
    }

    // Collect bills data from checkboxes
    const bills = [];
    if (document.getElementById('billWater').checked) bills.push('water');
    if (document.getElementById('billGarbage').checked) bills.push('garbage');
    if (document.getElementById('billElectricity').checked) bills.push('electricity');
    if (document.getElementById('billWifi').checked) bills.push('wifi');
    if (document.getElementById('billSecurity').checked) bills.push('security');
    if (document.getElementById('billCleaning').checked) bills.push('cleaning');

    console.log('Bills collected:', bills);

    // Get amenities and convert to array
    const amenitiesInput = document.getElementById('propAmenities');
    console.log('Amenities input element:', amenitiesInput);
    console.log('Amenities raw value:', amenitiesInput?.value);

    const amenitiesValue = amenitiesInput?.value || '';
    let amenities = [];
    if (amenitiesValue.trim()) {
        amenities = amenitiesValue
            .split(',')
            .map(item => item.trim())
            .filter(item => item.length > 0);
    }
    console.log('Amenities after processing:', amenities);
    console.log('Amenities type:', typeof amenities);
    console.log('Is array?:', Array.isArray(amenities));

    // Get security details
    const securityInput = document.getElementById('propSecurity');
    console.log('Security input element:', securityInput);
    console.log('Security raw value:', securityInput?.value);

    const securityDetailsValue = securityInput?.value || '';
    const securityDetails = securityDetailsValue.trim() || null;
    console.log('Security details after processing:', securityDetails);

    // Get max occupancy (1 for non-hostels, user input for hostels)
    const maxOccupancy = propertyType === 'HOSTEL'
        ? parseInt(document.getElementById('propMaxOccupancy').value)
        : 1;


    const property = {
        name: document.getElementById('propName').value.trim(),
        location: document.getElementById('propLocation').value.trim(),
        type: document.getElementById('propType').value,
        maxOccupancy: parseInt(document.getElementById('propMaxOccupancy').value),
        price: parseFloat(document.getElementById('propPrice').value),
        bills: bills,
        amenities: amenities,
        securityDetails: securityDetails
    };

    console.log('=== FINAL PROPERTY OBJECT ===');
    console.log('Property object:', property);
    console.log('Property JSON:', JSON.stringify(property, null, 2));

    try {
        let res;

        if (currentPropertyId) {
            console.log('UPDATE MODE - Property ID:', currentPropertyId);
            res = await fetch(`/api/landlord/properties/${currentPropertyId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Landlord-Id': LANDLORD_ID
                },
                body: JSON.stringify(property)
            });
        } else {
            console.log('CREATE MODE');
            res = await fetch('/api/landlord/properties', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Landlord-Id': LANDLORD_ID
                },
                body: JSON.stringify(property)
            });
        }

        console.log('Response status:', res.status);
        console.log('Response ok:', res.ok);

        if (res.ok) {
            if (currentPropertyId) {
                alert('Property updated successfully! You can now upload images.');
                document.getElementById('imageUploadSection').style.display = 'block';
                return;
            } else {
                const createdProperty = await res.json();
                console.log('Created property:', createdProperty);
                currentPropertyId = createdProperty.id;
                document.getElementById('imageUploadSection').style.display = 'block';
                alert('Property created! Now upload images.');
                return;
            }
        } else {
            const errorText = await res.text();
            console.error('=== SERVER ERROR ===');
            console.error('Status:', res.status);
            console.error('Error text:', errorText);
            alert('Error: ' + errorText);
        }
    } catch (err) {
        console.error('=== NETWORK ERROR ===');
        console.error('Error:', err);
        alert('Network error: ' + err.message);
    }
});




// Upload images function with detailed error handling
async function uploadImages() {
    const fileInput = document.getElementById('propertyImages');
    const files = fileInput.files;

    if (files.length === 0) {
        alert('Please select at least one image');
        return;
    }

    if (!currentPropertyId) {
        alert('Please save the property first');
        return;
    }

    // Validate file sizes before uploading
    const maxFileSize = 10 * 1024 * 1024; // 10MB in bytes
    const maxTotalSize = 50 * 1024 * 1024; // 50MB in bytes
    let totalSize = 0;
    const oversizedFiles = [];

    for (let i = 0; i < files.length; i++) {
        totalSize += files[i].size;
        if (files[i].size > maxFileSize) {
            oversizedFiles.push({
                name: files[i].name,
                size: (files[i].size / (1024 * 1024)).toFixed(2) + ' MB'
            });
        }
    }

    // Check individual file sizes
    if (oversizedFiles.length > 0) {
        const fileList = oversizedFiles.map(f => `- ${f.name} (${f.size})`).join('\n');
        alert(`Upload Failed: File size limit exceeded\n\nThe following files are too large (max 10MB per file):\n${fileList}\n\nPlease compress or resize these images before uploading.`);
        return;
    }

    // Check total upload size
    if (totalSize > maxTotalSize) {
        alert(`Upload Failed: Total upload size too large\n\nTotal size: ${(totalSize / (1024 * 1024)).toFixed(2)} MB\nMaximum allowed: 50 MB\n\nPlease reduce the number of images or compress them.`);
        return;
    }

    // Check file types
    const invalidFiles = [];
    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (!file.type.startsWith('image/')) {
            invalidFiles.push(file.name);
        }
    }

    if (invalidFiles.length > 0) {
        alert(`Upload Failed: Invalid file types\n\nOnly image files are allowed. The following are not images:\n${invalidFiles.join('\n')}`);
        return;
    }

    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
        formData.append('images', files[i]);
    }

    // Show loading indicator
    const uploadBtn = document.querySelector('button[onclick="uploadImages()"]');
    const originalText = uploadBtn.textContent;
    uploadBtn.textContent = 'Uploading...';
    uploadBtn.disabled = true;

    try {
        const res = await fetch(`/api/landlord/properties/${currentPropertyId}/images`, {
            method: 'POST',
            headers: {
                'X-Landlord-Id': LANDLORD_ID
            },
            body: formData
        });

        // Reset button state
        uploadBtn.textContent = originalText;
        uploadBtn.disabled = false;

        if (res.ok) {
            const result = await res.json();
            alert(`Success! ${files.length} image(s) uploaded successfully!`);
            closeModal();
            fetchProperties();
        } else {
            // Parse error response
            const contentType = res.headers.get('content-type');
            let errorMessage = 'Unknown error occurred';

            if (contentType && contentType.includes('application/json')) {
                const errorData = await res.json();
                errorMessage = errorData.message || errorData.error || 'Upload failed';
            } else {
                const errorText = await res.text();

                // Check for common Spring errors
                if (errorText.includes('Maximum upload size exceeded')) {
                    errorMessage = 'File size too large. Maximum allowed:\n- Per file: 10 MB\n- Total upload: 50 MB\n\nPlease compress or resize your images.';
                } else if (errorText.includes('FileSizeLimitExceededException')) {
                    errorMessage = 'One or more files exceed the 10 MB size limit.\n\nPlease compress your images before uploading.';
                } else if (errorText.includes('SizeLimitExceededException')) {
                    errorMessage = 'Total upload size exceeds 50 MB limit.\n\nPlease reduce the number of images or compress them.';
                } else {
                    errorMessage = errorText.substring(0, 200); // Show first 200 chars
                }
            }

            alert(`Upload Failed (${res.status})\n\n${errorMessage}`);
        }
    } catch (err) {
        // Reset button state
        uploadBtn.textContent = originalText;
        uploadBtn.disabled = false;

        console.error('Upload error:', err);

        // Provide specific error messages
        if (err.name === 'TypeError' && err.message.includes('Failed to fetch')) {
            alert('Network Error: Unable to connect to server.\n\nPlease check:\n- Your internet connection\n- If the server is running\n- If you\'re using the correct URL');
        } else if (err.name === 'AbortError') {
            alert('Upload Cancelled: The upload was interrupted.\n\nThis might happen if:\n- Files are too large\n- Connection was lost\n- Request timed out');
        } else {
            alert(`Upload Error: ${err.message}\n\nPlease try again or contact support if the problem persists.`);
        }
    }
}

// Show info about selected files
function showFileInfo() {
    const fileInput = document.getElementById('propertyImages');
    const fileInfo = document.getElementById('fileInfo');
    const files = fileInput.files;

    if (files.length === 0) {
        fileInfo.innerHTML = '';
        return;
    }

    let totalSize = 0;
    let fileList = [];

    for (let i = 0; i < files.length; i++) {
        const size = (files[i].size / (1024 * 1024)).toFixed(2);
        totalSize += files[i].size;

        const sizeColor = files[i].size > 10 * 1024 * 1024 ? 'red' : 'green';
        fileList.push(`<div>✓ ${files[i].name} (<span style="color: ${sizeColor}">${size} MB</span>)</div>`);
    }

    const totalMB = (totalSize / (1024 * 1024)).toFixed(2);
    const totalColor = totalSize > 50 * 1024 * 1024 ? 'red' : 'green';

    fileInfo.innerHTML = `
        <strong>Selected ${files.length} file(s):</strong>
        ${fileList.join('')}
        <div style="margin-top: 5px; font-weight: bold;">
            Total size: <span style="color: ${totalColor}">${totalMB} MB</span> / 50 MB
        </div>
    `;
}

// View rooms for a property
function viewRooms(propertyId) {
    localStorage.setItem('currentPropertyId', propertyId);
    window.location.href = `property-rooms.html?propertyId=${propertyId}`;
}