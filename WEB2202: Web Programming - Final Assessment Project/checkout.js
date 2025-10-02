// Quantity Management Functions

/**
 * Decrease the quantity of a cart item.
 * Retrieves the adjacent input field, decreases its value by 1 (if above 1),
 * and calls updateQuantity to update the subtotal and overall total.
 */
function decreaseQuantity(button, index) {
    var input = button.nextElementSibling;
    var value = parseInt(input.value);
    if (value > 1) {
        input.value = value - 1;
        updateQuantity(index, value - 1);
    }
}

/**
 * Increase the quantity of a cart item.
 * Retrieves the adjacent input field, increases its value by 1,
 * and calls updateQuantity to update the subtotal and overall total.
 */
function increaseQuantity(button, index) {
    var input = button.previousElementSibling;
    var value = parseInt(input.value);
    input.value = value + 1;
    updateQuantity(index, value + 1);
}

/**
 * Update the quantity of a specific cart item.
 * Recalculates the item's subtotal using its price and the new quantity,
 * updates the display, and refreshes the overall total price.
 */
function updateQuantity(index, quantity) {
    var cartItem = document.querySelector('.cart-item[data-index="' + index + '"]');
    var price = parseFloat(cartItem.querySelector('.item-price').textContent.replace('Price: RM', ''));
    var subtotal = price * quantity;
    cartItem.querySelector('.item-subtotal span').textContent = subtotal.toFixed(2);
    updateTotalPrice();
}

/**
 * Remove a cart item from the page.
 * Selects the item using its data-index attribute, removes it,
 * and updates the overall total price.
 */
function removeItem(index) {
    var cartItem = document.querySelector('.cart-item[data-index="' + index + '"]');
    cartItem.remove();
    updateTotalPrice();
}

/**
 * Update the overall total price.
 * Sums all individual item subtotals and updates the displayed total.
 */
function updateTotalPrice() {
    var totalPrice = 0;
    var subtotals = document.querySelectorAll('.item-subtotal span');
    subtotals.forEach(function(subtotal) {
        totalPrice += parseFloat(subtotal.textContent);
    });
    document.getElementById('total-price').textContent = totalPrice.toFixed(2);
}

// Card Number Formatting

/**
 * Format the card number input as the user types.
 * Removes extra spaces and inserts a space every 4 digits for neat formatting.
 */
function formatCardNumber(event) {
    const input = event.target;
    const value = input.value.replace(/\s+/g, '');
    const formattedValue = value.replace(/(\d{4})(?=\d)/g, '$1 ');
    input.value = formattedValue.trim();
}

// Event Listeners

// Attach the formatCardNumber function to the card number input field
document.getElementById('card-number').addEventListener('input', formatCardNumber);

// Shipping details editing: Enable modifying shipping details when the "edit-shipping" button is clicked.
document.getElementById('edit-shipping').addEventListener('click', function() {
    // Select only the text inputs within the shipping form
    var inputs = document.querySelectorAll('#shipping-form input[type="text"]');
    // Remove the readonly attribute to make fields editable
    inputs.forEach(function(input) {
        input.removeAttribute('readonly');
    });
    // Hide the Modify button
    this.style.display = 'none';

    // Create a Save button to save the modified shipping details
    var saveButton = document.createElement('button');
    saveButton.type = 'button';
    saveButton.id = 'save-shipping';
    saveButton.textContent = 'Save';
    document.getElementById('shipping-form').appendChild(saveButton);

    // When Save is clicked, reapply readonly to inputs and restore the Modify button
    saveButton.addEventListener('click', function() {
        // Optionally, send the updated data to the server via AJAX here

        // Re-add the readonly attribute to each input field
        inputs.forEach(function(input) {
            input.setAttribute('readonly', 'readonly');
        });
        // Remove the Save button and show the Modify button again
        saveButton.remove();
        document.getElementById('edit-shipping').style.display = 'inline-block';
    });
});
