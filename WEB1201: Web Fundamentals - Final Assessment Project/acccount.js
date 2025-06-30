/* eslint-disable no-unused-vars */
/* global document, setTimeout, window */

// Function to handle form submission and display notification.
function handleFormSubmission(event, notificationId, redirectUrl) {
    event.preventDefault(); // Prevent the default form submission

    // Display the notification.
    const notification = document.getElementById(notificationId);
    notification.style.display = 'block';

    // Redirect to Home.html after three seconds.
    setTimeout(() => {
        window.location.href = redirectUrl;
    }, 3000);
}

// Add event listener for the registration form.
document.getElementById('registration-form').addEventListener('submit', (event) => {
    handleFormSubmission(event, 'registration-notification', 'home.html');
});

// Add event listener for the log in form.
document.getElementById('login-form').addEventListener('submit', (event) => {
    handleFormSubmission(event, 'login-notification', 'home.html');
});

// Function to disable all elements of a form.
function disableForm(formId) {
    const form = document.getElementById(formId);
    Array.from(form.elements).forEach(element => {
        element.disabled = true;
    });
}