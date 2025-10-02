// Function to close the notification element by hiding it
function closeNotification() {
    document.getElementById('notification').style.display = 'none';
}

// When the window loads, show the notification and then hide it after 10 seconds
window.onload = function() {
    // Get the notification element
    var notification = document.getElementById('notification');
    // Display the notification as a flex container
    notification.style.display = 'flex';
    // Set a timer to hide the notification after 10,000 milliseconds (10 seconds)
    setTimeout(function() {
        notification.style.display = 'none';
    }, 10000);
}
