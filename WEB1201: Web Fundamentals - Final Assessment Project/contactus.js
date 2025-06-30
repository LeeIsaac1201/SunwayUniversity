/* eslint-env browser */
/* global document */

// Expose functions to global scope for HTML usage
window.closeNotification = closeNotification
window.handleContactFormSubmission = handleContactFormSubmission

// Function to display notifications
function showNotification (type, message) {
  const notificationId = type === 'success' ? 'notification' : 'error-notification'
  const notification = document.getElementById(notificationId)
  notification.innerHTML =
    message +
    '<span class="close-btn" onclick="closeNotification(\'' + type + '\')">&times;</span>'
  notification.style.display = 'block'
}

// Function to hide notifications
function closeNotification (type) {
  const notificationId = type === 'success' ? 'notification' : 'error-notification'
  const notification = document.getElementById(notificationId)
  notification.style.display = 'none'
}

// Function to handle form submission
function handleContactFormSubmission (event) {
  event.preventDefault()
  const name = document.getElementById('name').value.trim()
  const email = document.getElementById('email').value.trim()
  const message = document.getElementById('message').value.trim()

  if (name === '' || email === '' || message === '') {
    showNotification('error', 'Please fill out all fields correctly.')
    return false
  }

  showNotification('success', 'Your message has been sent successfully!')
  document.getElementById('contact-form').reset()
  return false
}