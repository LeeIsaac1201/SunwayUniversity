// Quantity Controls Functions
function decreaseQuantity(button) {
    var input = button.nextElementSibling;
    var value = parseInt(input.value);
    if (value > 1) {
        input.value = value - 1;
        updateQuantity(button, value - 1);
    }
}

function increaseQuantity(button) {
    var input = button.previousElementSibling;
    var value = parseInt(input.value);
    input.value = value + 1;
    updateQuantity(button, value + 1);
}

function updateQuantity(button, quantity) {
    var form = button.closest('.item').querySelector('form');
    form.querySelector('.item-quantity').value = quantity;
}

// Add to Cart Function
function addToCart(event, itemId) {
    event.preventDefault();
    var form = document.getElementById('add-to-cart-form-' + itemId);
    var formData = new FormData(form);

    fetch('shopping_cart.php', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification('Item added to cart successfully!', false);
        } else {
            showNotification('Failed to add item to cart.', true);
        }
    })
    .catch(error => {
        showNotification('An error occurred. Please try again.', true);
    });
}

// Notification Functions
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

function closeNotification() {
    var notification = document.getElementById('notification');
    notification.style.display = 'none';
}

// Scroll to Product on Search
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('name');
    if (searchQuery) {
        const productElement = document.getElementById(searchQuery);
        if (productElement) {
            productElement.scrollIntoView({ behavior: 'smooth' });
        }
    }
});
