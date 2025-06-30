/* global window, document, localStorage, setTimeout, alert, console */

// Expose functions used in HTML to global scope
window.updateQuantity = updateQuantity
window.closeNotification = closeNotification

// Initialise on window load
window.onload = function () {
  console.log('Window loaded')
  renderCartItems()
  calculateTotalAmount()
  document.querySelector('form').onsubmit = handleFormSubmission
  setUpInputListeners()
}

// Display items in the cart
function renderCartItems () {
  console.log('Rendering cart items')
  try {
    const cart = JSON.parse(localStorage.getItem('shoppingCart')) || []
    const cartItemsContainer = document.getElementById('order-summary')
    cartItemsContainer.innerHTML = ''

    cart.forEach((item, index) => {
      const cartItemElement = document.createElement('div')
      cartItemElement.className = 'cart-item'
      cartItemElement.innerHTML = `
        <span>${item.name} - $${item.price.toFixed(2)} x ${item.quantity}</span>
        <div class="quantity-buttons">
          <button type="button" onclick="updateQuantity(${index}, -1)">-</button>
          <button type="button" onclick="updateQuantity(${index}, 1)">+</button>
        </div>
      `
      cartItemsContainer.appendChild(cartItemElement)
    })

    calculateTotalAmount()
  } catch (error) {
    console.error('Error displaying cart items:', error)
  }
}

// Calculate and display total amount
function calculateTotalAmount () {
  console.log('Calculating total amount')
  try {
    const cart = JSON.parse(localStorage.getItem('shoppingCart')) || []
    let totalAmount = 0

    cart.forEach(item => {
      totalAmount += item.price * item.quantity
    })

    const totalAmountElement = document.getElementById('total-amount')
    if (totalAmountElement) {
      totalAmountElement.innerText = cart.length > 0
        ? `Total: $${totalAmount.toFixed(2)}`
        : 'Total: $0.00'
    } else {
      console.error('Element with ID "total-amount" not found.')
    }
  } catch (error) {
    console.error('Error calculating total amount:', error)
  }
}

// Update quantity of items in the cart
function updateQuantity (index, change) {
  console.log('Updating quantity')
  try {
    const cart = JSON.parse(localStorage.getItem('shoppingCart')) || []
    if (cart[index].quantity + change > 0) {
      cart[index].quantity += change
    } else {
      cart.splice(index, 1)
    }
    localStorage.setItem('shoppingCart', JSON.stringify(cart))
    renderCartItems()
  } catch (error) {
    console.error('Error updating quantity:', error)
  }
}

// Handle form submission
function handleFormSubmission (event) {
  event.preventDefault()
  console.log('Form submitted')

  const totalAmount = parseFloat(document.getElementById('total-amount').innerText.replace('Total: $', ''))
  if (totalAmount === 0) {
    showOrderErrorNotification()
    return false
  }

  if (validateForm()) {
    showOrderSuccessNotification()
    setTimeout(() => {
      clearOrderSummary()
      clearFormFields()
    }, 3000)
  }
}

// Set up input listeners
function setUpInputListeners () {
  console.log('Setting up input listeners')
  document.getElementById('card-number').addEventListener('input', formatCardNumber)
  document.getElementById('exp-date').addEventListener('input', formatExpDate)
}

// Format card number
function formatCardNumber (event) {
  const input = event.target
  const value = input.value.replace(/\s+/g, '')
  const formattedValue = value.replace(/(\d{4})(?=\d)/g, '$1 ')
  input.value = formattedValue.trim()
}

// Format expiration date
function formatExpDate (event) {
  const input = event.target
  let value = input.value.replace(/[^0-9]/g, '')
  if (value.length > 2) {
    value = value.slice(0, 2) + '/' + value.slice(2)
  }
  input.value = value
}

// Validate form inputs
function validateForm () {
  console.log('Validating form')
  try {
    const nameRegex = /^[\p{L} .'-]+$/u
    const zipRegex = /^[A-Za-z0-9 -]+$/

    if (!document.getElementById('full-name').value.match(nameRegex)) {
      alert('Please enter a valid full name.')
      return false
    }

    if (document.getElementById('address').value.trim() === '') {
      alert('Address field cannot be empty.')
      return false
    }

    if (document.getElementById('city').value.trim() === '') {
      alert('Please enter a valid city name.')
      return false
    }

    if (!document.getElementById('zip').value.match(zipRegex)) {
      alert('Please enter a valid postal code.')
      return false
    }

    if (!document.getElementById('state').value.match(nameRegex)) {
      alert('Please enter a valid state name.')
      return false
    }

    if (document.getElementById('country').value.trim() === '') {
      alert('Please enter a valid country name.')
      return false
    }

    if (!document.getElementById('card-name').value.match(nameRegex)) {
      alert('Please enter a valid name on card.')
      return false
    }

    if (!document.getElementById('card-number').value.replace(/\s+/g, '').match(/^\d{16}$/)) {
      alert('Please enter a valid 16-digit card number.')
      return false
    }

    try {
      const expDate = document.getElementById('exp-date').value
      if (!expDate.match(/^(0[1-9]|1[0-2])\/\d{2}$/)) {
        throw new Error('Invalid expiration date format.')
      }
      const [month, year] = expDate.split('/').map(num => parseInt(num, 10))
      const currentDate = new Date()
      const currentMonth = currentDate.getMonth() + 1
      const currentYear = currentDate.getFullYear() % 100
      if (year < currentYear || (year === currentYear && month < currentMonth)) {
        throw new Error('The card has expired.')
      }
    } catch (error) {
      alert(`Please enter a valid expiration date. ${error.message}`)
      return false
    }

    try {
      const cvv = document.getElementById('cvv').value
      if (!cvv.match(/^\d{3}$/)) {
        throw new Error('The Card Verification Value (CVV) format is invalid.')
      }
    } catch (error) {
      alert(`Please enter a valid 3-digit Card Verification Value (CVV). ${error.message}`)
      return false
    }

    return true
  } catch (error) {
    console.error('Validation error:', error)
    alert('There was an error validating the form. Please try again.')
    return false
  }
}

// Show success notification
function showOrderSuccessNotification () {
  console.log('Showing order success notification')
  document.getElementById('order-success-notification').style.display = 'block'
}

// Show error notification
function showOrderErrorNotification () {
  console.log('Showing order error notification')
  document.getElementById('order-error-notification').style.display = 'block'
}

// Close notification
function closeNotification (id) {
  console.log('Closing notification')
  document.getElementById(id).style.display = 'none'
}

// Clear cart and form
function clearOrderSummary () {
  console.log('Clearing order summary')
  localStorage.removeItem('shoppingCart')
  renderCartItems()
  calculateTotalAmount()
}

function clearFormFields () {
  console.log('Clearing form fields')
  document.querySelector('form').reset()
}

// Clear cart on unload
window.onbeforeunload = clearOrderSummary