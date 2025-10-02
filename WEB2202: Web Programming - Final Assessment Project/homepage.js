// Main function to initialise event listeners
function main() {
    // Add event listener to the search button
    document.getElementById('search-button').addEventListener('click', function(event) {
        const searchTerm = document.getElementById('search-input').value.toLowerCase();
        if (searchTerm) {
            // Redirect to display_items.php with the search term as a query parameter
            window.location.href = `display_items.php?name=${encodeURIComponent(searchTerm)}`;
        } else {
            // Prevent default form submission if search term is empty
            event.preventDefault();
        }
    });
}

// Call the main function to initialise the event listeners
main();
