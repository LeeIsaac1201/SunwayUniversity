// DOMContentLoaded event listener
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);

    // Handle search query
    const searchQuery = urlParams.get('name');
    if (searchQuery) {
        // Format the search query to match the product id format (lowercase and spaces replaced with dashes)
        const formattedQuery = searchQuery.trim().toLowerCase().replace(/\s+/g, '-');
        const productElement = document.getElementById(formattedQuery);
        if (productElement) {
            productElement.scrollIntoView({ behavior: 'smooth' });
        }
    }

    // Handle notification message
    const notificationMessage = urlParams.get('notification');
    // Only display notification if notificationMessage exists and is not just whitespace
    if (notificationMessage && notificationMessage.trim() !== "") {
        var notification = document.getElementById('notification');
        var notificationMessageElement = document.getElementById('notification-message');
        notificationMessageElement.textContent = notificationMessage;
        notification.style.display = 'block';
        setTimeout(() => {
            closeNotification();
            // Remove the notification query parameter from the URL after displaying the notification
            window.history.replaceState({}, document.title, window.location.pathname);
        }, 3000); // Close the notification after 3 seconds
    }
});

// Function to close notification
function closeNotification() {
    var notification = document.getElementById('notification');
    notification.style.display = 'none';
}

// Function to show specific section
function showSection(section) {
    document.getElementById('users-section').style.display = section === 'users' ? 'block' : 'none';
    document.getElementById('items-section').style.display = section === 'items' ? 'block' : 'none';
    document.getElementById('orders-section').style.display = section === 'orders' ? 'block' : 'none'; // Added Orders section
}
