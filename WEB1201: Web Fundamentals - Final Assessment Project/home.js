/* eslint-disable no-unused-vars */
/* global document, setTimeout, localStorage */

// Email Validation
function validateEmail (event) {
  event.preventDefault()
  const emailInput = document.getElementById('email')
  const email = emailInput.value
  const notification = document.getElementById('notification')
  const errorNotification = document.getElementById('error-notification')

  if (validateEmailFormat(email)) {
    showNotification(notification)
    autoCloseNotification('notification')
    emailInput.value = ''
  } else {
    showNotification(errorNotification)
    autoCloseNotification('error-notification')
  }
}

// Validates email format using a regular expression
function validateEmailFormat (email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

// Notification Handling
function showNotification (notification) {
  if (notification) {
    notification.style.display = 'block'
  }
}

// Hides the notification element with the given identification
function closeNotification (id) {
  const notification = document.getElementById(id)
  if (notification) {
    notification.style.display = 'none'
  }
}

// Automatically hides the notification after three seconds
function autoCloseNotification (id) {
  setTimeout(() => {
    closeNotification(id)
  }, 3000)
}

// Cart Management
function addToCart (product, price, quantityId) {
  const quantity = parseInt(document.getElementById(quantityId).value, 10)
  const cartAnnouncement = document.getElementById('cart-announcement')

  const cart = JSON.parse(localStorage.getItem('shoppingCart')) || []
  const productIndex = cart.findIndex(item => item.name === product)

  if (productIndex !== -1) {
    cart[productIndex].quantity += quantity
  } else {
    cart.push({ name: product, price: price, quantity: quantity })
  }
  localStorage.setItem('shoppingCart', JSON.stringify(cart))

  const productName = getProductName(product, quantity)
  cartAnnouncement.innerText = `${quantity} ${productName} added to your shopping cart.`
  cartAnnouncement.style.display = 'block'
  setTimeout(() => {
    cartAnnouncement.style.display = 'none'
  }, 3000)
}

// Returns the correct singular or plural product name based on quantity
function getProductName (product, quantity) {
  switch (product) {
    case 'Bamboo Straws':
      return quantity === 1 ? 'Bamboo Straw' : 'Bamboo Straws'
    case 'Recycled Bags':
      return quantity === 1 ? 'Recycled Bag' : 'Recycled Bags'
    case 'Solar Lantern':
      return quantity === 1 ? 'Solar Lantern' : 'Solar Lanterns'
    default:
      return product
  }
}