// Function to decrease the quantity of an item
function decreaseQuantity(button) {
    var input = button.nextElementSibling;
    var value = parseInt(input.value);
    if (value > 1) {
        input.value = value - 1;
        updateSubtotal(button);
        updateCart(button.closest('form'));
    }
}

// Function to increase the quantity of an item
function increaseQuantity(button) {
    var input = button.previousElementSibling;
    var value = parseInt(input.value);
    input.value = value + 1;
    updateSubtotal(button);
    updateCart(button.closest('form'));
}

// Function to update the subtotal of an item
function updateSubtotal(button) {
    var form = button.closest('.update-form');
    var quantity = parseInt(form.querySelector('.quantity-input').value);
    var price = parseFloat(form.closest('.cart-item').querySelector('.item-price').textContent.replace('Price: RM', ''));
    var subtotalElement = form.closest('.cart-item').querySelector('.item-subtotal span');
    var subtotal = price * quantity;
    subtotalElement.textContent = subtotal.toFixed(2);
    updateTotalPrice();
}

// Function to update the total price of all items in the cart
function updateTotalPrice() {
    var total = 0;
    document.querySelectorAll('.item-subtotal span').forEach(function(subtotalElement) {
        total += parseFloat(subtotalElement.textContent);
    });
    document.getElementById('total-price').textContent = total.toFixed(2);
}

// Function to update the cart on the server
function updateCart(form) {
    var formData = new FormData(form);
    fetch('shopping_cart.php', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (!data.success) {
            alert('Failed to update cart.');
        }
    })
    .catch(error => {
        alert('An error occurred. Please try again.');
    });
}

// Function to remove an item from the cart
function removeFromCart(event, form) {
    event.preventDefault();
    var formData = new FormData(form);
    fetch('shopping_cart.php', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            form.closest('.cart-item').remove();
            updateTotalPrice();
        } else {
            alert('Failed to remove item from cart.');
        }
    })
    .catch(error => {
        alert('An error occurred. Please try again.');
    });
}

// Function to proceed to the checkout page
function proceedToCheckout() {
    var cartItems = JSON.parse(document.getElementById('cart-items').value);
    if (cartItems.length === 0) {
        showNotification('Your cart is empty. Cannot proceed to checkout.', true);
    } else {
        window.location.href = 'checkout.php';
    }
}

// Function to show a notification
function showNotification(message, isError) {
    var notification = document.getElementById('notification');
    var notificationMessage = document.getElementById('notification-message');
    notificationMessage.textContent = message;
    if (isError) {
        notification.classList.add('error');
    } else {
        notification.classList.remove('error');
    }
    notification.style.display = 'block';
    setTimeout(closeNotification, 3000); // Close the notification after 3 seconds
}

// Function to close the notification
function closeNotification() {
    var notification = document.getElementById('notification');
    notification.style.display = 'none';
}
